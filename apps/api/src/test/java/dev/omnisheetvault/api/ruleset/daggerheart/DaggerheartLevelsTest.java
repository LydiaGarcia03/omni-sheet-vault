package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.MAPPER;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.json;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.valid;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

class DaggerheartLevelsTest {

    private final DaggerheartTestCatalogue catalogue = new DaggerheartTestCatalogue();
    private final DaggerheartCharacterCreationFlow flow = new DaggerheartCharacterCreationFlow(MAPPER);
    private final DaggerheartDerivedStats stats = new DaggerheartDerivedStats(MAPPER);

    private static String level(String experience, String card, String... advancements) {
        return """
                {"experience":"%s","domainCard":"%s","advancements":[%s]}""".formatted(experience, card, String.join(",", advancements));
    }

    private static ObjectNode leveled(String... levels) {
        ObjectNode build = valid();
        ArrayNode array = build.putArray("levels");
        for (String level : levels) {
            array.add(MAPPER.readTree(level));
        }
        return build;
    }

    private static final String LEVEL_2 = level("Archer", "sage-two", "{\"option\":\"traits\",\"tier\":2,\"traits\":[\"agility\",\"strength\"]}",
            "{\"option\":\"hitPoint\",\"tier\":2}");
    private static final String LEVEL_3 = level("", "bone-three", "{\"option\":\"traits\",\"tier\":2,\"traits\":[\"finesse\",\"instinct\"]}",
            "{\"option\":\"stress\",\"tier\":2}");
    private static final String LEVEL_4 = level("", "sage-four", "{\"option\":\"traits\",\"tier\":2,\"traits\":[\"presence\",\"knowledge\"]}",
            "{\"option\":\"evasion\",\"tier\":2}");

    private List<String> problems(ObjectNode build) {
        return flow.plan(json(build), catalogue).problems();
    }

    private JsonNode sheet(ObjectNode build) {
        return MAPPER.readTree(flow.materialize(null, json(build), catalogue).sheetJson());
    }

    @Test
    void levelTwoTakesTheTierAchievementTwoAdvancementsAndACard() {
        JsonNode sheet = sheet(leveled(LEVEL_2));
        JsonNode derived = stats.derive(sheet);

        assertThat(sheet.path("level").asInt()).isEqualTo(2);
        assertThat(derived.path("proficiency").asInt()).isEqualTo(2);
        assertThat(derived.path("traits").path("agility").path("value").asInt()).isEqualTo(3);
        assertThat(derived.path("hitPointSlots").path("value").asInt()).isEqualTo(7);
        assertThat(derived.path("majorThreshold").path("value").asInt()).isEqualTo(8);
        assertThat(derived.path("severeThreshold").path("value").asInt()).isEqualTo(15);
        assertThat(sheet.path("experiences").path(2).path("name").asString()).isEqualTo("Archer");
        assertThat(sheet.path("experiences").path(2).path("bonus").asInt()).isEqualTo(2);
        assertThat(sheet.path("domainCards")).extracting(card -> card.path("slug").asString()).contains("sage-two");
        assertThat(derived.path("hitPointSlots").path("contributions").path(1).path("source").asString()).isEqualTo("Level 2 advancement");
    }

    @Test
    void aValidRunToLevelFourHasNoProblems() {
        assertThat(problems(leveled(LEVEL_2, LEVEL_3, LEVEL_4))).isEmpty();
    }

    @Test
    void anOptionCantTakeMoreThanItsTiersSlots() {
        String hitPoints = level("", "bone-three", "{\"option\":\"hitPoint\",\"tier\":2}", "{\"option\":\"hitPoint\",\"tier\":2}");

        assertThat(problems(leveled(LEVEL_2, hitPoints))).containsExactly("Level 3: Tier 2 has 2 slots for \"Permanently gain one Hit Point slot\".");
    }

    @Test
    void aRaisedTraitStaysMarkedUntilLevelFive() {
        String again = level("", "bone-three", "{\"option\":\"traits\",\"tier\":2,\"traits\":[\"agility\",\"finesse\"]}", "{\"option\":\"stress\",\"tier\":2}");

        assertThat(problems(leveled(LEVEL_2, again))).containsExactly("Level 3: Agility is marked until level 5.");
    }

    @Test
    void tierThreeOptionsWaitForLevelFive() {
        String proficiency = level("Archer", "sage-two", "{\"option\":\"proficiency\",\"tier\":2}");

        assertThat(problems(leveled(proficiency))).containsExactly("Level 2: \"Increase your Proficiency by +1\" starts at Tier 3.");
    }

    @Test
    void aCardAboveTheLevelIsRefused() {
        String tooHigh = level("Archer", "sage-four", "{\"option\":\"hitPoint\",\"tier\":2}", "{\"option\":\"stress\",\"tier\":2}");

        assertThat(problems(leveled(tooHigh))).singleElement().asString().contains("beyond your level");
    }

    @Test
    void multiclassingAtLevelFiveAddsTheClassFeatureFoundationAndADomain() {
        String level5 = level("Scout", "codex-three",
                "{\"option\":\"multiclass\",\"tier\":3,\"class\":\"wizard\",\"domain\":\"codex\",\"subclass\":\"school-of-knowledge\"}");
        ObjectNode build = leveled(LEVEL_2, LEVEL_3, LEVEL_4, level5);

        assertThat(problems(build)).isEmpty();
        JsonNode sheet = sheet(build);
        assertThat(sheet.path("identity").path("multiclass").path("class").path("name").asString()).isEqualTo("Wizard");
        assertThat(sheet.path("features")).extracting(feature -> feature.path("name").asString()).contains("Prestidigitation", "Prepared");
        assertThat(stats.derive(sheet).path("proficiency").asInt()).isEqualTo(3);
    }

    @Test
    void multiclassingCrossesOutTheTiersUpgradedSubclass() {
        String level5 = level("Scout", "codex-three",
                "{\"option\":\"multiclass\",\"tier\":3,\"class\":\"wizard\",\"domain\":\"codex\",\"subclass\":\"school-of-knowledge\"}");
        String level6 = level("", "bone-five", "{\"option\":\"subclass\",\"tier\":3}", "{\"option\":\"hitPoint\",\"tier\":3}");

        assertThat(problems(leveled(LEVEL_2, LEVEL_3, LEVEL_4, level5, level6)))
                .containsExactly("Level 6: multiclassing crossed out the upgraded subclass in Tier 3.");
    }

    @Test
    void anUpgradedSubclassAddsTheSpecialization() {
        String level5 = level("Scout", "bone-five", "{\"option\":\"subclass\",\"tier\":3}", "{\"option\":\"traits\",\"tier\":3,\"traits\":[\"agility\",\"strength\"]}");

        JsonNode sheet = sheet(leveled(LEVEL_2, LEVEL_3, LEVEL_4, level5));

        assertThat(sheet.path("features")).extracting(feature -> feature.path("name").asString()).contains("Elusive Predator").doesNotContain("Apex Predator");
    }

    @Test
    void reassemblingKeepsPlayStateAndCardPlaces() {
        ObjectNode current = (ObjectNode) sheet(valid());
        ((ObjectNode) current.get("trackers")).put("hope", 5).put("hitPointsMarked", 2);
        ((ObjectNode) current.path("domainCards").get(0)).put("inLoadout", false);
        current.put("notes", "Kept");

        JsonNode leveled = MAPPER.readTree(flow.materialize(json(current), json(leveled(LEVEL_2)), catalogue).sheetJson());

        assertThat(leveled.path("trackers").path("hope").asInt()).isEqualTo(5);
        assertThat(leveled.path("notes").asString()).isEqualTo("Kept");
        assertThat(leveled.path("domainCards").path(0).path("inLoadout").asBoolean()).isFalse();
        assertThat(leveled.path("domainCards").path(2).path("inLoadout").asBoolean()).isTrue();
    }
}

package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.MAPPER;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.json;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.valid;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

class DaggerheartDerivedStatsTest {

    private final DaggerheartCharacterCreationFlow flow = new DaggerheartCharacterCreationFlow(MAPPER);
    private final DaggerheartDerivedStats stats = new DaggerheartDerivedStats(MAPPER);
    private final DaggerheartTestCatalogue catalogue = new DaggerheartTestCatalogue();

    private JsonNode derived(ObjectNode build) {
        return stats.derive(MAPPER.readTree(flow.materialize(null, json(build), catalogue).sheetJson()));
    }

    @Test
    void derivesEvasionArmorAndThresholdsFromClassArmorAndFeatures() {
        JsonNode derived = derived(valid());

        assertThat(derived.path("evasion").path("value").asInt()).isEqualTo(13);
        assertThat(derived.path("evasion").path("contributions")).extracting(part -> part.path("source").asString())
                .containsExactly("Ranger", "Nimble");
        assertThat(derived.path("armorScore").path("value").asInt()).isEqualTo(3);
        assertThat(derived.path("majorThreshold").path("value").asInt()).isEqualTo(7);
        assertThat(derived.path("severeThreshold").path("value").asInt()).isEqualTo(14);
        assertThat(derived.path("hitPointSlots").path("value").asInt()).isEqualTo(6);
        assertThat(derived.path("stressSlots").path("value").asInt()).isEqualTo(6);
        assertThat(derived.path("maxHope").asInt()).isEqualTo(6);
        assertThat(derived.path("proficiency").asInt()).isEqualTo(1);
        assertThat(derived.path("traits").path("agility").path("value").asInt()).isEqualTo(2);
    }

    @Test
    void equippedGearAddsItsFeaturesBonuses() {
        JsonNode derived = derived(valid().put("primaryWeapon", "broadsword").put("secondaryWeapon", "round-shield").put("armor", "chainmail-armor"));

        assertThat(derived.path("armorScore").path("value").asInt()).isEqualTo(5);
        assertThat(derived.path("evasion").path("value").asInt()).isEqualTo(12);
        assertThat(derived.path("attacks")).extracting(attack -> attack.path("damage").asString()).containsExactly("1d8", "1d4");
    }

    @Test
    void aSubclassFoundationRaisesTheSevereThreshold() {
        JsonNode derived = derived(valid().put("class", "brawler").put("subclass", "juggernaut").put("primaryWeapon", "")
                .put("classItem", "A book").set("domainCards", MAPPER.createArrayNode().add("bone-card").add("valor-card")));

        assertThat(derived.path("severeThreshold").path("value").asInt()).isEqualTo(17);
    }

    @Test
    void unarmoredThresholdsAreTheLevelAndTwiceTheLevel() {
        ObjectNode sheet = MAPPER.createObjectNode().put("level", 1);

        JsonNode derived = stats.derive(sheet);

        assertThat(derived.path("majorThreshold").path("value").asInt()).isEqualTo(1);
        assertThat(derived.path("severeThreshold").path("value").asInt()).isEqualTo(2);
        assertThat(derived.path("armorScore").path("value").asInt()).isZero();
    }

    @Test
    void aSpellcastWeaponUsesTheSubclasssSpellcastTrait() {
        ObjectNode sheet = (ObjectNode) MAPPER.readTree("""
                {"level":1,"proficiency":1,"traits":{"knowledge":2},
                 "identity":{"subclass":{"spellcastTrait":"Knowledge"}},
                 "weapons":{"primary":{"name":"Arcane-Frame Wheelchair","trait":"Spellcast","range":"Far",
                   "damage":{"die":"d6","bonus":0,"type":"magic"},"burden":1}}}""");

        JsonNode attack = stats.derive(sheet).path("attacks").get(0);

        assertThat(attack.path("trait").asString()).isEqualTo("Knowledge");
        assertThat(attack.path("modifier").asInt()).isEqualTo(2);
        assertThat(attack.path("damage").asString()).isEqualTo("1d6");
    }
}

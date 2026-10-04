package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.groups.Default;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

class Dnd5eCharacterBuildTest {

    private final ObjectMapper objectMapper = Dnd5eSheetJsonMapper.lenient(new ObjectMapper());
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void roundTripsThroughJson() {
        Dnd5eCharacterBuild build = multiclassBuild();

        String json = objectMapper.writeValueAsString(build);
        Dnd5eCharacterBuild read = objectMapper.readValue(json, Dnd5eCharacterBuild.class);

        assertThat(read).isEqualTo(build);
        assertThat(read.characterLevel()).isEqualTo(7);
    }

    @Test
    void aValidBuildHasNoViolations() {
        assertThat(validator.validate(multiclassBuild())).isEmpty();
    }

    @Test
    void rejectsMoreThanTwentyTotalLevels() {
        Dnd5eCharacterBuild build = withClasses(List.of(
                new Dnd5eBuildClass("sorcerer", null, 15), new Dnd5eBuildClass("rogue", null, 6)));

        assertThat(messages(validator.validate(build))).contains("total class levels must be between 1 and 20");
    }

    @Test
    void rejectsTheSameClassListedTwiceAndMissingAbilities() {
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(
                "dwarf", "Mountain", null, "folk-hero",
                List.of(new Dnd5eBuildClass("fighter", null, 1), new Dnd5eBuildClass("fighter", null, 2)),
                Dnd5eAbilityScoreMethod.MANUAL, Map.of("strength", 15),
                Dnd5eHitPointMethod.FIXED, List.of(), List.of());

        assertThat(messages(validator.validate(build, Default.class, Dnd5eCharacterBuild.Complete.class))).contains(
                "each class may appear only once", "base ability scores must name exactly the six abilities");
    }

    @Test
    void aDraftWithNothingChosenIsValidButNotComplete() {
        Dnd5eCharacterBuild draft = new Dnd5eCharacterBuild(null, null, null, null, null, null, null, null, null, null);

        assertThat(validator.validate(draft)).isEmpty();
        assertThat(messages(validator.validate(draft, Dnd5eCharacterBuild.Complete.class))).contains(
                "base ability scores must name exactly the six abilities");
        assertThat(validator.validate(draft, Dnd5eCharacterBuild.Complete.class)).extracting(v -> v.getPropertyPath().toString())
                .contains("speciesSlug", "backgroundSlug", "classes", "abilityScoreMethod");
        assertThat(draft.classes()).isEmpty();
        assertThat(draft.hitPointMethod()).isEqualTo(Dnd5eHitPointMethod.FIXED);
    }

    @Test
    void aDraftStillRejectsScoresForUnknownAbilities() {
        Dnd5eCharacterBuild draft = new Dnd5eCharacterBuild(
                null, null, null, null, null, null, Map.of("luck", 12), null, null, null);

        assertThat(messages(validator.validate(draft))).contains("base ability scores may only name the six abilities");
    }

    @Test
    void perLevelSpellAnswersReadAsTheClassPoolInLevelOrder() {
        String json = """
                {"classes": [{"classSlug": "sorcerer", "level": 3}],
                 "choices": [
                   {"id": "class:sorcerer:1:skills", "selections": ["arcana"]},
                   {"id": "class:sorcerer:4:asi", "selections": ["charisma", "charisma"]},
                   {"id": "class:sorcerer:3:spells", "selections": ["misty-step"]},
                   {"id": "class:sorcerer:1:cantrips", "selections": ["fire-bolt", "mage-hand"]},
                   {"id": "class:sorcerer:1:spells", "selections": ["shield", "chromatic-orb"]},
                   {"id": "class:sorcerer:2:spells", "selections": ["magic-missile"]},
                   {"id": "class:wizard:4:spellbook", "selections": ["fireball"]},
                   {"id": "class:wizard:1:spellbook", "selections": ["sleep", "fireball"]}]}
                """;

        Dnd5eCharacterBuild build = objectMapper.readValue(json, Dnd5eCharacterBuild.class);

        assertThat(build.choices()).containsExactly(
                new Dnd5eBuildChoice("class:sorcerer:1:skills", List.of("arcana")),
                new Dnd5eBuildChoice("class:sorcerer:4:asi", List.of("charisma", "charisma")),
                new Dnd5eBuildChoice("class:sorcerer:spells", List.of("shield", "chromatic-orb", "magic-missile", "misty-step")),
                new Dnd5eBuildChoice("class:sorcerer:cantrips", List.of("fire-bolt", "mage-hand")),
                new Dnd5eBuildChoice("class:wizard:spellbook", List.of("sleep", "fireball")));
        assertThat(validator.validate(build)).isEmpty();
    }

    @Test
    void aPooledAnswerWinsOverPerLevelAnswersForTheSamePool() {
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(null, null, null, null, null, null, null, null, null, List.of(
                new Dnd5eBuildChoice("class:sorcerer:1:cantrips", List.of("fire-bolt", "mage-hand")),
                new Dnd5eBuildChoice("class:sorcerer:cantrips", List.of("mage-hand", "acid-splash"))));

        assertThat(build.choices()).containsExactly(new Dnd5eBuildChoice("class:sorcerer:cantrips", List.of("mage-hand", "acid-splash")));
    }

    @Test
    void pooledSpellAnswersStayAsTheyAre() {
        List<Dnd5eBuildChoice> choices = List.of(
                new Dnd5eBuildChoice("class:sorcerer:cantrips", List.of("fire-bolt")),
                new Dnd5eBuildChoice("class:sorcerer:4:asi-or-feat", List.of("asi")));

        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(null, null, null, null, null, null, null, null, null, choices);

        assertThat(build.choices()).isEqualTo(choices);
    }

    @Test
    void aVersionOneSheetWithoutABuildStillReads() {
        Dnd5eSheet sheet = objectMapper.readValue(versionOneSheetJson(), Dnd5eSheet.class);

        assertThat(sheet.build()).isNull();
        assertThat(sheet.schemaVersion()).isEqualTo(1);
    }

    private String versionOneSheetJson() {
        Dnd5eSheet current = new Dnd5eSheet(
                10, 10, 10, 10, 10, 10, 1, 8, 30, Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 8, 0, 0, false, List.of(), List.of(),
                List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0,
                List.of(), List.of(), List.of(), false, null, null, null, null, null, 1);
        ObjectNode json = objectMapper.valueToTree(current);
        json.remove("build");
        return json.toString();
    }

    private static Set<String> messages(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.toSet());
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }

    private static Dnd5eCharacterBuild multiclassBuild() {
        return withClasses(List.of(new Dnd5eBuildClass("sorcerer", "sorcerer-draconic", 3), new Dnd5eBuildClass("rogue", null, 4)));
    }

    private static Dnd5eCharacterBuild withClasses(List<Dnd5eBuildClass> classes) {
        return new Dnd5eCharacterBuild(
                "changeling-mpmm", null, null, "urchin", classes,
                Dnd5eAbilityScoreMethod.STANDARD_ARRAY,
                Map.of("strength", 8, "dexterity", 15, "constitution", 13, "intelligence", 10, "wisdom", 12, "charisma", 14),
                Dnd5eHitPointMethod.FIXED, List.of(),
                List.of(new Dnd5eBuildChoice("class:sorcerer:1:skills", List.of("arcana", "persuasion"))));
    }
}

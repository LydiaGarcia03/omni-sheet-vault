package dev.omnisheetvault.api.ruleset.vtm5;

import static dev.omnisheetvault.api.ruleset.vtm5.Vtm5TestBuilds.CONTENT;
import static dev.omnisheetvault.api.ruleset.vtm5.Vtm5TestBuilds.MAPPER;
import static dev.omnisheetvault.api.ruleset.vtm5.Vtm5TestBuilds.json;
import static dev.omnisheetvault.api.ruleset.vtm5.Vtm5TestBuilds.valid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.ruleset.LevelUpNotAllowedException;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

class Vtm5CharacterCreationFlowTest {

    private final Vtm5CharacterCreationFlow flow = new Vtm5CharacterCreationFlow(MAPPER, CONTENT);

    @Test
    void anEmptyDraftHasEveryStepPending() {
        BuildPlan plan = flow.plan(flow.emptyDraft(), null);

        assertThat(plan.pending()).extracting(CreationChoice::type)
                .contains("basics", "clan", "attributes", "skills", "predator", "advantages", "beliefs");
        assertThat(plan.problems()).isEmpty();
    }

    @Test
    void aCompleteBuildHasNothingPendingAndNoProblems() {
        BuildPlan plan = flow.plan(Vtm5TestBuilds.VALID, null);

        assertThat(plan.pending()).isEmpty();
        assertThat(plan.problems()).isEmpty();
    }

    @Test
    void materializesTheSheetWithThePredatorTypesGifts() {
        MaterializedSheet materialized = flow.materialize(null, Vtm5TestBuilds.VALID, null);
        JsonNode sheet = MAPPER.readTree(materialized.sheetJson());

        assertThat(sheet.path("identity").path("clan").asString()).isEqualTo("Toreador");
        assertThat(sheet.path("humanity").asInt()).isEqualTo(8);
        assertThat(sheet.path("bloodPotency").asInt()).isEqualTo(1);
        assertThat(sheet.path("hunger").asInt()).isEqualTo(1);
        assertThat(sheet.path("disciplines").path("Auspex").path("dots").asInt()).isEqualTo(3);
        assertThat(sheet.path("specialties").path("Medicine").get(0).asString()).isEqualTo("Phlebotomy");
        assertThat(sheet.path("xpLog").get(0).path("amount").asInt()).isEqualTo(15);
        assertThat(sheet.path("advantages")).hasSize(6);
        assertThat(materialized.startingItems()).isEmpty();
    }

    @Test
    void aPredatorSpecialtyOnAnUntrainedSkillGivesItADot() {
        ObjectNode build = valid();
        ((ObjectNode) build.path("skills")).remove("Medicine");
        ((ObjectNode) build.path("skills")).put("Survival", 1);

        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), null).sheetJson());

        assertThat(sheet.path("skills").path("Medicine").asInt()).isEqualTo(1);
    }

    @Test
    void doesNotMaterializeAnUnfinishedBuild() {
        MaterializedSheet materialized = flow.materialize(null, flow.emptyDraft(), null);

        assertThat(materialized.isMaterialized()).isFalse();
    }

    @Test
    void flagsTooManyAttributesAtOneRating() {
        ObjectNode build = valid();
        ((ObjectNode) build.path("attributes")).put("Wits", 4);

        assertThat(flow.plan(json(build), null).problems()).anyMatch(problem -> problem.startsWith("Too many attributes at 4"));
    }

    @Test
    void attributesCantStartAboveFour() {
        ObjectNode build = valid();
        ((ObjectNode) build.path("attributes")).put("Intelligence", 5);

        assertThat(flow.plan(json(build), null).problems()).anyMatch(problem -> problem.contains("can't start above 4"));
    }

    @Test
    void creationDisciplinesComeFromTheClan() {
        ObjectNode build = valid();
        build.put("disciplineOne", "Potence");

        assertThat(flow.plan(json(build), null).problems()).contains("Potence is not one of your clan's disciplines.");
    }

    @Test
    void aPowerAboveTheDisciplinesRatingIsAProblem() {
        ObjectNode build = valid();
        build.putObject("powers").putArray("Celerity").add("Blink");

        assertThat(flow.plan(json(build), null).problems()).anyMatch(problem -> problem.contains("Blink is above"));
    }

    @Test
    void somePredatorTypesAreClosedToSomeClans() {
        ObjectNode build = valid();
        build.put("clan", "Ventrue");
        build.put("predator", "Farmer");

        assertThat(flow.plan(json(build), null).problems()).contains("Farmer is not open to Ventrue.");
    }

    @Test
    void bloodSorceryFromAPredatorNeedsASorcererClan() {
        ObjectNode build = valid();
        build.put("predator", "Bagger");
        build.put("predatorDiscipline", "Blood Sorcery");

        assertThat(flow.plan(json(build), null).problems()).anyMatch(problem -> problem.startsWith("Blood Sorcery from a predator type"));
    }

    @Test
    void advantagesOverTheBudgetAreAProblem() {
        ObjectNode build = valid();
        ((ObjectNode) build.path("advantages")).put("Resources", 5);

        assertThat(flow.plan(json(build), null).problems()).anyMatch(problem -> problem.startsWith("Advantages spend"));
    }

    @Test
    void anAncillaHasLargerBudgetsAndLessHumanity() {
        ObjectNode build = valid();
        build.put("age", "Ancilla");
        build.put("generation", "11th");
        ((ObjectNode) build.path("advantages")).put("Allies", 2);
        ((ObjectNode) build.path("advantages")).put("Ugly", 1);
        ((ObjectNode) build.path("advantages")).put("Destitute", 1);

        BuildPlan plan = flow.plan(json(build), null);
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), null).sheetJson());

        assertThat(plan.pending()).isEmpty();
        assertThat(sheet.path("humanity").asInt()).isEqualTo(7);
        assertThat(sheet.path("bloodPotency").asInt()).isEqualTo(2);
    }

    @Test
    void unknownNamesAreProblems() {
        ObjectNode build = valid();
        build.put("clan", "Lasombra");
        build.put("predator", "Nobody");
        ((ObjectNode) build.path("advantages")).put("Wings", 1);

        assertThat(flow.plan(json(build), null).problems())
                .contains("Unknown clan: Lasombra", "Unknown predator type: Nobody", "Unknown advantage: Wings");
    }

    @Test
    void aThinBloodNeedsNoDisciplinesAndHasBloodPotencyZero() {
        ObjectNode build = valid();
        build.put("clan", "Thin-blooded");
        build.remove("disciplineTwo");
        build.remove("disciplineOne");
        build.put("predatorDiscipline", "Fortitude");
        build.putObject("powers").putArray("Fortitude").add("Resilience");
        build.putArray("thinBloodMerits").add("Thin-blood Alchemist");
        build.putArray("thinBloodFlaws").add("Baby Teeth");
        build.put("alchemyMethod", "Fixatio");
        ((ObjectNode) build.path("powers")).putArray("Thin-Blood Alchemy").add("Far Reach");

        BuildPlan plan = flow.plan(json(build), null);
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), null).sheetJson());

        assertThat(plan.choices()).noneMatch(choice -> choice.id().equals("disciplines"));
        assertThat(sheet.path("bloodPotency").asInt()).isZero();
        assertThat(sheet.path("identity").path("generation").asString()).isEqualTo("14th–16th");
        assertThat(plan.pending()).isEmpty();
        assertThat(plan.problems()).isEmpty();
        assertThat(sheet.path("disciplines").path("Thin-Blood Alchemy").path("powers").get(0).asString()).isEqualTo("Far Reach");
        assertThat(sheet.path("thinBlood").path("alchemyMethod").asString()).isEqualTo("Fixatio");
    }

    @Test
    void aCaitiffPicksAnyDisciplines() {
        ObjectNode build = valid();
        build.put("clan", "Caitiff");
        build.put("disciplineOne", "Potence");
        build.putObject("powers").put("x", 0);
        ((ObjectNode) build.path("powers")).remove("x");
        ((ObjectNode) build.path("powers")).putArray("Auspex").add("Heightened Senses").add("Sense the Unseen").add("Premonition");
        ((ObjectNode) build.path("powers")).putArray("Potence").add("Lethal Body");

        assertThat(flow.plan(json(build), null).problems()).isEmpty();
    }

    @Test
    void readsTheBuildBackFromItsSheet() {
        String sheet = flow.materialize(null, Vtm5TestBuilds.VALID, null).sheetJson();

        assertThat(flow.buildOf(sheet)).hasValueSatisfying(build -> assertThat(build).contains("Toreador"));
        assertThat(flow.buildOf("{}")).isEmpty();
    }

    @Test
    void rejectsMalformedBuildsAndHasNoLevels() {
        assertThatThrownBy(() -> flow.plan("not json", null)).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> flow.plan("[]", null)).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> flow.withLevelUp(Vtm5TestBuilds.VALID, "fighter", null)).isInstanceOf(LevelUpNotAllowedException.class);
        assertThat(flow.preview(Vtm5TestBuilds.VALID, null).sheetJson()).isNull();
        assertThat(flow.systemId()).isEqualTo("vtm-v5");
    }

    @Test
    void aBloodSorcererStartsWithOneLevelOneRitual() {
        ObjectNode build = valid();
        build.put("clan", "Tremere");
        build.put("disciplineOne", "Blood Sorcery");
        build.putObject("powers").putArray("Auspex").add("Heightened Senses").add("Sense the Unseen").add("Premonition");
        ((ObjectNode) build.path("powers")).putArray("Blood Sorcery").add("A Taste for Blood");

        assertThat(flow.plan(json(build), null).pending()).extracting(CreationChoice::id).containsExactly("ritual");

        build.put("ritual", "Truth of Blood");
        assertThat(flow.plan(json(build), null).problems()).contains("Truth of Blood is not a level-1 ritual.");

        build.put("ritual", "Blood Walk");
        assertThat(flow.plan(json(build), null).pending()).isEmpty();
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), null).sheetJson());
        assertThat(sheet.path("rituals").get(0).asString()).isEqualTo("Blood Walk");
    }

    @Test
    void thinBloodMeritsComeInPairsWithFlawsAndAreThreeAtMost() {
        ObjectNode build = valid();
        build.put("clan", "Thin-blooded");
        build.remove("disciplineTwo");
        build.remove("disciplineOne");
        build.putArray("thinBloodMerits").add("Day Drinker").add("Lifelike").add("Lifelike").add("Lifelike").add("Wings");
        build.putArray("thinBloodFlaws").add("Baby Teeth").add("Dead Flesh").add("Branded").add("Mortal Frailty").add("Vitae Dependency").add("Horns");

        assertThat(flow.plan(json(build), null).problems())
                .contains("Unknown thin-blood merit: Wings", "Unknown thin-blood flaw: Horns", "At most 3 thin-blood merits.", "Take as many thin-blood flaws as merits.");
    }

    @Test
    void loresheetEntriesSpendAdvantagePointsAndRespectClans() {
        ObjectNode build = valid();
        ((ObjectNode) build.path("advantages")).put("Contacts", 0);
        build.putObject("loresheets").putArray("Descendant of Helena").add(1);
        assertThat(flow.plan(json(build), null).pending()).isEmpty();
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), null).sheetJson());
        assertThat(sheet.path("loresheets").path("Descendant of Helena").get(0).asInt()).isEqualTo(1);

        ((ObjectNode) build.path("loresheets")).putArray("Descendant of Tyler").add(2);
        ((ObjectNode) build.path("loresheets")).putArray("Nobody").add(1);
        assertThat(flow.plan(json(build), null).problems())
                .contains("Descendant of Tyler is only for the Brujah.", "Unknown loresheet: Nobody")
                .anyMatch(problem -> problem.startsWith("Advantages spend"));
    }

    @Test
    void aCoterieContributionSpendsAdvantagePoints() {
        ObjectNode build = valid();
        ((ObjectNode) build.path("advantages")).put("Resources", 0);
        build.put("coterieContribution", 2);
        assertThat(flow.plan(json(build), null).pending()).isEmpty();
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), null).sheetJson());
        assertThat(sheet.path("coterie").path("contribution").asInt()).isEqualTo(2);

        build.put("coterieContribution", 9);
        assertThat(flow.plan(json(build), null).problems()).contains("A coterie contribution is 0 to 7 points.");
    }
}

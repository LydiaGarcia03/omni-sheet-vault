package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.RollModeInfo;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Conditions and exhaustion (C3c): copied modifiers, roll modes, speed and hit point maximum. */
class Dnd5eConditionEffectsTest {

    private static final String POISONED = """
            {"modifiers":[{"type":"DISADVANTAGE","target":"ATTACK_ROLLS","value":0},
                          {"type":"DISADVANTAGE","target":"ABILITY_CHECKS","value":0}]}""";
    private static final String RESTRAINED = """
            {"modifiers":[{"type":"SET","target":"SPEED","value":0},
                          {"type":"DISADVANTAGE","target":"DEXTERITY_SAVING_THROWS","value":0}]}""";
    private static final String INVISIBLE = """
            {"modifiers":[{"type":"ADVANTAGE","target":"ATTACK_ROLLS","value":0}]}""";
    private static final String EXHAUSTION = """
            {"modifiers":[],"levels":{
              "1":[{"type":"DISADVANTAGE","target":"ABILITY_CHECKS","value":0}],
              "2":[{"type":"HALVE","target":"SPEED","value":0}],
              "3":[{"type":"DISADVANTAGE","target":"ATTACK_ROLLS","value":0},{"type":"DISADVANTAGE","target":"SAVING_THROWS","value":0}],
              "4":[{"type":"HALVE","target":"HIT_POINT_MAXIMUM","value":0}],
              "5":[{"type":"SET","target":"SPEED","value":0}],"6":[]}}""";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(objectMapper);
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void aConditionTurnedOnForcesItsRollModesWithItsNameAsTheSource() {
        VitalsZone vitals = calculator.calculateVitals(mutator.toggleCondition(sheet(), "poisoned", POISONED));

        assertThat(vitals.rollModes().get("ATTACK").disadvantageSources()).containsExactly("Poisoned");
        assertThat(vitals.rollModes().get("SKILL_CHECK:stealth").mode()).isEqualTo("DISADVANTAGE");
        assertThat(vitals.rollModes().get("INITIATIVE").mode()).isEqualTo("DISADVANTAGE");
        assertThat(vitals.rollModes()).doesNotContainKey("SAVING_THROW:dexterity");
    }

    @Test
    void turningTheConditionOffRemovesItsEffects() {
        String on = mutator.toggleCondition(sheet(), "poisoned", POISONED);

        VitalsZone vitals = calculator.calculateVitals(mutator.toggleCondition(on, "poisoned", POISONED));

        assertThat(vitals.rollModes()).isEmpty();
        assertThat(vitals.activeConditions()).isEmpty();
    }

    @Test
    void advantageAndDisadvantageFromDifferentSourcesCancel() {
        String both = mutator.toggleCondition(mutator.toggleCondition(sheet(), "poisoned", POISONED), "invisible", INVISIBLE);

        RollModeInfo attack = calculator.calculateVitals(both).rollModes().get("ATTACK");

        assertThat(attack.mode()).isEqualTo("NORMAL");
        assertThat(attack.advantageSources()).containsExactly("Invisible");
        assertThat(attack.disadvantageSources()).containsExactly("Poisoned");
    }

    @Test
    void restrainedStopsMovementAndHindersDexteritySavesOnly() {
        VitalsZone vitals = calculator.calculateVitals(mutator.toggleCondition(sheet(), "restrained", RESTRAINED));

        assertThat(vitals.speed()).isZero();
        assertThat(vitals.rollModes().get("SAVING_THROW:dexterity").mode()).isEqualTo("DISADVANTAGE");
        assertThat(vitals.rollModes()).doesNotContainKey("SAVING_THROW:strength");
    }

    @Test
    void exhaustionEffectsAccumulateByLevel() {
        VitalsZone levelTwo = calculator.calculateVitals(mutator.setExhaustionLevel(sheet(), 2, EXHAUSTION));
        VitalsZone levelFour = calculator.calculateVitals(mutator.setExhaustionLevel(sheet(), 4, EXHAUSTION));
        VitalsZone cleared = calculator.calculateVitals(mutator.setExhaustionLevel(mutator.setExhaustionLevel(sheet(), 4, EXHAUSTION), 0, EXHAUSTION));

        assertThat(levelTwo.speed()).isEqualTo(15);
        assertThat(levelTwo.rollModes()).doesNotContainKey("ATTACK");
        assertThat(levelFour.rollModes().get("SAVING_THROW:wisdom").disadvantageSources()).containsExactly("Exhaustion 3");
        assertThat(levelFour.hitPoints().value()).isEqualTo(44 / 2);
        assertThat(levelFour.hitPoints().contributions()).extracting(Contribution::source).contains("Exhaustion 4 (halved)");
        assertThat(cleared.speed()).isEqualTo(30);
        assertThat(cleared.hitPoints().value()).isEqualTo(44);
    }

    /** Level 5, d10, Con 14 (+2): 10 + 2 + 4 × (6 + 2) = 44 hit points; speed 30. */
    private String sheet() {
        Dnd5eSheet sheet = new Dnd5eSheet(
                10, 14, 14, 10, 10, 10, 5, 10, 30, Set.of(), Set.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 44, 0, 0, false, List.of(), List.of(),
                List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(), background(), List.of(), 0,
                List.of(), List.of(), List.of(), false, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private static Dnd5eBackground background() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

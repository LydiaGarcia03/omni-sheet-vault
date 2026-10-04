package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.InvalidHitDiceRecoveryException;
import dev.omnisheetvault.api.ruleset.RollKind;
import dev.omnisheetvault.api.ruleset.UnresolvableRollException;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** C2b: hit dice per die size — Vex's shape, Rogue 4 (d8) and Sorcerer 3 (d6). */
class Dnd5eHitDicePoolsTest {

    private static final List<String> ROGUE = List.of("Rogue");
    private static final List<String> SORCERER = List.of("Sorcerer");

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(objectMapper);
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void eachDieSizeIsItsOwnPoolLargestFirst() {
        HitDice hitDice = calculator.calculateVitals(vex(0, 0, 0)).hitDice();

        assertThat(hitDice.pools()).containsExactly(new HitDice.Pool(8, 4, 0, ROGUE), new HitDice.Pool(6, 3, 0, SORCERER));
        assertThat(hitDice.dieSize()).isEqualTo(8);
        assertThat(hitDice.max()).isEqualTo(7);
    }

    @Test
    void spendingDiceOfOneSizeLeavesTheOtherAlone() {
        HitDice hitDice = calculator.calculateVitals(mutator.spendHitDice(vex(0, 0, 0), 6, 2, 0)).hitDice();

        assertThat(hitDice.pool(6)).hasValue(new HitDice.Pool(6, 3, 2, SORCERER));
        assertThat(hitDice.pool(8)).hasValue(new HitDice.Pool(8, 4, 0, ROGUE));
        assertThat(hitDice.used()).isEqualTo(2);
    }

    @Test
    void spendingWithoutASizeUsesTheLargestWithDiceLeft() {
        String allD8Spent = vex(4, 0, 4);

        HitDice hitDice = calculator.calculateVitals(mutator.spendHitDice(allD8Spent, 1, 0)).hitDice();

        assertThat(hitDice.pool(6)).hasValue(new HitDice.Pool(6, 3, 1, SORCERER));
    }

    @Test
    void aLongRestByDefaultRecoversHalfTheDiceLargestFirst() {
        HitDice hitDice = calculator.calculateVitals(mutator.applyLongRest(vex(2, 3, 5))).hitDice();

        assertThat(hitDice.pool(8)).hasValue(new HitDice.Pool(8, 4, 0, ROGUE));
        assertThat(hitDice.pool(6)).hasValue(new HitDice.Pool(6, 3, 2, SORCERER));
    }

    @Test
    void aLongRestRecoversTheDiceThePlayerChooses() {
        HitDice hitDice = calculator.calculateVitals(mutator.applyLongRest(vex(2, 3, 5), Map.of(6, 3))).hitDice();

        assertThat(hitDice.pool(8)).hasValue(new HitDice.Pool(8, 4, 2, ROGUE));
        assertThat(hitDice.pool(6)).hasValue(new HitDice.Pool(6, 3, 0, SORCERER));
    }

    @Test
    void aChoiceOverHalfTheDiceOrOverWhatWasSpentIsRefused() {
        assertThatThrownBy(() -> mutator.applyLongRest(vex(4, 3, 7), Map.of(8, 2, 6, 2)))
                .isInstanceOf(InvalidHitDiceRecoveryException.class)
                .hasMessageContaining("at most 3");
        assertThatThrownBy(() -> mutator.applyLongRest(vex(0, 1, 1), Map.of(8, 1)))
                .isInstanceOf(InvalidHitDiceRecoveryException.class);
    }

    @Test
    void aTotalRecordedBeforePerClassTrackingCountsAsTheLargestDice() {
        HitDice hitDice = calculator.calculateVitals(vex(0, 0, 5)).hitDice();

        assertThat(hitDice.pool(8)).hasValue(new HitDice.Pool(8, 4, 4, ROGUE));
        assertThat(hitDice.pool(6)).hasValue(new HitDice.Pool(6, 3, 1, SORCERER));
    }

    @Test
    void aSheetWithoutClassesKeepsOnePool() {
        HitDice hitDice = calculator.calculateVitals(sheet(null, 2)).hitDice();

        assertThat(hitDice.pools()).containsExactly(new HitDice.Pool(8, 7, 2));
    }

    @Test
    void theRollUsesTheRequestedDieSize() {
        VitalsZone vitals = calculator.calculateVitals(vex(0, 0, 0));
        Dnd5eMechanicResolver resolver = new Dnd5eMechanicResolver();

        assertThat(resolver.resolve(vitals, RollKind.HIT_DICE, "6", null).diceSides()).isEqualTo(6);
        assertThat(resolver.resolve(vitals, RollKind.HIT_DICE, null, null).diceSides()).isEqualTo(8);
        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.HIT_DICE, "10", null)).isInstanceOf(UnresolvableRollException.class);
    }

    private String vex(int roguesUsed, int sorcerersUsed, int total) {
        return sheet(List.of(
                new Dnd5eClassLevel("rogue", "Rogue", "rogue-thief", "Thief", 4, 8, roguesUsed),
                new Dnd5eClassLevel("sorcerer", "Sorcerer", "sorcerer-draconic", "Draconic Bloodline", 3, 6, sorcerersUsed)), total);
    }

    private String sheet(List<Dnd5eClassLevel> classLevels, int hitDiceUsed) {
        Dnd5eSheet sheet = new Dnd5eSheet(
                8, 18, 13, 10, 12, 16, 7, 8, 30, Set.of(), Set.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 1, 0, 0, false, List.of(), List.of(),
                List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), hitDiceUsed,
                List.of(), List.of(), List.of(), false, null, null, classLevels, classLevels == null ? null : 35, null, null, null,
                Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

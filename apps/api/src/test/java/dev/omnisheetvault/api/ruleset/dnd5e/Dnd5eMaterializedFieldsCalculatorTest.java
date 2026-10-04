package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** The calculator's use of the fields a materialized build writes: expertise, per-level hit dice, tremorsense. */
class Dnd5eMaterializedFieldsCalculatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void expertiseDoublesTheProficiencyBonusAndReportsTheExpertLevel() {
        VitalsZone vitals = calculate(sheet(Set.of("stealth"), null, 4));

        assertThat(vitals.skillProficiencies().get("stealth")).isEqualTo("EXPERT");
        assertThat(vitals.skillProficiencies().get("athletics")).isEqualTo("FULL");
        assertThat(vitals.skills().get("stealth").value()).isEqualTo(2 + 4);
        assertThat(vitals.skills().get("stealth").contributions()).extracting(Contribution::source)
                .contains("Proficiency bonus ×2 (Expertise)");
    }

    @Test
    void storedHitDiceReplaceTheSingleDieAverage() {
        // Rogue 4 / Sorcerer 3 at the average: 8 + 5 + 5 + 5 + 4 + 4 + 4 = 35, plus Con +1 per level.
        VitalsZone vitals = calculate(sheet(Set.of(), 35, 7));

        assertThat(vitals.hitPoints().value()).isEqualTo(35 + 7);
        assertThat(vitals.hitPoints().contributions()).extracting(Contribution::source)
                .containsExactly("Hit dice (levels 1-7)", "Constitution modifier (×7)");
    }

    @Test
    void aSheetWithoutStoredHitDiceKeepsTheSingleDieFormula() {
        VitalsZone vitals = calculate(sheet(Set.of(), null, 4));

        assertThat(vitals.hitPoints().value()).isEqualTo(8 + 1 + 3 * (5 + 1));
    }

    @Test
    void labelsTremorsense() {
        VitalsZone vitals = calculate(sheet(Set.of(), null, 1));

        assertThat(vitals.specialSenses().getFirst().label()).isEqualTo("Tremorsense 30 ft.");
    }

    private VitalsZone calculate(Dnd5eSheet sheet) {
        return calculator.calculateVitals(objectMapper.writeValueAsString(sheet));
    }

    /** Dex 14 and Con 12 (modifiers +2/+1), proficient in Athletics and Stealth, hit die d8. */
    private static Dnd5eSheet sheet(Set<String> expertise, Integer hitPointBase, int level) {
        return new Dnd5eSheet(
                10, 14, 12, 10, 10, 10, level, 8, 30, Set.of(), Set.of("athletics", "stealth"), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 1, 0, 0, false, List.of(), List.of(),
                List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0,
                List.of(), List.of(new Dnd5eSpecialSense(Dnd5eSenseType.TREMORSENSE, 30)), List.of(), false, null, expertise,
                null, hitPointBase, null, Dnd5eSheet.SCHEMA_VERSION);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.RollNote;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Stage C audit F5: situational modifiers (Dwarven Resilience) are notes, never applied. */
class Dnd5eRollNotesTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void aSituationalAdvantageIsNotedButForcesNoRollMode() {
        Dnd5eModifier resilience = new Dnd5eModifier(
                Dnd5eModifierType.ADVANTAGE, Dnd5eModifierTarget.SAVING_THROWS, 0, null, null, "Dwarven Resilience", "against poison");

        VitalsZone vitals = calculate(resilience);

        assertThat(vitals.rollModes()).isEmpty();
        assertThat(vitals.rollNotes()).containsExactly(new RollNote("ADVANTAGE", "SAVING_THROWS", "against poison", "Dwarven Resilience"));
    }

    @Test
    void aSituationalBonusIsNeverSummed() {
        Dnd5eModifier conditional = new Dnd5eModifier(
                Dnd5eModifierType.BONUS, Dnd5eModifierTarget.ARMOR_CLASS, 2, null, null, "Test", "against ranged attacks");

        assertThat(calculate(conditional).armorClass().value()).isEqualTo(10);
    }

    private VitalsZone calculate(Dnd5eModifier modifier) {
        Dnd5eFeatureTrait trait = new Dnd5eFeatureTrait("trait", modifier.source(), Dnd5eFeatureTraitCategory.SPECIES_TRAIT,
                "Dwarf", modifier.source(), "", null, 0, null, List.of(modifier));
        Dnd5eSheet sheet = new Dnd5eSheet(
                10, 10, 10, 10, 10, 10, 1, 8, 25, Set.of(), Set.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 8, 0, 0, false, List.of(), List.of(),
                List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(trait), emptyBackground(), List.of(), 0,
                List.of(), List.of(), List.of(), false, null, null, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return calculator.calculateVitals(objectMapper.writeValueAsString(sheet));
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

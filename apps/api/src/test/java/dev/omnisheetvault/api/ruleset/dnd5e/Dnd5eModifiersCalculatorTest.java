package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Feature modifiers (C3a) on armor class, hit points and attacks per action. */
class Dnd5eModifiersCalculatorTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    private static final Dnd5eModifier DEFENSE = modifier(Dnd5eModifierType.BONUS, Dnd5eModifierTarget.ARMORED_ARMOR_CLASS, 1, null, "Fighting Style: Defense");
    private static final Dnd5eModifier TOUGH = modifier(Dnd5eModifierType.BONUS, Dnd5eModifierTarget.HIT_POINTS_PER_LEVEL, 2, null, "Tough");
    private static final Dnd5eModifier RESILIENCE_HP =
            modifier(Dnd5eModifierType.BONUS, Dnd5eModifierTarget.HIT_POINTS_PER_LEVEL, 1, "sorcerer", "Draconic Resilience");
    private static final Dnd5eModifier RESILIENCE_AC =
            modifier(Dnd5eModifierType.SET_BASE, Dnd5eModifierTarget.UNARMORED_ARMOR_CLASS, 13, null, "Draconic Resilience");

    @Test
    void armoredBonusesApplyOnlyWhileWearingArmor() {
        Dnd5eSheet armored = sheet(List.of(trait(DEFENSE)), List.of(chainMail(true)), 20, null, List.of());
        Dnd5eSheet unarmored = sheet(List.of(trait(DEFENSE)), List.of(chainMail(false)), 20, null, List.of());

        assertThat(calculate(armored).armorClass().value()).isEqualTo(16 + 1);
        assertThat(calculate(armored).armorClass().contributions()).extracting(Contribution::source).contains("Fighting Style: Defense");
        assertThat(calculate(unarmored).armorClass().value()).isEqualTo(10 + 2);
    }

    @Test
    void theBestUnarmoredBaseReplacesTenWithoutArmor() {
        Dnd5eSheet sheet = sheet(List.of(trait(RESILIENCE_AC)), List.of(), 3, null, List.of());

        VitalsZone vitals = calculate(sheet);

        assertThat(vitals.armorClass().value()).isEqualTo(13 + 2);
        assertThat(vitals.armorClass().contributions()).extracting(Contribution::source)
                .containsExactly("Base (Draconic Resilience)", "Dexterity modifier");
    }

    @Test
    void hitPointsPerLevelCountCharacterOrClassLevels() {
        List<Dnd5eClassLevel> classLevels = List.of(
                new Dnd5eClassLevel("rogue", "Rogue", null, null, 4, 8),
                new Dnd5eClassLevel("sorcerer", "Sorcerer", null, null, 3, 6));
        Dnd5eSheet sheet = sheet(List.of(trait(TOUGH), trait(RESILIENCE_HP)), List.of(), 7, 35, classLevels);

        VitalsZone vitals = calculate(sheet);

        assertThat(vitals.hitPoints().value()).isEqualTo(35 + 7 + 2 * 7 + 3);
        assertThat(Dnd5eFormulas.maxHitPoints(sheet)).isEqualTo(vitals.hitPoints().value());
        assertThat(vitals.hitPoints().contributions()).extracting(Contribution::source)
                .contains("Tough (+2 × 7)", "Draconic Resilience (+1 × 3)");
    }

    @Test
    void attacksPerActionTakeTheHighestExtraAttack() {
        Dnd5eSheet sheet = sheet(List.of(
                trait(modifier(Dnd5eModifierType.SET, Dnd5eModifierTarget.EXTRA_ATTACKS, 1, null, "Extra Attack")),
                trait(modifier(Dnd5eModifierType.SET, Dnd5eModifierTarget.EXTRA_ATTACKS, 3, null, "Extra Attack (3)"))),
                List.of(), 20, null, List.of());

        assertThat(calculate(sheet).attacksPerAction()).isEqualTo(4);
        assertThat(calculate(sheet(List.of(), List.of(), 1, null, List.of())).attacksPerAction()).isEqualTo(1);
    }

    private VitalsZone calculate(Dnd5eSheet sheet) {
        return calculator.calculateVitals(objectMapper.writeValueAsString(sheet));
    }

    private static Dnd5eModifier modifier(Dnd5eModifierType type, Dnd5eModifierTarget target, int value, String classSlug, String source) {
        return new Dnd5eModifier(type, target, value, null, classSlug, source);
    }

    private static Dnd5eFeatureTrait trait(Dnd5eModifier modifier) {
        return new Dnd5eFeatureTrait("key:" + modifier.source(), modifier.source(), Dnd5eFeatureTraitCategory.CLASS_FEATURE,
                "Test", modifier.source(), "", null, 0, null, List.of(modifier));
    }

    private static Dnd5eItem chainMail(boolean equipped) {
        return new Dnd5eItem(
                "chain", "Chain Mail", 1, "", "", equipped, false, false, "chain-mail",
                Dnd5eItemKind.ARMOR, "Armor", "none", null, null, null, null, null,
                null, null, null, null, null, List.of(), false, null, null,
                Dnd5eArmorCategory.HEAVY, 16, false, null, null, null, null,
                null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
    }

    /** Dex 14 and Con 12 (modifiers +2/+1), hit die d8. */
    private static Dnd5eSheet sheet(List<Dnd5eFeatureTrait> traits, List<Dnd5eItem> items, int level, Integer hitPointBase,
            List<Dnd5eClassLevel> classLevels) {
        return new Dnd5eSheet(
                10, 14, 12, 10, 10, 10, level, 8, 30, Set.of(), Set.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 1, 0, 0, false, List.of(), List.of(),
                List.of(), List.of(), items, 0, 0, 0, 0, 0, traits, emptyBackground(), List.of(), 0,
                List.of(), List.of(), List.of(), false, null, Set.of(),
                classLevels.isEmpty() ? null : classLevels, hitPointBase, null, Dnd5eSheet.SCHEMA_VERSION);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

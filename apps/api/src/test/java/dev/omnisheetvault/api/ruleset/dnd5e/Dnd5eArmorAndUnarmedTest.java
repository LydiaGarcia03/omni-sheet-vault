package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.AttackRow;
import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Stage C audit F2 and F3: Unarmed Strike always listed; worn armor's Strength requirement and stealth disadvantage. */
class Dnd5eArmorAndUnarmedTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void everyCharacterHasAnUnarmedStrikeListedLast() {
        VitalsZone vitals = calculate(sheet(20, List.of(), List.of(longsword())));

        assertThat(vitals.attacks().keySet()).containsExactly("longsword", "unarmed-strike");
        AttackRow unarmed = vitals.attacks().get("unarmed-strike");
        assertThat(unarmed.toHit().value()).isEqualTo(5 + 3);
        assertThat(unarmed.damageDiceCount()).isZero();
        assertThat(unarmed.damageModifier()).isEqualTo(1 + 5);
        assertThat(unarmed.damageType()).isEqualTo("bludgeoning");
    }

    @Test
    void aHandAuthoredUnarmedStrikeIsNotDuplicated() {
        Dnd5eAttack authored = new Dnd5eAttack("fists", "Unarmed Strike", "5 ft.", "strength", 0, 1, "bludgeoning", "Unarmed Attack", "");

        VitalsZone vitals = calculate(sheet(20, List.of(authored), List.of()));

        assertThat(vitals.attacks().keySet()).containsExactly("fists");
    }

    @Test
    void heavyArmorBelowItsStrengthRequirementCostsTenFeet() {
        VitalsZone weak = calculate(sheet(12, List.of(), List.of(chainMail(true))));
        VitalsZone strong = calculate(sheet(13, List.of(), List.of(chainMail(true))));

        assertThat(weak.speed()).isEqualTo(20);
        assertThat(weak.provenance().speed().contributions()).contains(new Contribution("Chain Mail (Strength 13)", -10));
        assertThat(strong.speed()).isEqualTo(30);
    }

    @Test
    void armorWithStealthDisadvantageMarksOnlyStealth() {
        VitalsZone worn = calculate(sheet(20, List.of(), List.of(chainMail(true))));
        VitalsZone carried = calculate(sheet(20, List.of(), List.of(chainMail(false))));

        assertThat(worn.rollModes().get("SKILL_CHECK:stealth").disadvantageSources()).containsExactly("Chain Mail");
        assertThat(worn.rollModes()).doesNotContainKey("SKILL_CHECK:acrobatics");
        assertThat(carried.rollModes()).doesNotContainKey("SKILL_CHECK:stealth");
    }

    private VitalsZone calculate(Dnd5eSheet sheet) {
        return calculator.calculateVitals(objectMapper.writeValueAsString(sheet));
    }

    private static Dnd5eItem chainMail(boolean equipped) {
        return new Dnd5eItem("chain", "Chain Mail", 1, "", "", equipped, false, false, "chain-mail",
                Dnd5eItemKind.ARMOR, "Heavy Armor", "none", null, null, null, null, null,
                null, null, null, null, null, List.of(), false, null, null,
                Dnd5eArmorCategory.HEAVY, 16, true, 13, null, null, null,
                null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
    }

    private static Dnd5eItem longsword() {
        return new Dnd5eItem("longsword", "Longsword", 1, "", "", true, false, false, "longsword",
                Dnd5eItemKind.WEAPON, "Melee Weapon", "none", null, null, null, Dnd5eWeaponCategory.MARTIAL, Dnd5eRangeCategory.MELEE,
                1, 8, "slashing", 1, 10, List.of("V"), false, null, null,
                null, null, false, null, null, null, null,
                null, null, null, List.of(), 0, Dnd5eStorageLocation.EQUIPMENT, null);
    }

    /** Level 5 (+3), Dexterity 10, speed 30, proficient with martial weapons. */
    private static Dnd5eSheet sheet(int strength, List<Dnd5eAttack> attacks, List<Dnd5eItem> items) {
        return new Dnd5eSheet(
                strength, 10, 10, 10, 10, 10, 5, 10, 30, Set.of(), Set.of(), List.of("Heavy"), List.of("Martial"), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), Set.of(), 0, 1, 0, 0, false, attacks, List.of(),
                List.of(), List.of(), items, 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0,
                List.of(), List.of(), List.of(), false, null, null, null, null, null, null, null, Dnd5eSheet.SCHEMA_VERSION);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.AttackRow;
import dev.omnisheetvault.api.ruleset.Background;
import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Coins;
import dev.omnisheetvault.api.ruleset.Encumbrance;
import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.ResolvedRoll;
import dev.omnisheetvault.api.ruleset.RollKind;
import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.SpellcastingClassInfo;
import dev.omnisheetvault.api.ruleset.UnresolvableRollException;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class Dnd5eMechanicResolverTest {

    private final Dnd5eMechanicResolver resolver = new Dnd5eMechanicResolver();

    @Test
    void resolvesAnAbilityCheckAsD20PlusTheModifier() {
        VitalsZone vitals = vitalsWith(
                Map.of("strength", value(3)), Map.of(), Map.of(), null);

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.ABILITY_CHECK, "strength", null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(20);
        assertThat(roll.modifier()).isEqualTo(3);
        assertThat(roll.context()).isEqualTo("Strength: check");
    }

    @Test
    void resolvesASavingThrow() {
        VitalsZone vitals = vitalsWith(Map.of(), Map.of("wisdom", value(5)), Map.of(), null);

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SAVING_THROW, "wisdom", null);

        assertThat(roll.modifier()).isEqualTo(5);
        assertThat(roll.context()).isEqualTo("Wisdom: saving throw");
    }

    @Test
    void resolvesASkillCheckAndHumanizesTheCamelCaseKey() {
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of("sleightOfHand", value(2)), null);

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SKILL_CHECK, "sleightOfHand", null);

        assertThat(roll.modifier()).isEqualTo(2);
        assertThat(roll.context()).isEqualTo("Sleight Of Hand: check");
    }

    @Test
    void resolvesInitiativeWithNoKey() {
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), value(4));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.INITIATIVE, null, null);

        assertThat(roll.modifier()).isEqualTo(4);
        assertThat(roll.context()).isEqualTo("Initiative: roll");
    }

    @Test
    void resolvesAnAttackHitAsD20PlusToHit() {
        AttackRow longsword = new AttackRow("Longsword", "5 ft", value(6), 1, 8, 3, "slashing", "Melee Weapon", "", "ACTION");
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, Map.of("longsword", longsword));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.ATTACK_HIT, "longsword", null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(20);
        assertThat(roll.modifier()).isEqualTo(6);
        assertThat(roll.context()).isEqualTo("Longsword: attack roll");
    }

    @Test
    void resolvesAnAttackDamageAsItsOwnDiceExpression() {
        AttackRow longsword = new AttackRow("Longsword", "5 ft", value(6), 1, 8, 3, "slashing", "Melee Weapon", "", "ACTION");
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, Map.of("longsword", longsword));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.ATTACK_DAMAGE, "longsword", null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(8);
        assertThat(roll.modifier()).isEqualTo(3);
        assertThat(roll.context()).isEqualTo("Longsword: damage");
    }

    @Test
    void resolvesATwoHandedDamageWithTheVersatileDiceAndTheSameModifier() {
        AttackRow longsword = new AttackRow("Longsword", "5 ft", value(6), 1, 8, 3, "slashing", "Melee Weapon", "", "ACTION", 1, 10);
        AttackRow dagger = new AttackRow("Dagger", "5 ft", value(6), 1, 4, 3, "piercing", "Melee Weapon", "", "ACTION");
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, Map.of("longsword", longsword, "dagger", dagger));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.ATTACK_DAMAGE_VERSATILE, "longsword", null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(10);
        assertThat(roll.modifier()).isEqualTo(3);
        assertThat(roll.context()).isEqualTo("Longsword: two-handed damage");
        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.ATTACK_DAMAGE_VERSATILE, "dagger", null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    @Test
    void rejectsAnUnknownAttackKey() {
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, Map.of());

        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.ATTACK_HIT, "greataxe", null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    @Test
    void rejectsAnUnknownSkillKey() {
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null);

        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.SKILL_CHECK, "juggling", null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    @Test
    void rejectsAMissingKeyForAKindThatNeedsOne() {
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null);

        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.ABILITY_CHECK, null, null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    @Test
    void resolvesOneHitDieAsTheSheetsOwnDieSizePlusConstitution() {
        VitalsZone vitals = vitalsWith(Map.of("constitution", value(2)), Map.of(), Map.of(), null);

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.HIT_DICE, null, null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(10);
        assertThat(roll.modifier()).isEqualTo(2);
        assertThat(roll.context()).isEqualTo("Hit Die (d10): heal");
    }

    @Test
    void resolvesASpellAttackAsD20PlusTheSpellcastingClassesAttackBonus() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell fireBolt = new Spell(
                "fireBolt", "Fire Bolt", "Fighter", 0, "evocation", "1 Action", "120 feet", false, false, true, 1,
                10, "fire", "V, S", "Damage", false, false, "", null, "V, S", null, "Instantaneous", null, null, null);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(fireBolt));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_ATTACK, "fireBolt", null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(20);
        assertThat(roll.modifier()).isEqualTo(7);
        assertThat(roll.context()).isEqualTo("Fire Bolt: spell attack roll");
    }

    @Test
    void resolvesSpellDamageAsItsOwnDiceExpressionWithNoAbilityModifier() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell fireBolt = new Spell(
                "fireBolt", "Fire Bolt", "Fighter", 0, "evocation", "1 Action", "120 feet", false, false, true, 1,
                10, "fire", "V, S", "Damage", false, false, "", null, "V, S", null, "Instantaneous", null, null, null);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(fireBolt));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_DAMAGE, "fireBolt", null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(10);
        assertThat(roll.modifier()).isZero();
        assertThat(roll.context()).isEqualTo("Fire Bolt: damage");
    }

    @Test
    void rejectsASpellAttackWhenTheSpellCarriesNoAttackRollFlag() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell mageArmor = new Spell(
                "mageArmor", "Mage Armor", "Fighter", 1, "abjuration", "1 Action", "Touch", false, false, false,
                null, null, null, "V, S, M (a piece of cured leather)", "Buff", false, false, "", null, "V, S, M",
                "a piece of cured leather", "8 hours", null, null, null);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(mageArmor));

        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.SPELL_ATTACK, "mageArmor", null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    @Test
    void rejectsSpellDamageWhenTheSpellCarriesNoDamageDice() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell mageArmor = new Spell(
                "mageArmor", "Mage Armor", "Fighter", 1, "abjuration", "1 Action", "Touch", false, false, false,
                null, null, null, "V, S, M (a piece of cured leather)", "Buff", false, false, "", null, "V, S, M",
                "a piece of cured leather", "8 hours", null, null, null);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(mageArmor));

        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.SPELL_DAMAGE, "mageArmor", null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    @Test
    void resolvesSpellHealAsItsOwnDiceExpressionPlusTheSpellcastingModifier() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(3), value(7), value(11), "KNOWN", 2, 4, null);
        Spell cureWounds = new Spell(
                "cureWounds", "Cure Wounds", "Fighter", 1, "evocation", "1 Action", "Touch", false, false, false, 1,
                8, null, "V, S", "Healing", false, false, "", null, "V, S", null, "Instantaneous", null, null, null);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(cureWounds));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_HEAL, "cureWounds", null);

        assertThat(roll.diceCount()).isEqualTo(1);
        assertThat(roll.diceSides()).isEqualTo(8);
        assertThat(roll.modifier()).isEqualTo(3);
        assertThat(roll.context()).isEqualTo("Cure Wounds: healing");
    }

    @Test
    void scalesSpellDamageUpByTheChosenCastLevelAboveTheSpellsOwnBase() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        // Inflict Wounds' real shape: 1st level, 3d10 base, +1d10 per slot level above 1st.
        Spell inflictWounds = scalingSpell("inflictWounds", "Inflict Wounds", 1, 3, 10, 1, 10);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(inflictWounds));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_DAMAGE, "inflictWounds", 3);

        assertThat(roll.diceCount()).isEqualTo(5);
        assertThat(roll.diceSides()).isEqualTo(10);
    }

    @Test
    void doesNotScaleSpellDamageWhenCastAtTheSpellsOwnBaseLevel() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell inflictWounds = scalingSpell("inflictWounds", "Inflict Wounds", 1, 3, 10, 1, 10);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(inflictWounds));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_DAMAGE, "inflictWounds", 1);

        assertThat(roll.diceCount()).isEqualTo(3);
    }

    @Test
    void doesNotScaleSpellDamageWhenNoCastLevelIsGiven() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell inflictWounds = scalingSpell("inflictWounds", "Inflict Wounds", 1, 3, 10, 1, 10);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(inflictWounds));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_DAMAGE, "inflictWounds", null);

        assertThat(roll.diceCount()).isEqualTo(3);
    }

    @Test
    void scalesSpellHealUpByTheChosenCastLevelAboveTheSpellsOwnBase() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(3), value(7), value(11), "KNOWN", 2, 4, null);
        // Cure Wounds' real shape: 1st level, 1d8 base, +1d8 per slot level above 1st.
        Spell cureWounds = scalingSpell("cureWounds", "Cure Wounds", 1, 1, 8, 1, 8);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(cureWounds));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_HEAL, "cureWounds", 3);

        assertThat(roll.diceCount()).isEqualTo(3);
        assertThat(roll.diceSides()).isEqualTo(8);
        assertThat(roll.modifier()).isEqualTo(3);
    }

    @Test
    void doesNotScaleWhenTheScalingDieSidesDontMatchTheBaseDie() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell oddSpell = scalingSpell("oddSpell", "Odd Spell", 1, 3, 10, 1, 6);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(oddSpell));

        ResolvedRoll roll = resolver.resolve(vitals, RollKind.SPELL_DAMAGE, "oddSpell", 3);

        assertThat(roll.diceCount()).isEqualTo(3);
        assertThat(roll.diceSides()).isEqualTo(10);
    }

    private Spell scalingSpell(
            String key, String name, int level, int damageDiceCount, int damageDiceSides,
            int higherLevelsDamageDiceCount, int higherLevelsDamageDiceSides) {
        return new Spell(
                key, name, "Fighter", level, "evocation", "1 Action", "Touch", false, false, false, damageDiceCount,
                damageDiceSides, null, "V, S", "Damage", false, false, "", null, "V, S", null, "Instantaneous", null,
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides);
    }

    @Test
    void rejectsSpellHealWhenTheSpellCarriesNoHealingDice() {
        SpellcastingClassInfo fighter = new SpellcastingClassInfo("Fighter", "intelligence", value(0), value(7), value(11), "KNOWN", 2, 4, null);
        Spell mageArmor = new Spell(
                "mageArmor", "Mage Armor", "Fighter", 1, "abjuration", "1 Action", "Touch", false, false, false,
                null, null, null, "V, S, M (a piece of cured leather)", "Buff", false, false, "", null, "V, S, M",
                "a piece of cured leather", "8 hours", null, null, null);
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(fighter), List.of(mageArmor));

        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.SPELL_HEAL, "mageArmor", null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    @Test
    void rejectsAnUnknownSpellKey() {
        VitalsZone vitals = vitalsWith(Map.of(), Map.of(), Map.of(), null, List.of(), List.of());

        assertThatThrownBy(() -> resolver.resolve(vitals, RollKind.SPELL_DAMAGE, "wishfulThinking", null))
                .isInstanceOf(UnresolvableRollException.class);
    }

    private CalculatedValue value(int amount) {
        return new CalculatedValue(amount, List.of());
    }

    private VitalsZone vitalsWith(
            Map<String, CalculatedValue> abilityModifiers,
            Map<String, CalculatedValue> savingThrows,
            Map<String, CalculatedValue> skills,
            CalculatedValue initiative) {
        return vitalsWith(abilityModifiers, savingThrows, skills, initiative, Map.of());
    }

    private VitalsZone vitalsWith(
            Map<String, CalculatedValue> abilityModifiers,
            Map<String, CalculatedValue> savingThrows,
            Map<String, CalculatedValue> skills,
            CalculatedValue initiative,
            Map<String, AttackRow> attacks) {
        return vitalsWith(abilityModifiers, savingThrows, skills, initiative, attacks, List.of(), List.of());
    }

    private VitalsZone vitalsWith(
            Map<String, CalculatedValue> abilityModifiers,
            Map<String, CalculatedValue> savingThrows,
            Map<String, CalculatedValue> skills,
            CalculatedValue initiative,
            List<SpellcastingClassInfo> spellcasting,
            List<Spell> spells) {
        return vitalsWith(abilityModifiers, savingThrows, skills, initiative, Map.of(), spellcasting, spells);
    }

    private VitalsZone vitalsWith(
            Map<String, CalculatedValue> abilityModifiers,
            Map<String, CalculatedValue> savingThrows,
            Map<String, CalculatedValue> skills,
            CalculatedValue initiative,
            Map<String, AttackRow> attacks,
            List<SpellcastingClassInfo> spellcasting,
            List<Spell> spells) {
        return new VitalsZone(
                Map.of(),
                abilityModifiers,
                value(0),
                value(0),
                initiative != null ? initiative : value(0),
                value(0),
                30,
                1,
                savingThrows,
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                skills,
                Map.of(),
                Map.of(),
                List.of(),
                List.of(),
                List.of(),
                List.of(),
                Set.of(),
                0,
                0,
                0,
                0,
                false,
                attacks,
                List.of(),
                spellcasting,
                spells,
                List.of(),
                new Coins(0, 0, 0, 0, 0),
                List.of(),
                emptyBackground(),
                List.of(),
                new HitDice(10, 5, 0),
                List.of(),
                List.of(),
                List.of(),
                new Encumbrance(false, 0, 0, false, List.of()));
    }

    private Background emptyBackground() {
        return new Background("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

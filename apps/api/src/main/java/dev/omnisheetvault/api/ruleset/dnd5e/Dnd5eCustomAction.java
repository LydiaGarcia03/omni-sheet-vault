package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A player-authored custom action — punch list item 7
 * (systems/dnd-5e/references/sheet-fidelity-audit.md), distinct from {@code featureActions}
 * (catalog/class-granted). One record for all three {@link Dnd5eCustomActionTemplate}
 * shapes rather than three separate types, since General/Spell/Weapon differ only in
 * which extra fields are populated, not in shape — confirmed live, all three share
 * one core field set. Every field beyond {@code key}/{@code template}/{@code name}/
 * {@code snippet}/{@code description} is nullable: almost
 * every field on D&D Beyond's own form is optional, and a stored action simply omits
 * whichever it never set (see the audit doc's "Custom Action 1" example, which sets
 * only to-hit/damage/damage type/stat).
 *
 * <p>{@code stat} and {@code saveType} are ability-key strings, same free-text
 * treatment as {@link Dnd5eAttack#abilityModifierKey}. To-hit is the stat's
 * modifier plus the proficiency bonus only when {@code proficient} is set (confirmed
 * live: an action with {@code proficient} unchecked shows a to-hit equal to its bare
 * stat modifier). Damage is {@code diceCount}d{@code dieType} plus the stat's
 * modifier automatically, plus {@code fixedValue} as an extra flat bonus on top
 * (confirmed live: a blank {@code fixedValue} still showed "+1" damage from the
 * stat modifier alone). {@code fixedSaveDc} is a flat override for a save-based
 * action's DC, not additive.
 *
 * <p>{@code displayAsAttack} folds this action into the sheet's attack table
 * exactly like a weapon row (confirmed live) — but only when it resolves to a real
 * to-hit roll ({@code Dnd5eSheetCalculator} requires {@code stat} to be set); a
 * save-based action ({@code saveType} set instead) has nowhere to go in
 * {@code Dnd5eAttack}/{@code AttackRow}'s shape, which has no DC slot, so it is a
 * deliberate limitation of this slice: it stays list-only, same as one with
 * {@code displayAsAttack} unset, rather than a guess at extending the shared
 * attack-row shape from one unconfirmed case. {@code snippet} was confirmed live
 * not to render anywhere on the sheet itself; it is still captured (D&D Beyond's own
 * form has the field), just not surfaced yet — a future consumer would need its own
 * decision on where, not a change to this shape. A Spell-template action was
 * confirmed live to never surface on the Spells tab, Actions-tab-only regardless of
 * template. Without an {@code activationType} (D&D Beyond's "--", what a new action
 * starts with) it shows nowhere on the Actions tab.
 */
public record Dnd5eCustomAction(
        @NotBlank String key,
        @NotNull Dnd5eCustomActionTemplate template,
        @NotBlank String name,
        @NotNull String snippet,
        @NotNull String description,
        Dnd5eRangeCategory rangeCategory,
        @Min(0) Integer rangeFeet,
        String stat,
        @Min(0) Integer diceCount,
        @Min(1) Integer dieType,
        Integer fixedValue,
        String damageType,
        String saveType,
        Integer fixedSaveDc,
        Dnd5eSpellRangeType spellRangeType,
        Dnd5eAreaOfEffectType aoeType,
        @Min(0) Integer aoeSize,
        Dnd5eActivationType activationType,
        @Min(0) Integer activationTime,
        boolean affectedByMartialArts,
        boolean proficient,
        boolean displayAsAttack,
        Dnd5eWeaponAttackType weaponAttackType,
        @Min(0) Integer longRange,
        boolean dualWield,
        boolean silvered) {
}

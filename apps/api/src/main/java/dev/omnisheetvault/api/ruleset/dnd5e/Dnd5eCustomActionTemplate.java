package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * Which of D&D Beyond's three "Add New Actions" templates a custom action was
 * created from — see systems/dnd-5e/references/sheet-fidelity-audit.md's punch list item 7.
 * All three share one core field set; {@code SPELL} additionally uses
 * {@code Dnd5eCustomAction.spellRangeType}, {@code WEAPON} additionally uses
 * {@code weaponAttackType}/{@code longRange}/{@code dualWield}/{@code silvered}.
 * {@code GENERAL} uses only the core fields.
 */
public enum Dnd5eCustomActionTemplate {
    GENERAL,
    SPELL,
    WEAPON
}

package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * The Weapon template's own extra Attack Type field — confirmed live against D&D
 * Beyond's own "Add New Actions" form (systems/dnd-5e/references/sheet-fidelity-audit.md's
 * punch list item 7). Null for {@code GENERAL}/{@code SPELL} templates, and for a
 * Weapon-template action left at D&D Beyond's own unset {@code --}.
 */
public enum Dnd5eWeaponAttackType {
    NATURAL,
    UNARMED_STRIKE
}

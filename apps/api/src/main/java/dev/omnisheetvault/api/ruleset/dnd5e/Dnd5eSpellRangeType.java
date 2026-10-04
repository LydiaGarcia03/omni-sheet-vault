package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * The Spell template's own extra range field, alongside {@link Dnd5eRangeCategory}'s
 * core Melee/Ranged toggle — confirmed live against D&D Beyond's own "Add New
 * Actions" form (systems/dnd-5e/references/sheet-fidelity-audit.md's punch list item 7).
 * {@code SELF} is the one value the core toggle has no room for, e.g. a
 * self-targeted spell with no attack roll or save. Null for {@code GENERAL}/
 * {@code WEAPON} templates, which don't have this field at all.
 */
public enum Dnd5eSpellRangeType {
    MELEE,
    RANGED,
    SELF
}

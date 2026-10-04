package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * A custom action's core Melee/Ranged toggle, confirmed live against D&D Beyond's
 * own "Add New Actions" form — systems/dnd-5e/references/sheet-fidelity-audit.md's punch
 * list item 7. Distinct from {@link Dnd5eSpellRangeType}, the Spell template's own
 * extra Ranged/Melee/Self field.
 */
public enum Dnd5eRangeCategory {
    MELEE,
    RANGED
}

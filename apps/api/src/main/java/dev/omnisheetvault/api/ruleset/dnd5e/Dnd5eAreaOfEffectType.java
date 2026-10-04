package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * A custom action's AoE Type, confirmed live against D&D Beyond's own "Add New
 * Actions" form — systems/dnd-5e/references/sheet-fidelity-audit.md's punch list item 7.
 * Null when the action has no area of effect (D&D Beyond's own unset {@code --}).
 */
public enum Dnd5eAreaOfEffectType {
    CONE,
    CUBE,
    CYLINDER,
    LINE,
    SPHERE,
    SQUARE,
    SQUARE_FEET,
    EMANATION
}

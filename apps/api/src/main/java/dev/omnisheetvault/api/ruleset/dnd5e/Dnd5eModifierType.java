package dev.omnisheetvault.api.ruleset.dnd5e;

/** How a modifier combines with others on the same target; names follow D&D Beyond's {@code ModifierTypeEnum}. */
public enum Dnd5eModifierType {
    /** Adds its value. */
    BONUS,
    /** Sets the value; among several, the highest wins. */
    SET,
    /** A base value competing with armor's; among several, the highest wins. */
    SET_BASE,
    /** Halves the value, rounded down (exhaustion's speed and hit point maximum). */
    HALVE,
    /** Rolls on the target are made with advantage. */
    ADVANTAGE,
    /** Rolls on the target are made with disadvantage. */
    DISADVANTAGE
}

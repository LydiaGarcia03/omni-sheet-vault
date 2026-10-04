package dev.omnisheetvault.api.dice;

/**
 * The player's roll-time choice for a single-die roll — see ground-rules.md's Dice
 * section. Advantage and disadvantage marked together cancel out to {@link #NORMAL},
 * matching the tabletop rule that they never stack.
 */
enum RollMode {
    NORMAL,
    ADVANTAGE,
    DISADVANTAGE;

    static RollMode from(boolean advantage, boolean disadvantage) {
        if (advantage == disadvantage) {
            return NORMAL;
        }
        return advantage ? ADVANTAGE : DISADVANTAGE;
    }

    String label() {
        return name().toLowerCase();
    }
}

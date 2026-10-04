package dev.omnisheetvault.api.dice;

import dev.omnisheetvault.api.ruleset.RollKind;

/**
 * Thrown when advantage or disadvantage is requested for a roll that isn't a single
 * die — only a single die can be rolled twice and one kept. See ground-rules.md's
 * Dice section.
 */
public class IneligibleRollModeException extends RuntimeException {

    public IneligibleRollModeException(RollKind kind) {
        super("Advantage/disadvantage only applies to a single-die roll, not " + kind);
    }
}

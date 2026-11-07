package dev.omnisheetvault.api.ruleset;

import dev.omnisheetvault.api.shared.LocalizedProblem;

/**
 * Thrown when spending more hit dice than are currently available — see
 * features/character-sheet.md's "Rest" mutation and roadmap.md phase 9.
 */
public class InsufficientHitDiceException extends RuntimeException implements LocalizedProblem {

    private final int requested;
    private final int available;

    public InsufficientHitDiceException(int requested, int available) {
        super("Cannot spend " + requested + " hit dice: only " + available + " available");
        this.requested = requested;
        this.available = available;
    }

    @Override
    public String messageCode() {
        return "error.insufficientHitDice";
    }

    @Override
    public Object[] messageArguments() {
        return new Object[] {requested, available};
    }
}

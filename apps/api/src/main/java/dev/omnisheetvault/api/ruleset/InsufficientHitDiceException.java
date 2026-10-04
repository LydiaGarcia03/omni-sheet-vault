package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when spending more hit dice than are currently available — see
 * features/character-sheet.md's "Rest" mutation and roadmap.md phase 9.
 */
public class InsufficientHitDiceException extends RuntimeException {

    public InsufficientHitDiceException(int requested, int available) {
        super("Cannot spend " + requested + " hit dice: only " + available + " available");
    }
}

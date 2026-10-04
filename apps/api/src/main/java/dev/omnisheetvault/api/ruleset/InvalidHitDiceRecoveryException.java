package dev.omnisheetvault.api.ruleset;

/** Thrown when a long rest's chosen hit dice recover more than the rest allows, or dice that were never spent. */
public class InvalidHitDiceRecoveryException extends RuntimeException {

    public InvalidHitDiceRecoveryException(String message) {
        super(message);
    }
}

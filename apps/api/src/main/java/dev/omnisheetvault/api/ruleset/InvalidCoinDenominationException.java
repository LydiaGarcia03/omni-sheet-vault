package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when a coin edit names a denomination outside the system's own set. Lives
 * here, not in a system subpackage, so {@code shared.ApiExceptionHandler} can map it
 * without importing across the system boundary — see architecture.md, same reasoning
 * as {@link InvalidConditionException}.
 */
public class InvalidCoinDenominationException extends RuntimeException {

    public InvalidCoinDenominationException(String denomination) {
        super("Unknown coin denomination: \"" + denomination + "\"");
    }
}

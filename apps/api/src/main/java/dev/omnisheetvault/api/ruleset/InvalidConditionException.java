package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when a condition toggle names something outside the system's own condition
 * list. Lives here, not in a system subpackage, so {@code shared.ApiExceptionHandler}
 * can map it without importing across the system boundary — see architecture.md.
 */
public class InvalidConditionException extends RuntimeException {

    public InvalidConditionException(String condition) {
        super("Unknown condition: \"" + condition + "\"");
    }
}

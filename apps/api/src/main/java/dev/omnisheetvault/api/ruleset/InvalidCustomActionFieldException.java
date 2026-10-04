package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when a custom action's field names a value outside the system's own closed
 * set (e.g. an unknown template or activation type). Lives here, not in a system
 * subpackage, so {@code shared.ApiExceptionHandler} can map it without importing
 * across the system boundary — see architecture.md, and the same reasoning as
 * {@link InvalidConditionException}.
 */
public class InvalidCustomActionFieldException extends RuntimeException {

    public InvalidCustomActionFieldException(String field, String value) {
        super("Invalid custom action " + field + ": \"" + value + "\"");
    }
}

package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when {@code updateBackgroundField} is called with a field name that
 * doesn't match the system's own editable field set — same treatment as
 * {@link InvalidConditionException}.
 */
public class InvalidBackgroundFieldException extends RuntimeException {

    public InvalidBackgroundFieldException(String field) {
        super("Unknown background field: " + field);
    }
}

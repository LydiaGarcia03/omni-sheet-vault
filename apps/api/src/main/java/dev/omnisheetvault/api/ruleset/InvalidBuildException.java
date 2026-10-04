package dev.omnisheetvault.api.ruleset;

/** A build document that can't be read, or that violates its own validation constraints. */
public class InvalidBuildException extends RuntimeException {

    public InvalidBuildException(String message) {
        super(message);
    }

    public InvalidBuildException(String message, Throwable cause) {
        super(message, cause);
    }
}

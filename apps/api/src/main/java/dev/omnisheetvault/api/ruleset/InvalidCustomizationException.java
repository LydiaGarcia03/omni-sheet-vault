package dev.omnisheetvault.api.ruleset;

/** A customization the system doesn't offer, or a value outside what it accepts. */
public class InvalidCustomizationException extends RuntimeException {

    public InvalidCustomizationException(String message) {
        super(message);
    }
}

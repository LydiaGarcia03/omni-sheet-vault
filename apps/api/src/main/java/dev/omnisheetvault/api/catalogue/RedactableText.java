package dev.omnisheetvault.api.catalogue;

/**
 * The redactable text field type the API contract needs — see adr-0005. When
 * redaction is enabled, {@code value} is never populated: redaction is enforced
 * server-side, not by sending prose the client is expected to hide.
 */
public record RedactableText(String value, boolean redacted) {

    static RedactableText of(String value, boolean redactionEnabled) {
        return redactionEnabled ? new RedactableText(null, true) : new RedactableText(value, false);
    }
}

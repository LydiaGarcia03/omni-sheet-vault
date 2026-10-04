package dev.omnisheetvault.api.ruleset;

/**
 * Thrown when a catalogue-sourced item's closed-set field (e.g. {@code itemKind},
 * {@code weaponCategory}) names a value outside the system's own set. Unlike
 * {@link InvalidCustomActionFieldException} (a player's own form input can genuinely
 * be wrong), this can only mean {@code ItemConverter} emitted, or {@code
 * CharacterSheetService} mapped, a value the system-specific mutator doesn't
 * recognize — a server-side data bug, not a client mistake, so it maps to 500, not 400.
 */
public class InvalidItemFieldException extends RuntimeException {

    public InvalidItemFieldException(String field, String value) {
        super("Invalid catalogue item " + field + ": \"" + value + "\"");
    }
}

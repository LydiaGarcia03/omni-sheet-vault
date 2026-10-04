package dev.omnisheetvault.api.ruleset;

import tools.jackson.databind.JsonNode;

/**
 * One selectable option of a {@link CreationChoice}: its key, display label, source book
 * and a short summary of what it grants. {@code data} optionally carries the system's own
 * machine-readable rules for the option (e.g. a point-buy cost table) so a UI can guide
 * the player without re-implementing them.
 */
public record CreationChoiceOption(
        String key,
        String label,
        String sourceBook,
        String summary,
        JsonNode data) {

    public CreationChoiceOption(String key, String label, String sourceBook, String summary) {
        this(key, label, sourceBook, summary, null);
    }
}

package dev.omnisheetvault.api.ruleset;

/**
 * A starting-equipment item a materialized build grants: a catalogue item slug, or a free-text custom name when
 * the item has no catalogue entry. {@code equipped} makes it start worn or wielded.
 */
public record StartingItem(String catalogueSlug, String customName, int quantity, String displayName, boolean equipped) {

    public StartingItem(String catalogueSlug, String customName, int quantity, String displayName) {
        this(catalogueSlug, customName, quantity, displayName, false);
    }
}

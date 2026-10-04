package dev.omnisheetvault.api.ruleset;

import tools.jackson.databind.JsonNode;

/**
 * A read-only view of one catalogue entry, as a {@link CatalogueLookup} returns it.
 * {@code playtest} marks an entry from playtest material (Unearthed Arcana);
 * {@code partner} names the partner brand its source belongs to, or is {@code null}.
 */
public record CatalogueRecord(
        String kind,
        String slug,
        String name,
        String sourceBook,
        Integer sourcePage,
        String description,
        JsonNode data,
        boolean playtest,
        String partner) {

    public CatalogueRecord(String kind, String slug, String name, String sourceBook, Integer sourcePage, String description, JsonNode data) {
        this(kind, slug, name, sourceBook, sourcePage, description, data, false, null);
    }
}

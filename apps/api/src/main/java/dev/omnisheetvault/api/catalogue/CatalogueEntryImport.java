package dev.omnisheetvault.api.catalogue;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import tools.jackson.databind.JsonNode;

/** The shape of one catalogue entry source file — see CatalogueImportService. */
record CatalogueEntryImport(
        @NotBlank String systemId,
        @NotNull CatalogueEntryKind kind,
        @NotBlank String slug,
        @NotBlank String name,
        String sourceBook,
        String sourceCode,
        Integer sourcePage,
        List<String> tags,
        String description,
        @NotNull JsonNode data) {

    /** A converted entry before ingestion names its source. */
    CatalogueEntryImport(String systemId, CatalogueEntryKind kind, String slug, String name, String sourceBook,
            Integer sourcePage, List<String> tags, String description, JsonNode data) {
        this(systemId, kind, slug, name, sourceBook, null, sourcePage, tags, description, data);
    }

    List<String> tagsOrEmpty() {
        return tags == null ? List.of() : tags;
    }

    CatalogueEntryImport withSlug(String newSlug) {
        return new CatalogueEntryImport(systemId, kind, newSlug, name, sourceBook, sourceCode, sourcePage, tags, description, data);
    }

    /** The source's full name and its 5etools code (e.g. "Player's Handbook", "PHB"). */
    CatalogueEntryImport withSource(String newSourceBook, String newSourceCode) {
        return new CatalogueEntryImport(systemId, kind, slug, name, newSourceBook, newSourceCode, sourcePage, tags, description, data);
    }
}

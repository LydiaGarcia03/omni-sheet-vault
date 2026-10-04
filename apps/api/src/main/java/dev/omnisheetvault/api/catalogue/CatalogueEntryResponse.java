package dev.omnisheetvault.api.catalogue;

import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Mechanics ({@code data}) are never redacted — only prose is, per adr-0005. {@code data}
 * is exposed as a raw tree rather than a typed record: the mechanical shape varies by
 * kind and system, and nothing consumes it yet (the UI that would is out of scope for
 * this phase).
 */
public record CatalogueEntryResponse(
        String id,
        String systemId,
        CatalogueEntryKind kind,
        String slug,
        String name,
        String sourceBook,
        Integer sourcePage,
        List<String> tags,
        RedactableText description,
        JsonNode data) {

    static CatalogueEntryResponse from(CatalogueEntry entry, boolean redactionEnabled, ObjectMapper objectMapper) {
        return new CatalogueEntryResponse(
                entry.id().toString(),
                entry.systemId(),
                entry.kind(),
                entry.slug(),
                entry.name(),
                entry.sourceBook(),
                entry.sourcePage(),
                entry.tags(),
                RedactableText.of(entry.description(), redactionEnabled),
                objectMapper.readTree(entry.data()));
    }
}

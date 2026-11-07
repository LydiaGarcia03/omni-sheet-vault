package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.shared.TranslationOverlay;
import java.util.List;
import java.util.Locale;
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
        return from(entry, redactionEnabled, objectMapper, Locale.ENGLISH);
    }

    /** As the player browses it: the entry's name, description and data texts in {@code locale} where translated. */
    static CatalogueEntryResponse from(CatalogueEntry entry, boolean redactionEnabled, ObjectMapper objectMapper, Locale locale) {
        JsonNode translation = objectMapper.readTree(entry.translations()).path(locale.toLanguageTag());
        JsonNode data = TranslationOverlay.overlay(objectMapper.readTree(entry.data()), translation.path("data"));
        return new CatalogueEntryResponse(
                entry.id().toString(),
                entry.systemId(),
                entry.kind(),
                entry.slug(),
                textOr(translation.path("name"), entry.name()),
                entry.sourceBook(),
                entry.sourcePage(),
                entry.tags(),
                RedactableText.of(textOr(translation.path("description"), entry.description()), redactionEnabled),
                data);
    }

    private static String textOr(JsonNode translated, String english) {
        return translated.isString() && !translated.asString().isBlank() ? translated.asString() : english;
    }
}

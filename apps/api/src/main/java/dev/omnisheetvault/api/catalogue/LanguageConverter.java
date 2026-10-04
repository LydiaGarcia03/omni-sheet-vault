package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools language into a {@code LANGUAGE} catalogue entry. {@code type}
 * is 5etools' own classification ({@code standard}, {@code exotic}, {@code rare},
 * {@code secret}, or {@code null} when a book doesn't classify it) — what an "any
 * standard language" grant picks from.
 */
final class LanguageConverter implements FiveEToolsConverter {

    private final ObjectMapper objectMapper;

    LanguageConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.LANGUAGE;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        List<JsonNode> languages = new ArrayList<>();
        JsonNode array = dataSource.readDataFile("languages.json").get("language");
        if (array != null) {
            array.forEach(languages::add);
        }
        return languages;
    }

    @Override
    public CatalogueEntryImport convert(JsonNode language) {
        String name = language.get("name").asString();
        ObjectNode data = objectMapper.createObjectNode();
        data.put("type", language.get("type") == null ? null : language.get("type").asString());
        data.put("script", language.get("script") == null ? null : TagMarkupStripper.strip(language.get("script").asString()));
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.LANGUAGE,
                FiveEToolsNaming.slug(name),
                name,
                language.get("source").asString(),
                language.get("page") == null || !language.get("page").isNumber() ? null : language.get("page").asInt(),
                List.of(),
                TagMarkupStripper.strip(FiveEToolsEntries.flatten(language.get("entries"))),
                data);
    }
}

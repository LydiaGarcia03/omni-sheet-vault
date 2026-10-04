package dev.omnisheetvault.api.catalogue;

import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools subclass, already enriched by {@link FiveEToolsClassData},
 * into a {@code SUBCLASS} catalogue entry. The slug is prefixed with the parent
 * class's slug because subclass names are only unique within their class.
 */
final class SubclassConverter implements FiveEToolsConverter {

    private final ObjectMapper objectMapper;
    private final FiveEToolsClassProgression progression;
    private final FiveEToolsMechanicsOverlay overlay;

    SubclassConverter(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        this.objectMapper = objectMapper;
        this.overlay = overlay;
        this.progression = new FiveEToolsClassProgression(objectMapper);
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.SUBCLASS;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        return FiveEToolsClassData.load(dataSource, objectMapper, overlay).subclasses();
    }

    @Override
    public CatalogueEntryImport convert(JsonNode subclass) {
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.SUBCLASS,
                slug(subclass),
                subclass.get("name").asString(),
                subclass.get("source").asString(),
                subclass.get("page") == null ? null : subclass.get("page").asInt(),
                List.of(),
                subclass.get(FiveEToolsClassData.INTRODUCTION_FIELD).asString(),
                data(subclass));
    }

    private ObjectNode data(JsonNode subclass) {
        ObjectNode data = objectMapper.createObjectNode();
        data.put("classSlug", subclass.get(FiveEToolsClassData.CLASS_SLUG_FIELD).asString());
        data.put("shortName", subclass.get("shortName").asString());
        JsonNode additionalSpells = subclass.get("additionalSpells");
        data.set("additionalSpells", additionalSpells == null ? objectMapper.nullNode() : additionalSpells.deepCopy());
        data.set("spellcasting", spellcasting(subclass));
        data.set("tableColumns", progression.tableColumns(subclass.get("subclassTableGroups")));
        data.set("optionalFeatureProgressions",
                progression.optionalFeatureProgressions(subclass.get("optionalfeatureProgression")));
        data.set("features", subclass.get(FiveEToolsClassData.FEATURES_FIELD).deepCopy());
        return data;
    }

    /** Null unless the subclass casts on its own (Eldritch Knight, Arcane Trickster). */
    private JsonNode spellcasting(JsonNode subclass) {
        return subclass.get("casterProgression") == null
                ? objectMapper.nullNode()
                : progression.spellcasting(subclass, subclass.get("subclassTableGroups"));
    }

    private static String slug(JsonNode subclass) {
        return subclass.get(FiveEToolsClassData.CLASS_SLUG_FIELD).asString() + "-"
                + FiveEToolsNaming.slug(subclass.get("shortName").asString());
    }
}

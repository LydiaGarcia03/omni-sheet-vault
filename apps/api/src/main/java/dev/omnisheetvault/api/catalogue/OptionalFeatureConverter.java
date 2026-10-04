package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools optional feature (Fighting Style, Eldritch Invocation, Maneuver,
 * Metamagic, Pact Boon, Infusion, Rune, …) into an {@code OPTIONAL_FEATURE} catalogue
 * entry. {@code featureTypes} holds 5etools' own codes, the same ones a class or
 * subclass lists in its {@code optionalFeatureProgressions}.
 */
final class OptionalFeatureConverter implements FiveEToolsConverter {

    private final ObjectMapper objectMapper;
    private final FiveEToolsProficiencies proficiencies;
    private final FiveEToolsClassProgression progression;
    private final FiveEToolsMechanicsOverlay overlay;

    OptionalFeatureConverter(ObjectMapper objectMapper) {
        this(objectMapper, FiveEToolsMechanicsOverlay.empty());
    }

    OptionalFeatureConverter(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        this.objectMapper = objectMapper;
        this.proficiencies = new FiveEToolsProficiencies(objectMapper);
        this.progression = new FiveEToolsClassProgression(objectMapper);
        this.overlay = overlay;
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.OPTIONAL_FEATURE;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        List<JsonNode> entries = new ArrayList<>();
        JsonNode array = dataSource.readDataFile("optionalfeatures.json").get("optionalfeature");
        if (array != null) {
            array.forEach(entries::add);
        }
        overlay.matchEntries(FiveEToolsMechanicsOverlay.OPTIONAL_FEATURE, entries);
        return entries;
    }

    @Override
    public CatalogueEntryImport convert(JsonNode feature) {
        String name = feature.get("name").asString();
        String prerequisite = FiveEToolsPrerequisites.text(feature.get("prerequisite"));
        String body = TagMarkupStripper.strip(FiveEToolsEntries.flatten(feature.get("entries")));
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.OPTIONAL_FEATURE,
                FiveEToolsNaming.slug(name),
                name,
                feature.get("source").asString(),
                feature.get("page") == null ? null : feature.get("page").asInt(),
                List.of(),
                prerequisite == null ? body : "Prerequisite: " + prerequisite + "\n\n" + body,
                data(feature, prerequisite));
    }

    private ObjectNode data(JsonNode feature, String prerequisite) {
        ObjectNode data = objectMapper.createObjectNode();
        data.set("featureTypes", feature.get("featureType").deepCopy());
        data.put("optional", feature.path("isClassFeatureVariant").asBoolean(false));
        data.put("prerequisiteText", prerequisite);
        data.set("prerequisites", copyOrNull(feature.get("prerequisite")));
        data.set("consumes", copyOrNull(feature.get("consumes")));
        data.set("additionalSpells", copyOrNull(feature.get("additionalSpells")));
        data.set("senses", senses(feature.get("senses")));
        data.set("skillAlternatives", proficiencies.skillAlternatives(feature.get("skillProficiencies")));
        data.set("optionalFeatureProgressions",
                progression.optionalFeatureProgressions(feature.get("optionalfeatureProgression")));
        JsonNode mechanics = overlay.findEntry(FiveEToolsMechanicsOverlay.OPTIONAL_FEATURE,
                feature.get("name").asString(), feature.get("source").asString());
        FiveEToolsMechanicsOverlay.copyMechanics(data, mechanics, objectMapper);
        return data;
    }

    /** {@code {"darkvision": 120}} becomes {@code [{"type":"darkvision","range":120}]}. */
    private JsonNode senses(JsonNode senses) {
        ArrayNode result = objectMapper.createArrayNode();
        if (senses == null) {
            return result;
        }
        for (JsonNode block : senses.isArray() ? senses : objectMapper.createArrayNode().add(senses)) {
            for (Map.Entry<String, JsonNode> sense : block.properties()) {
                ObjectNode mapped = result.addObject();
                mapped.put("type", sense.getKey());
                mapped.put("range", sense.getValue().asInt());
            }
        }
        return result;
    }

    private JsonNode copyOrNull(JsonNode node) {
        return node == null ? objectMapper.nullNode() : node.deepCopy();
    }
}

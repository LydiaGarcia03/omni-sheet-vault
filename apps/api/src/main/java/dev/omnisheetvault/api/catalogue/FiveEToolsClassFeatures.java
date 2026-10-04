package dev.omnisheetvault.api.catalogue;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Indexes 5etools {@code classFeature}/{@code subclassFeature} entries and turns a
 * pipe-separated feature reference into this project's feature shape, expanding
 * nested {@code refClassFeature}/{@code refSubclassFeature} blocks into features of
 * their own.
 */
final class FiveEToolsClassFeatures {

    private static final String DEFAULT_SOURCE = "PHB";

    private final ObjectMapper objectMapper;
    private final FiveEToolsSourceNames sourceNames;
    private final FiveEToolsMechanicsOverlay overlay;
    private final Map<String, JsonNode> classFeatures = new HashMap<>();
    private final Map<String, JsonNode> subclassFeatures = new HashMap<>();

    FiveEToolsClassFeatures(
            ObjectMapper objectMapper, FiveEToolsSourceNames sourceNames, FiveEToolsMechanicsOverlay overlay, List<JsonNode> classFiles) {
        this.objectMapper = objectMapper;
        this.sourceNames = sourceNames;
        this.overlay = overlay;
        for (JsonNode file : classFiles) {
            index(file.get("classFeature"), classFeatures, FiveEToolsClassFeatures::classFeatureKey);
            index(file.get("subclassFeature"), subclassFeatures, FiveEToolsClassFeatures::subclassFeatureKey);
        }
    }

    /** Resolves a class's {@code classFeatures} list, in order. */
    ArrayNode resolveClassFeatures(JsonNode classFeatureRefs) {
        ArrayNode features = objectMapper.createArrayNode();
        for (JsonNode ref : classFeatureRefs) {
            boolean grantsSubclassFeature = ref.isObject() && ref.path("gainSubclassFeature").asBoolean(false);
            String key = ref.isObject() ? ref.get("classFeature").asString() : ref.asString();
            appendResolved(features, lookupClassFeature(key), grantsSubclassFeature);
        }
        return features;
    }

    /**
     * Resolves a subclass's {@code subclassFeatures} list. A top-level feature named
     * after the subclass itself is its introduction: its prose goes to
     * {@code introduction}, and only its nested references become features.
     */
    SubclassFeatures resolveSubclassFeatures(JsonNode subclassFeatureRefs, String subclassName) {
        ArrayNode features = objectMapper.createArrayNode();
        String introduction = "";
        for (JsonNode ref : subclassFeatureRefs) {
            JsonNode feature = lookupSubclassFeature(ref.asString());
            if (introduction.isEmpty() && subclassName.equals(feature.get("name").asString())) {
                introduction = description(feature);
                appendNestedReferences(features, feature.get("entries"));
            } else {
                appendResolved(features, feature, false);
            }
        }
        return new SubclassFeatures(introduction, features);
    }

    record SubclassFeatures(String introduction, ArrayNode features) {
    }

    private void appendResolved(ArrayNode features, JsonNode feature, boolean grantsSubclassFeature) {
        features.add(toFeature(feature, grantsSubclassFeature));
        appendNestedReferences(features, feature.get("entries"));
    }

    private void appendNestedReferences(ArrayNode features, JsonNode entries) {
        if (entries == null) {
            return;
        }
        for (JsonNode entry : entries) {
            if (!entry.isObject()) {
                continue;
            }
            String type = entry.path("type").asString("");
            if ("refClassFeature".equals(type)) {
                appendResolved(features, lookupClassFeature(entry.get("classFeature").asString()), false);
            } else if ("refSubclassFeature".equals(type)) {
                appendResolved(features, lookupSubclassFeature(entry.get("subclassFeature").asString()), false);
            } else {
                appendNestedReferences(features, entry.get("entries"));
            }
        }
    }

    private ObjectNode toFeature(JsonNode feature, boolean grantsSubclassFeature) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("name", feature.get("name").asString());
        result.put("level", feature.get("level").asInt());
        result.put("sourceBook", sourceNames.nameOf(feature.get("source").asString()));
        if (feature.get("page") != null && feature.get("page").isNumber()) {
            result.put("sourcePage", feature.get("page").asInt());
        } else {
            result.putNull("sourcePage");
        }
        result.put("description", description(feature));
        result.put("optional", feature.path("isClassFeatureVariant").asBoolean(false));
        result.put("grantsSubclassFeature", grantsSubclassFeature);
        result.set("optionalFeatureOptions", optionalFeatureOptions(feature.get("entries")));
        attachOverlay(result, feature);
        return result;
    }

    /** The adr-0007 overlay's modifiers and choices for this feature, if any; a table-sourced choice takes its options from the feature's own table. */
    private void attachOverlay(ObjectNode result, JsonNode feature) {
        String classSlug = FiveEToolsNaming.slug(feature.get("className").asString());
        JsonNode subclassShortName = feature.get("subclassShortName");
        String subclassSlug = subclassShortName == null ? null : classSlug + "-" + FiveEToolsNaming.slug(subclassShortName.asString());
        JsonNode mechanics = overlay.find(classSlug, subclassSlug, feature.get("name").asString());
        ArrayNode choices = result.putArray("choices");
        FiveEToolsMechanicsOverlay.copyMechanics(result, mechanics, objectMapper);
        if (mechanics == null) {
            return;
        }
        for (JsonNode choice : mechanics.path("choices")) {
            ObjectNode resolved = (ObjectNode) choice.deepCopy();
            JsonNode fromTable = resolved.remove("optionsFromTable");
            if (fromTable != null) {
                resolved.set("options", tableOptions(feature, fromTable));
            }
            choices.add(resolved);
        }
    }

    private ArrayNode tableOptions(JsonNode feature, JsonNode columns) {
        JsonNode table = firstTable(feature.get("entries"));
        if (table == null) {
            throw new FiveEToolsIngestException("Mechanics overlay expects a table in feature: " + feature.get("name").asString());
        }
        ArrayNode options = objectMapper.createArrayNode();
        int labelColumn = columns.get("labelColumn").asInt();
        int summaryColumn = columns.path("summaryColumn").asInt(-1);
        for (JsonNode row : table.get("rows")) {
            String label = TagMarkupStripper.strip(row.get(labelColumn).asString());
            ObjectNode option = options.addObject();
            option.put("key", FiveEToolsNaming.slug(label));
            option.put("label", label);
            option.put("summary", summaryColumn < 0 ? null : TagMarkupStripper.strip(row.get(summaryColumn).asString()));
        }
        return options;
    }

    private static JsonNode firstTable(JsonNode entries) {
        if (entries == null) {
            return null;
        }
        for (JsonNode entry : entries) {
            if (entry.isObject() && "table".equals(entry.path("type").asString(""))) {
                return entry;
            }
            JsonNode nested = entry.isObject() ? firstTable(entry.get("entries")) : null;
            if (nested != null) {
                return nested;
            }
        }
        return null;
    }

    private ArrayNode optionalFeatureOptions(JsonNode entries) {
        ArrayNode options = objectMapper.createArrayNode();
        collectOptionalFeatureRefs(entries, options);
        return options;
    }

    private void collectOptionalFeatureRefs(JsonNode entries, ArrayNode options) {
        if (entries == null) {
            return;
        }
        for (JsonNode entry : entries) {
            if (!entry.isObject()) {
                continue;
            }
            if ("refOptionalfeature".equals(entry.path("type").asString(""))) {
                String[] parts = entry.get("optionalfeature").asString().split("\\|", -1);
                ObjectNode option = objectMapper.createObjectNode();
                option.put("name", parts[0]);
                option.put("source", partOrDefault(parts, 1, DEFAULT_SOURCE));
                options.add(option);
            }
            collectOptionalFeatureRefs(entry.get("entries"), options);
        }
    }

    private static String description(JsonNode feature) {
        return TagMarkupStripper.strip(FiveEToolsEntries.flatten(feature.get("entries")));
    }

    private JsonNode lookupClassFeature(String reference) {
        String[] parts = reference.split("\\|", -1);
        String classSource = partOrDefault(parts, 2, DEFAULT_SOURCE);
        String key = key(parts[0], parts[1], classSource, parts[3], partOrDefault(parts, 4, classSource));
        return require(classFeatures, key, reference);
    }

    private JsonNode lookupSubclassFeature(String reference) {
        String[] parts = reference.split("\\|", -1);
        String classSource = partOrDefault(parts, 2, DEFAULT_SOURCE);
        String subclassSource = partOrDefault(parts, 4, DEFAULT_SOURCE);
        String key = key(parts[0], parts[1], classSource, parts[3], subclassSource, parts[5],
                partOrDefault(parts, 6, subclassSource));
        return require(subclassFeatures, key, reference);
    }

    private static JsonNode require(Map<String, JsonNode> index, String key, String reference) {
        JsonNode feature = index.get(key);
        if (feature == null) {
            throw new FiveEToolsIngestException("Unresolvable 5etools feature reference: " + reference);
        }
        return feature;
    }

    private interface KeyFunction {
        String keyOf(JsonNode feature);
    }

    private static void index(JsonNode features, Map<String, JsonNode> target, KeyFunction keyFunction) {
        if (features == null) {
            return;
        }
        for (JsonNode feature : features) {
            target.put(keyFunction.keyOf(feature), feature);
        }
    }

    private static String classFeatureKey(JsonNode feature) {
        return key(feature.get("name").asString(), feature.get("className").asString(),
                feature.path("classSource").asString(DEFAULT_SOURCE), String.valueOf(feature.get("level").asInt()),
                feature.get("source").asString());
    }

    private static String subclassFeatureKey(JsonNode feature) {
        return key(feature.get("name").asString(), feature.get("className").asString(),
                feature.path("classSource").asString(DEFAULT_SOURCE), feature.get("subclassShortName").asString(),
                feature.path("subclassSource").asString(DEFAULT_SOURCE), String.valueOf(feature.get("level").asInt()),
                feature.get("source").asString());
    }

    private static String key(String... parts) {
        return String.join("|", parts).toLowerCase(Locale.ROOT);
    }

    private static String partOrDefault(String[] parts, int index, String fallback) {
        return parts.length > index && !parts[index].isBlank() ? parts[index] : fallback;
    }
}

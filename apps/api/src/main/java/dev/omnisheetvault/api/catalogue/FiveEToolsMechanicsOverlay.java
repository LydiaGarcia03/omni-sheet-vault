package dev.omnisheetvault.api.catalogue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * The adr-0007 curated overlay: hand-encoded mechanics (modifiers and choices) that
 * 5etools only describes in prose, read from {@code content/<system>/mechanics/}.
 * Class and subclass features are targeted by class or subclass slug plus feature
 * name; feats, optional features, species, conditions and spells by name plus 5etools source code (a
 * species file holds a {@code traits} map by trait name). An overlay file that
 * matches no ingested entry of its kind fails ingestion.
 */
final class FiveEToolsMechanicsOverlay {

    static final String CLASS_FEATURE = "classSlug";
    static final String SUBCLASS_FEATURE = "subclassSlug";
    static final String FEAT = "feat";
    static final String OPTIONAL_FEATURE = "optionalFeature";
    static final String SPECIES = "species";
    static final String CONDITION = "condition";
    static final String SPELL = "spell";

    private final Map<String, JsonNode> byKey;
    private final Set<String> matchedKeys = new HashSet<>();

    private FiveEToolsMechanicsOverlay(Map<String, JsonNode> byKey) {
        this.byKey = byKey;
    }

    static FiveEToolsMechanicsOverlay empty() {
        return new FiveEToolsMechanicsOverlay(Map.of());
    }

    static FiveEToolsMechanicsOverlay load(Path mechanicsRoot, ObjectMapper objectMapper) {
        Map<String, JsonNode> byKey = new HashMap<>();
        readInto(byKey, mechanicsRoot.resolve("class-features"), objectMapper, CLASS_FEATURE, "feature");
        readInto(byKey, mechanicsRoot.resolve("subclass-features"), objectMapper, SUBCLASS_FEATURE, "feature");
        readInto(byKey, mechanicsRoot.resolve("feats"), objectMapper, FEAT, "source");
        readInto(byKey, mechanicsRoot.resolve("optional-features"), objectMapper, OPTIONAL_FEATURE, "source");
        readInto(byKey, mechanicsRoot.resolve("species"), objectMapper, SPECIES, "source");
        readInto(byKey, mechanicsRoot.resolve("conditions"), objectMapper, CONDITION, "source");
        readInto(byKey, mechanicsRoot.resolve("spells"), objectMapper, SPELL, "source");
        return new FiveEToolsMechanicsOverlay(byKey);
    }

    /** The overlay for a class feature ({@code subclassSlug == null}) or a subclass feature, or {@code null}. */
    JsonNode find(String classSlug, String subclassSlug, String featureName) {
        return subclassSlug == null ? lookup(CLASS_FEATURE, classSlug, featureName) : lookup(SUBCLASS_FEATURE, subclassSlug, featureName);
    }

    /** The overlay for a feat or optional feature, by its name and 5etools source code, or {@code null}. */
    JsonNode findEntry(String targetField, String name, String sourceCode) {
        return lookup(targetField, name, sourceCode);
    }

    /** Writes an overlay's {@code modifiers}, {@code uses}, {@code action} and {@code grants} (e.g. languages named only in prose) onto an entry's data (empty or null without an overlay). */
    static void copyMechanics(ObjectNode target, JsonNode mechanics, ObjectMapper objectMapper) {
        target.set("modifiers", mechanics == null ? objectMapper.createArrayNode() : mechanics.path("modifiers").deepCopy());
        target.set("uses", mechanics == null || !mechanics.hasNonNull("uses") ? objectMapper.nullNode() : mechanics.get("uses").deepCopy());
        target.set("action", mechanics == null || !mechanics.hasNonNull("action") ? objectMapper.nullNode() : mechanics.get("action").deepCopy());
        target.set("grants", mechanics == null || !mechanics.hasNonNull("grants") ? objectMapper.nullNode() : mechanics.get("grants").deepCopy());
    }

    /** Fails when an overlay file of the given kinds matched nothing; call once that kind's entries have all been looked up. */
    void requireAllMatched(String... targetFields) {
        Set<String> fields = new HashSet<>();
        for (String targetField : targetFields) {
            fields.add(targetField.toLowerCase(Locale.ROOT));
        }
        Set<String> unmatched = new HashSet<>();
        for (String key : byKey.keySet()) {
            if (!matchedKeys.contains(key) && fields.contains(key.substring(0, key.indexOf(':')))) {
                unmatched.add(key);
            }
        }
        if (!unmatched.isEmpty()) {
            throw new FiveEToolsIngestException("Mechanics overlay files match no ingested entry: " + unmatched);
        }
    }

    /** Looks every raw entry up by name and source, then checks that each overlay file of {@code targetField} matched one. */
    void matchEntries(String targetField, List<JsonNode> rawEntries) {
        for (JsonNode entry : rawEntries) {
            findEntry(targetField, entry.path("name").asString(), entry.path("source").asString());
        }
        requireAllMatched(targetField);
    }

    private JsonNode lookup(String targetField, String first, String second) {
        String key = key(targetField, first, second);
        JsonNode overlay = byKey.get(key);
        if (overlay != null) {
            matchedKeys.add(key);
        }
        return overlay;
    }

    private static void readInto(Map<String, JsonNode> byKey, Path directory, ObjectMapper objectMapper, String targetField,
            String secondField) {
        if (!Files.isDirectory(directory)) {
            return;
        }
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*.json")) {
            for (Path file : files) {
                JsonNode overlay = objectMapper.readTree(Files.readString(file));
                JsonNode target = overlay.get("target");
                if (target == null || target.get(targetField) == null || target.get(secondField) == null || overlay.get("citation") == null) {
                    throw new FiveEToolsIngestException(
                            "Mechanics overlay needs target." + targetField + ", target." + secondField + " and citation: " + file);
                }
                byKey.put(key(targetField, target.get(targetField).asString(), target.get(secondField).asString()), overlay);
            }
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not read mechanics overlay: " + directory, e);
        }
    }

    private static String key(String targetField, String first, String second) {
        return (targetField + ":" + first + "|" + second).toLowerCase(Locale.ROOT);
    }
}

package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * The subset of 5etools' {@code _copy}/{@code _mod}/{@code _versions} mechanics that real
 * data uses: an entry copied from another with its {@code entries} array modified
 * ({@code replaceArr}, {@code appendArr}, {@code prependArr}, {@code removeArr},
 * {@code replaceTxt}), and versions that are either a named {@code _mod} of their base
 * or an {@code _abstract} template filled per {@code _implementations} entry. Anything
 * outside that subset fails ingestion.
 */
final class FiveEToolsCopies {

    /** Fields that describe the copied entry itself, not what it grants — never inherited by a copy. */
    private static final Set<String> NOT_INHERITED = Set.of(
            "srd", "basicRules", "reprintedAs", "hasFluff", "hasFluffImages", "_versions", "page",
            "otherSources", "soundClip", "additionalSources");

    private static final Pattern TEMPLATE_VARIABLE = Pattern.compile("\\{\\{(\\w+)}}");
    private static final JsonNodeFactory NODES = JsonNodeFactory.instance;

    private FiveEToolsCopies() {
    }

    interface KeyFunction {
        String keyOf(JsonNode entry);
    }

    /**
     * Every entry of {@code entries} with its {@code _copy} resolved (recursively, a copy
     * may copy a copy), keyed by {@code keyOf}. {@code copyTargetKeyOf} computes the same
     * key from a {@code _copy} reference.
     */
    static Map<String, ObjectNode> resolveAll(JsonNode entries, KeyFunction keyOf, KeyFunction copyTargetKeyOf) {
        Map<String, JsonNode> raw = new HashMap<>();
        if (entries != null) {
            entries.forEach(entry -> raw.put(keyOf.keyOf(entry), entry));
        }
        Map<String, ObjectNode> resolved = new HashMap<>();
        raw.keySet().forEach(key -> resolve(key, raw, resolved, copyTargetKeyOf));
        return resolved;
    }

    private static ObjectNode resolve(
            String key, Map<String, JsonNode> raw, Map<String, ObjectNode> resolved, KeyFunction copyTargetKeyOf) {
        ObjectNode done = resolved.get(key);
        if (done != null) {
            return done;
        }
        JsonNode entry = raw.get(key);
        ObjectNode result;
        if (entry.get("_copy") == null) {
            result = (ObjectNode) entry.deepCopy();
        } else {
            String targetKey = copyTargetKeyOf.keyOf(entry.get("_copy"));
            if (!raw.containsKey(targetKey)) {
                throw new FiveEToolsIngestException("Unresolvable 5etools _copy target: " + entry.get("_copy"));
            }
            result = resolveCopy(entry, resolve(targetKey, raw, resolved, copyTargetKeyOf));
        }
        resolved.put(key, result);
        return result;
    }

    /** {@code entry} with everything it doesn't define taken from {@code target}, then its {@code _copy._mod} applied. */
    static ObjectNode resolveCopy(JsonNode entry, JsonNode target) {
        ObjectNode result = (ObjectNode) target.deepCopy();
        NOT_INHERITED.forEach(result::remove);
        for (Map.Entry<String, JsonNode> field : entry.properties()) {
            if (!"_copy".equals(field.getKey())) {
                result.set(field.getKey(), field.getValue().deepCopy());
            }
        }
        applyMods(result, entry.get("_copy").get("_mod"));
        return result;
    }

    /** One resolved node per version of {@code base}, each named after its version. */
    static List<ObjectNode> expandVersions(JsonNode base) {
        List<ObjectNode> versions = new ArrayList<>();
        JsonNode versionArray = base.get("_versions");
        if (versionArray == null) {
            return versions;
        }
        for (JsonNode version : versionArray) {
            JsonNode abstractVersion = version.get("_abstract");
            if (abstractVersion == null) {
                versions.add(applyVersion(base, version));
                continue;
            }
            for (JsonNode implementation : version.get("_implementations")) {
                JsonNode filled = fillTemplate(abstractVersion, implementation.get("_variables"));
                ObjectNode resolved = applyVersion(base, filled);
                for (Map.Entry<String, JsonNode> field : implementation.properties()) {
                    if (!"_variables".equals(field.getKey())) {
                        resolved.set(field.getKey(), field.getValue().deepCopy());
                    }
                }
                versions.add(resolved);
            }
        }
        return versions;
    }

    private static ObjectNode applyVersion(JsonNode base, JsonNode version) {
        ObjectNode resolved = (ObjectNode) base.deepCopy();
        resolved.remove("_versions");
        resolved.put("name", version.get("name").asString());
        if (version.get("source") != null) {
            resolved.put("source", version.get("source").asString());
        }
        applyMods(resolved, version.get("_mod"));
        return resolved;
    }

    static void applyMods(ObjectNode target, JsonNode mods) {
        if (mods == null) {
            return;
        }
        for (Map.Entry<String, JsonNode> modGroup : mods.properties()) {
            if (!"entries".equals(modGroup.getKey())) {
                throw new FiveEToolsIngestException("Unsupported 5etools _mod target: " + modGroup.getKey());
            }
            ArrayNode entries = target.get("entries") instanceof ArrayNode array ? array : target.putArray("entries");
            JsonNode operations = modGroup.getValue();
            for (JsonNode operation : operations.isArray() ? operations : NODES.arrayNode().add(operations)) {
                applyMod(entries, operation);
            }
        }
    }

    private static void applyMod(ArrayNode entries, JsonNode operation) {
        String mode = operation.get("mode").asString();
        switch (mode) {
            case "appendArr" -> items(operation).forEach(entries::add);
            case "prependArr" -> {
                List<JsonNode> items = items(operation);
                for (int i = items.size() - 1; i >= 0; i--) {
                    entries.insert(0, items.get(i));
                }
            }
            case "insertArr" -> {
                List<JsonNode> items = items(operation);
                int index = operation.get("index").asInt();
                for (int i = items.size() - 1; i >= 0; i--) {
                    entries.insert(index, items.get(i));
                }
            }
            case "replaceArr" -> replaceArr(entries, operation);
            case "removeArr" -> removeArr(entries, operation);
            case "replaceTxt" -> replaceText(entries, operation);
            default -> throw new FiveEToolsIngestException("Unsupported 5etools _mod mode: " + mode);
        }
    }

    /** {@code replace} selects the entry by name, or by position as {@code {"index": n}}. */
    private static void replaceArr(ArrayNode entries, JsonNode operation) {
        JsonNode selector = operation.get("replace");
        int index = selector.isObject() ? selector.get("index").asInt() : indexOfNamed(entries, selector.asString());
        if (index < 0 || index >= entries.size()) {
            throw new FiveEToolsIngestException("5etools replaceArr found no entry for " + selector);
        }
        entries.remove(index);
        List<JsonNode> items = items(operation);
        for (int i = items.size() - 1; i >= 0; i--) {
            entries.insert(index, items.get(i));
        }
    }

    private static void removeArr(ArrayNode entries, JsonNode operation) {
        JsonNode names = operation.get("names");
        for (JsonNode name : names.isArray() ? names : NODES.arrayNode().add(names)) {
            int index = indexOfNamed(entries, name.asString());
            if (index < 0) {
                throw new FiveEToolsIngestException("5etools removeArr found no entry named '" + name.asString() + "'");
            }
            entries.remove(index);
        }
    }

    /** Applies a regex replacement to every string inside {@code entries}, at any depth. */
    private static void replaceText(ArrayNode entries, JsonNode operation) {
        String flags = operation.path("flags").asString("");
        Pattern pattern = Pattern.compile(operation.get("replace").asString(), flags.contains("i") ? Pattern.CASE_INSENSITIVE : 0);
        String replacement = Matcher.quoteReplacement(operation.get("with").asString());
        replaceTextIn(entries, pattern, replacement);
    }

    private static void replaceTextIn(JsonNode node, Pattern pattern, String replacement) {
        if (node instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) {
                if (array.get(i).isString()) {
                    array.set(i, NODES.stringNode(pattern.matcher(array.get(i).asString()).replaceAll(replacement)));
                } else {
                    replaceTextIn(array.get(i), pattern, replacement);
                }
            }
        } else if (node instanceof ObjectNode object) {
            for (String key : new ArrayList<>(object.propertyNames())) {
                JsonNode value = object.get(key);
                if (value.isString() && !"type".equals(key)) {
                    object.put(key, pattern.matcher(value.asString()).replaceAll(replacement));
                } else {
                    replaceTextIn(value, pattern, replacement);
                }
            }
        }
    }

    private static int indexOfNamed(ArrayNode entries, String name) {
        for (int i = 0; i < entries.size(); i++) {
            JsonNode entry = entries.get(i);
            if (entry.isObject() && name.equals(entry.path("name").asString(null))) {
                return i;
            }
        }
        return -1;
    }

    private static List<JsonNode> items(JsonNode operation) {
        JsonNode items = operation.get("items");
        List<JsonNode> result = new ArrayList<>();
        for (JsonNode item : items.isArray() ? items : NODES.arrayNode().add(items)) {
            result.add(item.deepCopy());
        }
        return result;
    }

    /** A deep copy of {@code template} with every {@code {{name}}} replaced by that variable's value. */
    private static JsonNode fillTemplate(JsonNode template, JsonNode variables) {
        Set<String> missing = new HashSet<>();
        JsonNode filled = fill(template.deepCopy(), variables, missing);
        if (!missing.isEmpty()) {
            throw new FiveEToolsIngestException("5etools template variables with no value: " + missing);
        }
        return filled;
    }

    private static JsonNode fill(JsonNode node, JsonNode variables, Set<String> missing) {
        if (node.isString()) {
            return NODES.stringNode(substitute(node.asString(), variables, missing));
        }
        if (node instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) {
                array.set(i, fill(array.get(i), variables, missing));
            }
        } else if (node instanceof ObjectNode object) {
            for (String key : new ArrayList<>(object.propertyNames())) {
                object.set(key, fill(object.get(key), variables, missing));
            }
        }
        return node;
    }

    private static String substitute(String text, JsonNode variables, Set<String> missing) {
        Matcher matcher = TEMPLATE_VARIABLE.matcher(text);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            JsonNode value = variables.get(matcher.group(1));
            if (value == null || !value.isValueNode()) {
                missing.add(matcher.group(1));
                matcher.appendReplacement(result, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(result, Matcher.quoteReplacement(value.asString()));
            }
        }
        matcher.appendTail(result);
        return result.toString();
    }
}

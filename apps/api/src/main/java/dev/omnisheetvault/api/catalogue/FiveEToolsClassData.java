package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Loads every {@code class/class-*.json} file and returns the player classes and
 * subclasses of the 2014 ruleset, each enriched with its resolved features under
 * {@code _features}. Classes tagged {@code edition: "one"} are the 2024 ruleset, and
 * sidekick classes are not player classes; a subclass is kept only when its parent
 * class is.
 */
final class FiveEToolsClassData {

    static final String FEATURES_FIELD = "_features";
    static final String INTRODUCTION_FIELD = "_introduction";
    static final String CLASS_SLUG_FIELD = "_classSlug";

    private static final String DEFAULT_SOURCE = "PHB";
    private static final String REVISED_2024_EDITION = "one";

    private final List<JsonNode> classes = new ArrayList<>();
    private final List<JsonNode> subclasses = new ArrayList<>();

    private FiveEToolsClassData() {
    }

    static FiveEToolsClassData load(FiveEToolsDataSource dataSource, ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        List<JsonNode> files = dataSource.readDataFiles("class", "class-*.json");
        FiveEToolsClassFeatures features =
                new FiveEToolsClassFeatures(objectMapper, FiveEToolsSourceNames.load(dataSource), overlay, files);
        FiveEToolsClassData data = new FiveEToolsClassData();
        Set<String> keptClassKeys = new HashSet<>();
        for (JsonNode file : files) {
            data.addClasses(file.get("class"), features, keptClassKeys);
        }
        for (JsonNode file : files) {
            data.addSubclasses(file.get("subclass"), features, keptClassKeys);
        }
        overlay.requireAllMatched(FiveEToolsMechanicsOverlay.CLASS_FEATURE, FiveEToolsMechanicsOverlay.SUBCLASS_FEATURE);
        return data;
    }

    List<JsonNode> classes() {
        return classes;
    }

    List<JsonNode> subclasses() {
        return subclasses;
    }

    private void addClasses(JsonNode classArray, FiveEToolsClassFeatures features, Set<String> keptClassKeys) {
        if (classArray == null) {
            return;
        }
        for (JsonNode playerClass : classArray) {
            if (isRevised2024(playerClass) || playerClass.path("isSidekick").asBoolean(false)) {
                continue;
            }
            ObjectNode enriched = (ObjectNode) playerClass.deepCopy();
            enriched.set(FEATURES_FIELD, features.resolveClassFeatures(playerClass.get("classFeatures")));
            classes.add(enriched);
            keptClassKeys.add(classKey(playerClass.get("name").asString(), playerClass.get("source").asString()));
        }
    }

    private void addSubclasses(JsonNode subclassArray, FiveEToolsClassFeatures features, Set<String> keptClassKeys) {
        if (subclassArray == null) {
            return;
        }
        for (JsonNode subclass : subclassArray) {
            String className = subclass.get("className").asString();
            String classSource = subclass.path("classSource").asString(DEFAULT_SOURCE);
            if (isRevised2024(subclass) || !keptClassKeys.contains(classKey(className, classSource))) {
                continue;
            }
            FiveEToolsClassFeatures.SubclassFeatures resolved =
                    features.resolveSubclassFeatures(subclass.get("subclassFeatures"), subclass.get("name").asString());
            ObjectNode enriched = (ObjectNode) subclass.deepCopy();
            enriched.set(FEATURES_FIELD, resolved.features());
            enriched.put(INTRODUCTION_FIELD, resolved.introduction());
            enriched.put(CLASS_SLUG_FIELD, FiveEToolsNaming.slug(className));
            subclasses.add(enriched);
        }
    }

    private static boolean isRevised2024(JsonNode entry) {
        return REVISED_2024_EDITION.equals(entry.path("edition").asString(""));
    }

    private static String classKey(String name, String source) {
        return (name + "|" + source).toLowerCase(Locale.ROOT);
    }
}

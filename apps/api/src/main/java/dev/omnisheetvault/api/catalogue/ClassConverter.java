package dev.omnisheetvault.api.catalogue;

import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools player class, already enriched by {@link FiveEToolsClassData},
 * into a {@code CLASS} catalogue entry holding everything a build needs per level.
 */
final class ClassConverter implements FiveEToolsConverter {

    private static final String ABILITY_SCORE_IMPROVEMENT = "Ability Score Improvement";

    private final ObjectMapper objectMapper;
    private final FiveEToolsProficiencies proficiencies;
    private final FiveEToolsClassProgression progression;
    private final FiveEToolsStartingEquipment startingEquipment;
    private final FiveEToolsMechanicsOverlay overlay;

    ClassConverter(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        this.objectMapper = objectMapper;
        this.overlay = overlay;
        this.proficiencies = new FiveEToolsProficiencies(objectMapper);
        this.progression = new FiveEToolsClassProgression(objectMapper);
        this.startingEquipment = new FiveEToolsStartingEquipment(objectMapper);
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.CLASS;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        return FiveEToolsClassData.load(dataSource, objectMapper, overlay).classes();
    }

    @Override
    public CatalogueEntryImport convert(JsonNode playerClass) {
        String name = playerClass.get("name").asString();
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.CLASS,
                FiveEToolsNaming.slug(name),
                name,
                playerClass.get("source").asString(),
                playerClass.get("page") == null ? null : playerClass.get("page").asInt(),
                List.of(),
                "",
                data(playerClass));
    }

    private ObjectNode data(JsonNode playerClass) {
        ArrayNode features = (ArrayNode) playerClass.get(FiveEToolsClassData.FEATURES_FIELD);
        ObjectNode data = objectMapper.createObjectNode();
        data.put("hitDie", playerClass.get("hd").get("faces").asInt());
        data.set("savingThrows", abilityKeys(playerClass.get("proficiency")));
        data.set("proficiencies", proficiencies.map(playerClass.get("startingProficiencies")));
        data.set("startingEquipment", startingEquipment(playerClass.get("startingEquipment")));
        data.set("multiclassing", multiclassing(playerClass.get("multiclassing")));
        data.set("spellcasting", progression.spellcasting(playerClass, playerClass.get("classTableGroups")));
        data.set("tableColumns", progression.tableColumns(playerClass.get("classTableGroups")));
        data.set("optionalFeatureProgressions",
                progression.optionalFeatureProgressions(playerClass.get("optionalfeatureProgression")));
        data.put("subclassTitle", playerClass.get("subclassTitle").asString());
        data.put("subclassLevel", subclassLevel(features));
        data.set("abilityScoreImprovementLevels", abilityScoreImprovementLevels(features));
        data.set("features", features);
        return data;
    }

    private ObjectNode startingEquipment(JsonNode block) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("additionalFromBackground", block.path("additionalFromBackground").asBoolean(false));
        JsonNode gold = block.get("goldAlternative");
        result.put("goldAlternative", gold == null ? null : TagMarkupStripper.strip(gold.asString()));
        ArrayNode text = result.putArray("text");
        for (JsonNode line : block.get("default")) {
            text.add(TagMarkupStripper.strip(line.asString()));
        }
        result.set("groups", startingEquipment.groups(block.get("defaultData")));
        return result;
    }

    /** Ability minimums: every key of {@code allOf} is required; any single key of {@code anyOf} suffices. */
    private JsonNode multiclassing(JsonNode block) {
        if (block == null) {
            return objectMapper.nullNode();
        }
        ObjectNode result = objectMapper.createObjectNode();
        ObjectNode allOf = result.putObject("allOf");
        ObjectNode anyOf = result.putObject("anyOf");
        JsonNode requirements = block.get("requirements");
        if (requirements != null) {
            for (Map.Entry<String, JsonNode> requirement : requirements.properties()) {
                if ("or".equals(requirement.getKey())) {
                    requirement.getValue().forEach(alternative -> putAbilityMinimums(anyOf, alternative));
                } else {
                    allOf.put(FiveEToolsNaming.abilityKey(requirement.getKey()), requirement.getValue().asInt());
                }
            }
        }
        result.set("proficienciesGained", proficiencies.map(block.get("proficienciesGained")));
        return result;
    }

    private static void putAbilityMinimums(ObjectNode target, JsonNode abilities) {
        for (Map.Entry<String, JsonNode> ability : abilities.properties()) {
            target.put(FiveEToolsNaming.abilityKey(ability.getKey()), ability.getValue().asInt());
        }
    }

    private ArrayNode abilityKeys(JsonNode abbreviations) {
        ArrayNode keys = objectMapper.createArrayNode();
        if (abbreviations != null) {
            abbreviations.forEach(abbreviation -> keys.add(FiveEToolsNaming.abilityKey(abbreviation.asString())));
        }
        return keys;
    }

    private static Integer subclassLevel(ArrayNode features) {
        for (JsonNode feature : features) {
            if (feature.get("grantsSubclassFeature").asBoolean()) {
                return feature.get("level").asInt();
            }
        }
        return null;
    }

    private ArrayNode abilityScoreImprovementLevels(ArrayNode features) {
        ArrayNode levels = objectMapper.createArrayNode();
        for (JsonNode feature : features) {
            if (ABILITY_SCORE_IMPROVEMENT.equals(feature.get("name").asString())) {
                levels.add(feature.get("level").asInt());
            }
        }
        return levels;
    }
}

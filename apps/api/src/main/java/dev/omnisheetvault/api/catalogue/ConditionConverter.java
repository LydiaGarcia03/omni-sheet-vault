package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools condition (Blinded, Poisoned, Exhaustion…) into a {@code CONDITION}
 * catalogue entry. 5etools describes their effects only in prose, so {@code modifiers}
 * (and, for exhaustion, {@code levels}: modifiers per level) come from the adr-0007
 * overlay.
 */
final class ConditionConverter implements FiveEToolsConverter {

    private final ObjectMapper objectMapper;
    private final FiveEToolsMechanicsOverlay overlay;

    ConditionConverter(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        this.objectMapper = objectMapper;
        this.overlay = overlay;
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.CONDITION;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        List<JsonNode> conditions = new ArrayList<>();
        JsonNode array = dataSource.readDataFile("conditionsdiseases.json").get("condition");
        if (array != null) {
            array.forEach(conditions::add);
        }
        overlay.matchEntries(FiveEToolsMechanicsOverlay.CONDITION, conditions);
        return conditions;
    }

    @Override
    public CatalogueEntryImport convert(JsonNode condition) {
        String name = condition.get("name").asString();
        JsonNode mechanics = overlay.findEntry(FiveEToolsMechanicsOverlay.CONDITION, name, condition.get("source").asString());
        ObjectNode data = objectMapper.createObjectNode();
        data.set("modifiers", mechanics == null ? objectMapper.createArrayNode() : mechanics.path("modifiers").deepCopy());
        data.set("levels", mechanics == null || !mechanics.hasNonNull("levels") ? objectMapper.nullNode() : mechanics.get("levels").deepCopy());
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.CONDITION,
                FiveEToolsNaming.slug(name),
                name,
                condition.get("source").asString(),
                condition.get("page") == null || !condition.get("page").isNumber() ? null : condition.get("page").asInt(),
                List.of(),
                TagMarkupStripper.strip(FiveEToolsEntries.flatten(condition.get("entries"))),
                data);
    }
}

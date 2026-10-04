package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools background ({@code _copy} resolved) into a {@code BACKGROUND}
 * catalogue entry. The four suggestion tables the Background tab rolls on
 * ({@code personalityTraits}, {@code ideals}, {@code bonds}, {@code flaws}) are found by
 * their column label wherever they sit in the text; any other table is kept under
 * {@code otherTables}. Entries tagged {@code edition: "one"} are the 2024 ruleset and
 * are dropped.
 */
final class BackgroundConverter implements FiveEToolsConverter {

    private static final Map<String, String> CHARACTERISTIC_FIELDS = Map.of(
            "personality trait", "personalityTraits", "trait", "personalityTraits",
            "ideal", "ideals", "bond", "bonds", "flaw", "flaws");
    private static final Pattern FEATURE_NAME = Pattern.compile("^(?:.*\\s)?Feature:\\s*(.+)$");
    private static final String REVISED_2024_EDITION = "one";

    private final ObjectMapper objectMapper;
    private final FiveEToolsProficiencies proficiencies;
    private final FiveEToolsStartingEquipment startingEquipment;

    BackgroundConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.proficiencies = new FiveEToolsProficiencies(objectMapper);
        this.startingEquipment = new FiveEToolsStartingEquipment(objectMapper);
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.BACKGROUND;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        JsonNode backgrounds = dataSource.readDataFile("backgrounds.json").get("background");
        Map<String, ObjectNode> resolved = FiveEToolsCopies.resolveAll(
                backgrounds, BackgroundConverter::key, BackgroundConverter::key);
        List<JsonNode> kept = new ArrayList<>();
        for (JsonNode raw : backgrounds) {
            ObjectNode background = resolved.get(key(raw));
            if (!REVISED_2024_EDITION.equals(background.path("edition").asString(""))) {
                kept.add(background);
            }
        }
        return kept;
    }

    @Override
    public CatalogueEntryImport convert(JsonNode background) {
        String name = background.get("name").asString();
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.BACKGROUND,
                FiveEToolsNaming.slug(name),
                name,
                background.get("source").asString(),
                background.get("page") == null ? null : background.get("page").asInt(),
                List.of(),
                TagMarkupStripper.strip(FiveEToolsEntries.flatten(background.get("entries"))),
                data(background));
    }

    private ObjectNode data(JsonNode background) {
        ObjectNode data = objectMapper.createObjectNode();
        for (String field : List.of("personalityTraits", "ideals", "bonds", "flaws")) {
            data.putArray(field);
        }
        ArrayNode otherTables = objectMapper.createArrayNode();
        collectTables(background.get("entries"), data, otherTables);
        data.set("otherTables", otherTables);
        data.set("features", features(background.get("entries")));
        data.set("skillAlternatives", proficiencies.skillAlternatives(background.get("skillProficiencies")));
        data.set("toolAlternatives", proficiencies.toolAlternatives(background.get("toolProficiencies")));
        data.set("languageAlternatives", proficiencies.grantAlternatives(background.get("languageProficiencies"), FiveEToolsNaming::slug));
        data.set("featAlternatives", proficiencies.grantAlternatives(background.get("feats"), FiveEToolsNaming::slug));
        data.set("skillToolLanguageChoices", copyOrNull(background.get("skillToolLanguageProficiencies")));
        data.set("startingEquipment", startingEquipment.groups(background.get("startingEquipment")));
        data.set("additionalSpells", copyOrNull(background.get("additionalSpells")));
        data.set("prerequisites", copyOrNull(background.get("prerequisite")));
        return data;
    }

    private void collectTables(JsonNode entries, ObjectNode data, ArrayNode otherTables) {
        if (entries == null) {
            return;
        }
        for (JsonNode entry : entries) {
            if (!entry.isObject()) {
                continue;
            }
            if ("table".equals(entry.path("type").asString(""))) {
                addTable(entry, data, otherTables);
            }
            collectTables(entry.get("entries"), data, otherTables);
            collectTables(entry.get("items"), data, otherTables);
        }
    }

    private void addTable(JsonNode table, ObjectNode data, ArrayNode otherTables) {
        List<String> labels = new ArrayList<>();
        table.path("colLabels").forEach(label -> labels.add(TagMarkupStripper.strip(label.asString())));
        String lastLabel = labels.isEmpty() ? "" : labels.getLast().toLowerCase(Locale.ROOT);
        String field = CHARACTERISTIC_FIELDS.get(lastLabel);
        if (field != null) {
            ArrayNode target = (ArrayNode) data.get(field);
            for (JsonNode row : table.get("rows")) {
                target.add(cellText(row.get(row.size() - 1)));
            }
            return;
        }
        ObjectNode other = otherTables.addObject();
        other.put("caption", table.get("caption") == null ? null : TagMarkupStripper.strip(table.get("caption").asString()));
        ArrayNode columns = other.putArray("colLabels");
        labels.forEach(columns::add);
        ArrayNode rows = other.putArray("rows");
        for (JsonNode row : table.get("rows")) {
            ArrayNode cells = rows.addArray();
            row.forEach(cell -> cells.add(cellText(cell)));
        }
    }

    /** A table cell is a string, a {@code {type: "cell", roll: {exact | min, max}}} die face, or a nested entry. */
    private static String cellText(JsonNode cell) {
        if (cell.isString()) {
            return TagMarkupStripper.strip(cell.asString());
        }
        if (cell.isNumber()) {
            return String.valueOf(cell.asInt());
        }
        JsonNode roll = cell.get("roll");
        if (roll != null) {
            return roll.get("exact") != null
                    ? String.valueOf(roll.get("exact").asInt())
                    : roll.get("min").asInt() + "–" + roll.get("max").asInt();
        }
        if (cell.get("entry") != null) {
            return cellText(cell.get("entry"));
        }
        if (cell.get("entries") != null) {
            return TagMarkupStripper.strip(FiveEToolsEntries.flatten(cell.get("entries")));
        }
        throw new FiveEToolsIngestException("Unsupported 5etools table cell: " + cell);
    }

    /** Named blocks such as "Feature: Military Rank" or "Baldur's Gate Feature: Religious Community". */
    private ArrayNode features(JsonNode entries) {
        ArrayNode features = objectMapper.createArrayNode();
        if (entries == null) {
            return features;
        }
        for (JsonNode entry : entries) {
            if (!entry.isObject() || entry.get("name") == null) {
                continue;
            }
            Matcher matcher = FEATURE_NAME.matcher(entry.get("name").asString());
            if (matcher.matches() || entry.path("data").path("isFeature").asBoolean(false)) {
                ObjectNode feature = features.addObject();
                feature.put("name", matcher.matches() ? matcher.group(1) : entry.get("name").asString());
                feature.put("description", TagMarkupStripper.strip(FiveEToolsEntries.flatten(entry.get("entries"))));
            }
        }
        return features;
    }

    private JsonNode copyOrNull(JsonNode node) {
        return node == null ? objectMapper.nullNode() : node.deepCopy();
    }

    private static String key(JsonNode entry) {
        return (entry.get("name").asString() + "|" + entry.get("source").asString()).toLowerCase(Locale.ROOT);
    }
}

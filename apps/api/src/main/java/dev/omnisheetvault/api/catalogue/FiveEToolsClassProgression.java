package dev.omnisheetvault.api.catalogue;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Maps a 5etools class's level-by-level progression: spellcasting (slots, pact slots,
 * cantrips and spells known, prepared-spell formula), optional-feature picks, and the
 * remaining class-table columns (Rages, Sneak Attack, Martial Arts, …).
 */
final class FiveEToolsClassProgression {

    static final int MAX_LEVEL = 20;

    private static final Pattern PREPARED_FORMULA =
            Pattern.compile("^<\\$level\\$>(?:\\s*/\\s*(\\d+))?\\s*\\+\\s*<\\$(\\w+)_mod\\$>$");
    private static final Pattern LEADING_DIGITS = Pattern.compile("^(\\d+)");
    private static final String PACT_SLOTS_LABEL = "Spell Slots";
    private static final String PACT_SLOT_LEVEL_LABEL = "Slot Level";

    private final ObjectMapper objectMapper;

    FiveEToolsClassProgression(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /** {@code entry} is a class or a spellcasting subclass; {@code tableGroups} is its class or subclass table. */
    ObjectNode spellcasting(JsonNode entry, JsonNode tableGroups) {
        ObjectNode result = objectMapper.createObjectNode();
        JsonNode ability = entry.get("spellcastingAbility");
        result.put("ability", ability == null ? null : FiveEToolsNaming.abilityKey(ability.asString()));
        JsonNode progression = entry.get("casterProgression");
        result.put("casterProgression", progression == null ? null : progression.asString());
        result.set("preparedSpellsFormula", preparedSpellsFormula(entry.get("preparedSpells")));
        result.set("cantripsKnownByLevel", copyOrNull(entry.get("cantripProgression")));
        result.set("spellsKnownByLevel", copyOrNull(entry.get("spellsKnownProgression")));
        result.set("spellbookSpellsAddedByLevel", copyOrNull(entry.get("spellsKnownProgressionFixed")));
        result.set("spellSlotsByLevel", spellSlots(tableGroups));
        result.set("pactSlotsByLevel", pactSlots(tableGroups));
        return result;
    }

    /** Every class-table column that isn't the per-level spell slot grid, rendered as display strings. */
    ArrayNode tableColumns(JsonNode classTableGroups) {
        ArrayNode columns = objectMapper.createArrayNode();
        for (JsonNode group : iterable(classTableGroups)) {
            JsonNode rows = group.get("rows");
            if (rows == null) {
                continue;
            }
            JsonNode labels = group.get("colLabels");
            for (int column = 0; column < labels.size(); column++) {
                ObjectNode mapped = columns.addObject();
                mapped.put("label", TagMarkupStripper.strip(labels.get(column).asString()));
                ArrayNode values = mapped.putArray("valuesByLevel");
                for (JsonNode row : rows) {
                    values.add(cellText(row.get(column)));
                }
            }
        }
        return columns;
    }

    /**
     * {@code progression} is either a 20-entry array or a sparse {@code {"level": count}} object carried
     * forward; the key {@code "*"} means every level.
     */
    ArrayNode optionalFeatureProgressions(JsonNode progressions) {
        ArrayNode result = objectMapper.createArrayNode();
        for (JsonNode progression : iterable(progressions)) {
            ObjectNode mapped = result.addObject();
            mapped.put("name", progression.get("name").asString());
            ArrayNode featureTypes = mapped.putArray("featureTypes");
            progression.get("featureType").forEach(featureTypes::add);
            mapped.set("countByLevel", countByLevel(progression.get("progression")));
        }
        return result;
    }

    private ArrayNode countByLevel(JsonNode progression) {
        if (progression.isArray()) {
            return (ArrayNode) progression.deepCopy();
        }
        ArrayNode counts = objectMapper.createArrayNode();
        int current = progression.path("*").asInt(0);
        for (int level = 1; level <= MAX_LEVEL; level++) {
            JsonNode atLevel = progression.get(String.valueOf(level));
            if (atLevel != null) {
                current = atLevel.asInt();
            }
            counts.add(current);
        }
        return counts;
    }

    private JsonNode preparedSpellsFormula(JsonNode formula) {
        if (formula == null) {
            return objectMapper.nullNode();
        }
        Matcher matcher = PREPARED_FORMULA.matcher(formula.asString().trim());
        if (!matcher.matches()) {
            throw new FiveEToolsIngestException("Unsupported prepared-spells formula: " + formula.asString());
        }
        ObjectNode result = objectMapper.createObjectNode();
        result.put("levelDivisor", matcher.group(1) == null ? 1 : Integer.parseInt(matcher.group(1)));
        result.put("ability", FiveEToolsNaming.abilityKey(matcher.group(2)));
        return result;
    }

    private JsonNode spellSlots(JsonNode classTableGroups) {
        for (JsonNode group : iterable(classTableGroups)) {
            JsonNode rows = group.get("rowsSpellProgression");
            if (rows != null) {
                return rows.deepCopy();
            }
        }
        return objectMapper.nullNode();
    }

    private JsonNode pactSlots(JsonNode classTableGroups) {
        for (JsonNode group : iterable(classTableGroups)) {
            List<String> labels = strippedLabels(group.get("colLabels"));
            int slotsColumn = labels.indexOf(PACT_SLOTS_LABEL);
            int slotLevelColumn = labels.indexOf(PACT_SLOT_LEVEL_LABEL);
            if (slotsColumn < 0 || slotLevelColumn < 0 || group.get("rows") == null) {
                continue;
            }
            ArrayNode byLevel = objectMapper.createArrayNode();
            for (JsonNode row : group.get("rows")) {
                ObjectNode level = byLevel.addObject();
                level.put("slots", row.get(slotsColumn).asInt());
                level.put("slotLevel", ordinal(cellText(row.get(slotLevelColumn))));
            }
            return byLevel;
        }
        return objectMapper.nullNode();
    }

    private static List<String> strippedLabels(JsonNode labels) {
        if (labels == null) {
            return List.of();
        }
        return StreamSupport.stream(labels.spliterator(), false)
                .map(label -> TagMarkupStripper.strip(label.asString()))
                .toList();
    }

    private static int ordinal(String text) {
        Matcher matcher = LEADING_DIGITS.matcher(text);
        if (!matcher.find()) {
            throw new FiveEToolsIngestException("Not an ordinal slot level: " + text);
        }
        return Integer.parseInt(matcher.group(1));
    }

    /** Renders one class-table cell the way the PHB prints it; an unknown cell shape fails ingestion rather than being guessed. */
    static String cellText(JsonNode cell) {
        if (cell.isNumber()) {
            return String.valueOf(cell.asInt());
        }
        if (cell.isString()) {
            return TagMarkupStripper.strip(cell.asString());
        }
        String type = cell.path("type").asString("");
        return switch (type) {
            case "bonus" -> signed(cell.get("value").asInt());
            case "bonusSpeed" -> cell.get("value").asInt() == 0 ? "—" : signed(cell.get("value").asInt()) + " ft.";
            case "dice" -> diceText(cell.get("toRoll"));
            default -> throw new FiveEToolsIngestException("Unsupported class-table cell: " + cell);
        };
    }

    private static String diceText(JsonNode toRoll) {
        StringBuilder text = new StringBuilder();
        for (JsonNode dice : toRoll) {
            if (!text.isEmpty()) {
                text.append(" + ");
            }
            text.append(dice.get("number").asInt()).append('d').append(dice.get("faces").asInt());
        }
        return text.toString();
    }

    private static String signed(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private JsonNode copyOrNull(JsonNode node) {
        return node == null ? objectMapper.nullNode() : node.deepCopy();
    }

    private Iterable<JsonNode> iterable(JsonNode node) {
        return node == null ? objectMapper.createArrayNode() : node;
    }
}

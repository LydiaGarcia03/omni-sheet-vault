package dev.omnisheetvault.api.catalogue;

import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Flattens a 5etools {@code entries} array into plain text, recursing into nested
 * {@code list} and named {@code entries} blocks. Reference blocks ({@code refClassFeature},
 * {@code refOptionalfeature}, {@code options}, …) carry no prose and render as nothing.
 */
final class FiveEToolsEntries {

    private static final Map<String, String> ABILITY_NAMES = Map.of(
            "str", "Strength", "dex", "Dexterity", "con", "Constitution",
            "int", "Intelligence", "wis", "Wisdom", "cha", "Charisma");

    private FiveEToolsEntries() {
    }

    static String flatten(JsonNode entriesArray) {
        StringBuilder text = new StringBuilder();
        if (entriesArray != null) {
            for (JsonNode entry : entriesArray) {
                appendEntry(text, entry);
            }
        }
        return text.toString();
    }

    private static void appendEntry(StringBuilder text, JsonNode entry) {
        if (entry.isString()) {
            appendSeparated(text, entry.asString());
            return;
        }
        if (!entry.isObject()) {
            return;
        }
        String type = typeName(entry);
        if ("list".equals(type)) {
            appendChildren(text, entry.get("items"));
            return;
        }
        if ("abilityDc".equals(type) || "abilityAttackMod".equals(type)) {
            appendSeparated(text, abilityFormulaText(entry, type));
            return;
        }
        JsonNode name = entry.get("name");
        if (name != null) {
            appendSeparated(text, name.asString() + ".");
        }
        JsonNode singleEntry = entry.get("entry");
        if (singleEntry != null) {
            appendEntry(text, singleEntry);
        }
        appendChildren(text, entry.get("entries"));
    }

    private static void appendChildren(StringBuilder text, JsonNode children) {
        if (children != null) {
            for (JsonNode child : children) {
                appendEntry(text, child);
            }
        }
    }

    /** Renders the PHB's own wording for a spellcasting class's save DC / attack modifier box. */
    private static String abilityFormulaText(JsonNode entry, String type) {
        String abilities = abilityList(entry.get("attributes"));
        String label = entry.get("name") == null ? "" : entry.get("name").asString() + " ";
        return "abilityDc".equals(type)
                ? label + "save DC = 8 + your proficiency bonus + your " + abilities + " modifier"
                : label + "attack modifier = your proficiency bonus + your " + abilities + " modifier";
    }

    private static String abilityList(JsonNode attributes) {
        StringBuilder names = new StringBuilder();
        if (attributes != null) {
            for (JsonNode attribute : attributes) {
                if (!names.isEmpty()) {
                    names.append(" or ");
                }
                names.append(ABILITY_NAMES.getOrDefault(attribute.asString(), attribute.asString()));
            }
        }
        return names.toString();
    }

    private static String typeName(JsonNode entry) {
        JsonNode type = entry.get("type");
        return type == null ? null : type.asString();
    }

    private static void appendSeparated(StringBuilder text, String value) {
        if (!text.isEmpty()) {
            text.append(' ');
        }
        text.append(value);
    }
}

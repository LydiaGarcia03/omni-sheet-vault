package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import tools.jackson.databind.JsonNode;

/**
 * Renders an optional feature's 5etools {@code prerequisite} block the way 5etools'
 * own renderer does in its classic style ({@code render.js}, {@code _getEntry_*}):
 * "5th level", "Pact of the Tome", "eldritch blast cantrip". Conditions of one
 * alternative are joined with ", "; alternatives with "; ". A key this class doesn't
 * know fails ingestion rather than being dropped.
 */
final class FiveEToolsPrerequisites {

    private static final Set<String> SUPPORTED_KEYS = Set.of("level", "pact", "spell", "item", "otherSummary");

    private FiveEToolsPrerequisites() {
    }

    static String text(JsonNode prerequisiteArray) {
        if (!FiveEToolsNaming.isNonEmptyArray(prerequisiteArray)) {
            return null;
        }
        List<String> alternatives = new ArrayList<>();
        for (JsonNode alternative : prerequisiteArray) {
            alternatives.add(alternativeText(alternative));
        }
        return String.join("; ", alternatives);
    }

    private static String alternativeText(JsonNode alternative) {
        for (Map.Entry<String, JsonNode> condition : alternative.properties()) {
            if (!SUPPORTED_KEYS.contains(condition.getKey())) {
                throw new FiveEToolsIngestException("Unsupported optional-feature prerequisite: " + alternative);
            }
        }
        List<String> parts = new ArrayList<>();
        if (alternative.get("level") != null) {
            parts.add(levelText(alternative.get("level")));
        }
        if (alternative.get("pact") != null) {
            parts.add(pactText(alternative.get("pact").asString()));
        }
        if (alternative.get("spell") != null) {
            parts.add(joinOr(alternative.get("spell"), FiveEToolsPrerequisites::spellText));
        }
        if (alternative.get("item") != null) {
            parts.add(joinOr(alternative.get("item"), JsonNode::asString));
        }
        if (alternative.get("otherSummary") != null) {
            parts.add(TagMarkupStripper.strip(alternative.get("otherSummary").get("entry").asString()));
        }
        return String.join(", ", parts);
    }

    /** The class name only shows when 5etools marks it {@code visible}, matching its classic style. */
    private static String levelText(JsonNode level) {
        int value = level.isNumber() ? level.asInt() : level.get("level").asInt();
        String text = ordinal(value) + " level";
        JsonNode playerClass = level.get("class");
        if (playerClass != null && playerClass.path("visible").asBoolean(false)) {
            text += " " + playerClass.get("name").asString();
        }
        return text;
    }

    private static String pactText(String pact) {
        return switch (pact) {
            case "Chain", "Tome", "Blade", "Talisman" -> "Pact of the " + pact;
            default -> pact;
        };
    }

    /** {@code name#c} is a cantrip; {@code #x} is 5etools' shorthand for the hex-or-curse requirement. */
    private static String spellText(JsonNode spell) {
        String[] parts = spell.asString().split("#", 2);
        if (parts.length == 1) {
            return parts[0];
        }
        return switch (parts[1]) {
            case "c" -> parts[0] + " cantrip";
            case "x" -> "hex spell or a warlock feature that curses";
            default -> spell.asString();
        };
    }

    private static String joinOr(JsonNode values, Function<JsonNode, String> render) {
        List<String> rendered = new ArrayList<>();
        values.forEach(value -> rendered.add(render.apply(value)));
        if (rendered.size() <= 1) {
            return String.join("", rendered);
        }
        return String.join(", ", rendered.subList(0, rendered.size() - 1)) + " or " + rendered.getLast();
    }

    private static String ordinal(int value) {
        int lastDigit = value % 10;
        int lastTwo = value % 100;
        if (lastDigit == 1 && lastTwo != 11) {
            return value + "st";
        }
        if (lastDigit == 2 && lastTwo != 12) {
            return value + "nd";
        }
        if (lastDigit == 3 && lastTwo != 13) {
            return value + "rd";
        }
        return value + "th";
    }
}

package dev.omnisheetvault.api.catalogue;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;

/**
 * Naming/text helpers shared by every {@link FiveEToolsConverter} — extracted out of
 * {@link SpellConverter} once {@link ItemConverter} needed the exact same slug/source-book/
 * entries-flattening logic, per systems/dnd-5e/features/5etools-ingestion.md's own "shared, kind-independent
 * pipeline pieces" design.
 */
final class FiveEToolsNaming {

    private static final Map<String, String> ABILITY_KEYS = Map.of(
            "str", "strength", "dex", "dexterity", "con", "constitution",
            "int", "intelligence", "wis", "wisdom", "cha", "charisma");

    private static final Pattern SLUG_SEPARATORS = Pattern.compile("[^a-z0-9]+");

    private FiveEToolsNaming() {
    }

    static String slug(String name) {
        String withoutApostrophes = name.toLowerCase(Locale.ROOT).replace("'", "");
        String slug = SLUG_SEPARATORS.matcher(withoutApostrophes).replaceAll("-");
        return trimHyphens(slug);
    }

    private static String trimHyphens(String text) {
        int start = 0;
        int end = text.length();
        while (start < end && text.charAt(start) == '-') {
            start++;
        }
        while (end > start && text.charAt(end - 1) == '-') {
            end--;
        }
        return text.substring(start, end);
    }

    /** Strips a trailing {@code |SOURCE} tag some 5etools codes carry (e.g. a {@code type} of {@code "WD|DMG"}). */
    static String stripSourceSuffix(String code) {
        int pipe = code.indexOf('|');
        return pipe < 0 ? code : code.substring(0, pipe);
    }

    /** Joins an {@code entries} array's plain-string elements; non-string entries (nested sub-blocks) are skipped, not flattened — named in each caller's own scope note. */
    static String rawEntriesText(Iterable<? extends JsonNode> entriesArray) {
        StringBuilder text = new StringBuilder();
        for (JsonNode entry : entriesArray) {
            if (entry.isString()) {
                if (!text.isEmpty()) {
                    text.append(' ');
                }
                text.append(entry.asString());
            }
        }
        return text.toString();
    }

    /** The sheet's own camelCase key for a 5etools name, e.g. {@code "sleight of hand"} → {@code "sleightOfHand"}. */
    static String camelCaseKey(String name) {
        String[] words = slug(name).split("-");
        StringBuilder key = new StringBuilder(words[0]);
        for (int i = 1; i < words.length; i++) {
            key.append(capitalize(words[i]));
        }
        return key.toString();
    }

    /** The sheet's own ability key for a 5etools ability abbreviation, e.g. {@code "int"} → {@code "intelligence"}. */
    static String abilityKey(String abbreviation) {
        String key = ABILITY_KEYS.get(abbreviation.toLowerCase(Locale.ROOT));
        if (key == null) {
            throw new FiveEToolsIngestException("Unknown 5etools ability abbreviation: " + abbreviation);
        }
        return key;
    }

    static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    static boolean isNonEmptyArray(JsonNode node) {
        return node != null && node.isArray() && !node.isEmpty();
    }
}

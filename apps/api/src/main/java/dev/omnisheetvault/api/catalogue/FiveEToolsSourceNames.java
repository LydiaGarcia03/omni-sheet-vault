package dev.omnisheetvault.api.catalogue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;

/**
 * Full source names from 5etools' own {@code books.json} and {@code adventures.json},
 * keyed by each entry's {@code source} code (case-insensitive). A book wins over an
 * adventure sharing its code. Codes neither file lists use {@link #UNLISTED_NAMES},
 * else the code itself. D&D 5e has one edition of each core book, so the "(2014)"
 * 5etools adds to tell them from the 2024 books is dropped.
 *
 * <p>Also knows which sources belong to the 2024 rules: every book or adventure
 * published on or after the 2024 Player's Handbook ({@value #REVISED_RULES_BOOK}).
 */
final class FiveEToolsSourceNames {

    static final String REVISED_RULES_BOOK = "XPHB";
    private static final String EDITION_SUFFIX = " (2014)";
    private static final Map<String, String> UNLISTED_NAMES = Map.of(
            "TFTYP", "Tales from the Yawning Portal",
            "UATHEMYSTICCLASS", "UA: The Mystic Class",
            "HAT-LMI", "Honor Among Thieves: Legendary Magic Items",
            "ROTOS", "The Rise of Tiamat Online Supplement",
            "EET", "Elemental Evil: Trinkets",
            "EEPC", "Elemental Evil Player's Companion",
            "MCV2DC", "Monstrous Compendium Volume 2: Dragonlance Creatures");

    private final Map<String, String> names;
    private final Set<String> revisedRulesSources;

    private FiveEToolsSourceNames(Map<String, String> names, Set<String> revisedRulesSources) {
        this.names = names;
        this.revisedRulesSources = revisedRulesSources;
    }

    static FiveEToolsSourceNames load(FiveEToolsDataSource dataSource) {
        Map<String, String> names = new HashMap<>(UNLISTED_NAMES);
        Map<String, String> published = new HashMap<>();
        putAll(names, published, dataSource.readDataFile("adventures.json").get("adventure"));
        putAll(names, published, dataSource.readDataFile("books.json").get("book"));
        String threshold = published.get(REVISED_RULES_BOOK);
        Set<String> revised = new HashSet<>();
        if (threshold != null) {
            published.forEach((code, date) -> {
                if (date.compareTo(threshold) >= 0) {
                    revised.add(code);
                }
            });
        }
        return new FiveEToolsSourceNames(names, revised);
    }

    String nameOf(String sourceCode) {
        return names.getOrDefault(sourceCode.toUpperCase(Locale.ROOT), sourceCode);
    }

    /** Upper-case codes of the sources written for the 2024 rules. */
    Set<String> revisedRulesSources() {
        return revisedRulesSources;
    }

    private static void putAll(Map<String, String> names, Map<String, String> published, JsonNode entries) {
        if (entries == null) {
            return;
        }
        for (JsonNode entry : entries) {
            String code = entry.get("source").asString().toUpperCase(Locale.ROOT);
            String name = entry.get("name").asString();
            names.put(code, name.endsWith(EDITION_SUFFIX) ? name.substring(0, name.length() - EDITION_SUFFIX.length()) : name);
            if (entry.hasNonNull("published")) {
                published.put(code, entry.get("published").asString());
            }
        }
    }
}

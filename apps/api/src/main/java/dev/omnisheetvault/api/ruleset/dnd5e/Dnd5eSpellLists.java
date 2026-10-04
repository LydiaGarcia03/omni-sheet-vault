package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import tools.jackson.databind.JsonNode;

/**
 * The catalogue's spells, indexed for character creation: class lists (from each
 * spell's 5etools {@code classes}), name lookups for granted-spell references, and
 * 5etools {@code {@filter}}-style queries ("level=0|class=Wizard").
 */
final class Dnd5eSpellLists {

    private static final String PREFERRED_SOURCE = "PHB";
    private static final Map<String, String> SCHOOL_CODES = Map.of(
            "a", "abjuration", "c", "conjuration", "d", "divination", "e", "enchantment",
            "v", "evocation", "i", "illusion", "n", "necromancy", "t", "transmutation");

    private final List<CatalogueRecord> spells;
    private final Map<String, CatalogueRecord> bySlug = new LinkedHashMap<>();
    private final Map<String, List<CatalogueRecord>> byName = new LinkedHashMap<>();

    Dnd5eSpellLists(CatalogueLookup catalogue) {
        this.spells = catalogue.list("SPELL").stream().sorted(Comparator.comparing(CatalogueRecord::name)).toList();
        for (CatalogueRecord spell : spells) {
            bySlug.put(spell.slug(), spell);
            byName.computeIfAbsent(nameKey(spell.name()), key -> new ArrayList<>()).add(spell);
        }
    }

    Optional<CatalogueRecord> bySlug(String slug) {
        return Optional.ofNullable(bySlug.get(slug));
    }

    /** A 5etools spell reference ("hellish rebuke#2", "cure wounds|phb"): its named source if given, else the PHB version, else the first. */
    Optional<CatalogueRecord> byReference(String reference) {
        String[] parts = reference.split("#")[0].split("\\|");
        List<CatalogueRecord> named = byName.getOrDefault(nameKey(parts[0]), List.of());
        if (parts.length > 1) {
            String source = parts[1].strip();
            Optional<CatalogueRecord> fromSource = named.stream().filter(spell -> sourceCode(spell).equalsIgnoreCase(source)).findFirst();
            if (fromSource.isPresent()) {
                return fromSource;
            }
        }
        return named.stream().filter(spell -> sourceCode(spell).equals(PREFERRED_SOURCE)).findFirst()
                .or(() -> named.stream().findFirst());
    }

    /** The 5etools source code ("PHB", "XGE") the converter keeps beside the full book name. */
    static String sourceCode(CatalogueRecord spell) {
        return spell.data().path("sourceCode").asString("");
    }

    /** Spells on a class's own list, up to {@code maxLevel}. */
    List<CatalogueRecord> classList(String className, int minLevel, int maxLevel) {
        return spells.stream()
                .filter(spell -> onClassList(spell, className))
                .filter(spell -> level(spell) >= minLevel && level(spell) <= maxLevel)
                .toList();
    }

    /** Spells a subclass adds to its class's list (Chronurgy's dunamancy spells, a domain's spells), up to {@code maxLevel}. */
    List<CatalogueRecord> subclassList(String className, String subclassShortName, int minLevel, int maxLevel) {
        return spells.stream()
                .filter(spell -> onSubclassList(spell, className, subclassShortName))
                .filter(spell -> level(spell) >= minLevel && level(spell) <= maxLevel)
                .toList();
    }

    private static boolean onSubclassList(CatalogueRecord spell, String className, String subclassShortName) {
        for (JsonNode entry : spell.data().path("subclasses")) {
            if (entry.path("className").asString("").equalsIgnoreCase(className)
                    && entry.path("subclassShortName").asString("").equalsIgnoreCase(subclassShortName)) {
                return true;
            }
        }
        return false;
    }

    /** Spells matching a 5etools filter; an unsupported filter key throws. */
    List<CatalogueRecord> matching(String filter) {
        Predicate<CatalogueRecord> predicate = spell -> true;
        for (String part : filter.split("\\|")) {
            String[] keyAndValues = part.split("=", 2);
            if (keyAndValues.length != 2) {
                throw new IllegalArgumentException("Unreadable spell filter: " + filter);
            }
            Set<String> values = Arrays.stream(keyAndValues[1].split(";"))
                    .map(value -> value.strip().toLowerCase(Locale.ROOT)).collect(Collectors.toSet());
            predicate = predicate.and(condition(keyAndValues[0].strip().toLowerCase(Locale.ROOT), values, filter));
        }
        return spells.stream().filter(predicate).toList();
    }

    static int level(CatalogueRecord spell) {
        return spell.data().path("level").asInt();
    }

    static String nameKey(String name) {
        return name.strip().toLowerCase(Locale.ROOT);
    }

    private static Predicate<CatalogueRecord> condition(String key, Set<String> values, String filter) {
        return switch (key) {
            case "level" -> spell -> values.contains(String.valueOf(level(spell)));
            case "class" -> spell -> values.stream().anyMatch(className -> onClassList(spell, className));
            case "school" -> spell -> values.stream().map(SCHOOL_CODES::get)
                    .anyMatch(school -> school != null && school.equals(spell.data().path("school").asString()));
            case "source" -> spell -> values.contains(sourceCode(spell).toLowerCase(Locale.ROOT));
            case "spell attack" -> spell -> spell.data().path("attackRoll").asBoolean(false);
            case "components & miscellaneous" -> {
                if (!values.equals(Set.of("ritual"))) {
                    throw new IllegalArgumentException("Unsupported spell filter: " + filter);
                }
                yield spell -> spell.data().path("ritual").asBoolean(false);
            }
            default -> throw new IllegalArgumentException("Unsupported spell filter: " + filter);
        };
    }

    private static boolean onClassList(CatalogueRecord spell, String className) {
        for (JsonNode entry : spell.data().path("classes")) {
            if (entry.path("name").asString("").equalsIgnoreCase(className)) {
                return true;
            }
        }
        return false;
    }
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * Builds option lists for creation choices from the catalogue: skills, tools by
 * category, languages by type, weapons and focuses by equipment category, feats,
 * optional features and subclasses. Only mundane items ({@code rarity: none}) are
 * starting-equipment or proficiency options.
 */
final class Dnd5eChoiceOptions {

    private static final Map<String, Set<String>> TOOL_CATEGORY_TYPE_LABELS = Map.of(
            "artisansTool", Set.of("Artisan's Tools"),
            "musicalInstrument", Set.of("Instrument"),
            "gamingSet", Set.of("Gaming Set"),
            "any", Set.of("Artisan's Tools", "Instrument", "Gaming Set", "Tool"));
    private static final Map<String, Set<String>> LANGUAGE_CATEGORY_TYPES = Map.of(
            "standard", Set.of("standard"),
            "exotic", Set.of("exotic"),
            "any", Set.of("standard", "exotic", "rare"));
    private static final Map<String, String> EQUIPMENT_TOOL_CATEGORIES = Map.of(
            "instrumentMusical", "musicalInstrument", "toolArtisan", "artisansTool", "setGaming", "gamingSet");
    private static final Map<String, String> FOCUS_TYPES = Map.of(
            "focusSpellcastingArcane", "arcane", "focusSpellcastingDruidic", "druid", "focusSpellcastingHoly", "holy");

    private final CatalogueLookup catalogue;
    private final boolean includeOptionalClassFeatures;
    private final Dnd5eOptionSummaries summaries;

    Dnd5eChoiceOptions(CatalogueLookup catalogue, boolean includeOptionalClassFeatures) {
        this.catalogue = catalogue;
        this.includeOptionalClassFeatures = includeOptionalClassFeatures;
        this.summaries = new Dnd5eOptionSummaries(catalogue, !catalogue.redactsProse());
    }

    Dnd5eOptionSummaries summaries() {
        return summaries;
    }

    /** A catalogue entry as an option whose summary says what it gives. */
    CreationChoiceOption summarized(CatalogueRecord record) {
        CreationChoiceOption plain = option(record);
        return new CreationChoiceOption(plain.key(), plain.label(), plain.sourceBook(), summaries.of(record), plain.data());
    }

    List<CreationChoiceOption> skills(List<String> keys) {
        List<String> selected = keys == null ? List.copyOf(Dnd5eSkills.LABELS.keySet()) : keys;
        return selected.stream().map(key -> new CreationChoiceOption(key, Dnd5eSkills.LABELS.getOrDefault(key, key), null, null)).toList();
    }

    List<CreationChoiceOption> abilities(List<String> keys) {
        return keys.stream().map(key -> new CreationChoiceOption(key, capitalize(key), null, null)).toList();
    }

    /** {@code keys} are item slugs; a key with no catalogue item (e.g. {@code vehicles-land}) keeps a label derived from it. */
    List<CreationChoiceOption> tools(List<String> keys, String category) {
        if (keys != null) {
            return keys.stream().map(this::itemOption).toList();
        }
        Set<String> typeLabels = TOOL_CATEGORY_TYPE_LABELS.get(category);
        if (typeLabels == null) {
            throw new IllegalArgumentException("Unknown tool category: " + category);
        }
        return mundaneItems(item -> typeLabels.contains(item.data().path("typeLabel").asString("")));
    }

    /**
     * A language is one proficiency however many books print it, so same-named entries
     * collapse into one option keyed by the plain name ({@code elvish}) — the key species
     * and backgrounds use for their fixed grants. The PHB entry is preferred as its source.
     */
    List<CreationChoiceOption> languages(List<String> keys, String category) {
        if (keys != null) {
            return keys.stream().map(key -> recordOption("LANGUAGE", key)).toList();
        }
        Set<String> types = LANGUAGE_CATEGORY_TYPES.get(category);
        if (types == null) {
            throw new IllegalArgumentException("Unknown language category: " + category);
        }
        Map<String, CreationChoiceOption> byName = new LinkedHashMap<>();
        catalogue.list("LANGUAGE").stream()
                .filter(language -> types.contains(language.data().path("type").asString("")))
                .sorted(Comparator.comparing(language -> !"Player's Handbook".equals(language.sourceBook())))
                .forEach(language -> byName.putIfAbsent(Dnd5ePrerequisites.slug(language.name()),
                        new CreationChoiceOption(Dnd5ePrerequisites.slug(language.name()), language.name(), language.sourceBook(),
                                Dnd5eOptionSummaries.language(language.data()))));
        return sorted(List.copyOf(byName.values()));
    }

    /** 5etools weapon filters name a category ("martial weapon", "simple weapon"); anything else is unsupported. */
    Optional<List<CreationChoiceOption>> weaponsByFilter(String fromFilter) {
        String filter = fromFilter.toLowerCase(Locale.ROOT);
        String category = filter.contains("martial weapon") ? "MARTIAL" : filter.contains("simple weapon") ? "SIMPLE" : null;
        if (category == null) {
            return Optional.empty();
        }
        return Optional.of(mundaneItems(item -> category.equals(item.data().path("weaponCategory").asString(""))));
    }

    /** Options for a starting-equipment category such as {@code weaponMartialMelee} or {@code focusSpellcastingArcane}. */
    Optional<List<CreationChoiceOption>> equipmentCategory(String equipmentType) {
        String focus = FOCUS_TYPES.get(equipmentType);
        if (focus != null) {
            return Optional.of(mundaneItems(item -> focus.equals(item.data().path("focusType").asString(""))));
        }
        String toolCategory = EQUIPMENT_TOOL_CATEGORIES.get(equipmentType);
        if (toolCategory != null) {
            return Optional.of(tools(null, toolCategory));
        }
        String lower = equipmentType.toLowerCase(Locale.ROOT);
        if (!lower.startsWith("weapon")) {
            return Optional.empty();
        }
        String category = lower.contains("martial") ? "MARTIAL" : lower.contains("simple") ? "SIMPLE" : null;
        boolean meleeOnly = lower.endsWith("melee");
        if (category == null) {
            return Optional.empty();
        }
        return Optional.of(mundaneItems(item -> category.equals(item.data().path("weaponCategory").asString(""))
                && (!meleeOnly || "MELEE".equals(item.data().path("attackType").asString("")))));
    }

    List<CatalogueRecord> feats() {
        return catalogue.list("FEAT");
    }

    /** Excludes TCE-style optional class features ({@code optional: true}) unless the build enables them, as D&D Beyond does. */
    List<CatalogueRecord> optionalFeatures(List<String> featureTypes) {
        return catalogue.list("OPTIONAL_FEATURE").stream()
                .filter(feature -> includeOptionalClassFeatures || !feature.data().path("optional").asBoolean(false))
                .filter(feature -> {
                    for (JsonNode type : feature.data().path("featureTypes")) {
                        if (featureTypes.contains(type.asString())) {
                            return true;
                        }
                    }
                    return false;
                })
                .sorted(Comparator.comparing(CatalogueRecord::name))
                .toList();
    }

    List<CreationChoiceOption> subclasses(String classSlug) {
        return sorted(catalogue.list("SUBCLASS").stream()
                .filter(subclass -> classSlug.equals(subclass.data().path("classSlug").asString("")))
                .map(this::summarized)
                .toList());
    }

    /** Mechanics a spell picker filters and shows by; the description is prose and stays behind catalogue redaction. */
    private static final List<String> SPELL_PICKER_FIELDS = List.of(
            "level", "school", "castingTime", "range", "duration", "components", "concentration", "ritual",
            "attackRoll", "saveAbility", "damageType", "damageDiceCount", "damageDiceSides", "effectSummary");

    static CreationChoiceOption option(CatalogueRecord record) {
        if ("SPELL".equals(record.kind())) {
            ObjectNode data = JsonNodeFactory.instance.objectNode();
            for (String field : SPELL_PICKER_FIELDS) {
                JsonNode value = record.data().get(field);
                if (value == null) {
                    data.putNull(field);
                } else {
                    data.set(field, value);
                }
            }
            return new CreationChoiceOption(record.slug(), record.name(), record.sourceBook(), Dnd5eOptionSummaries.spell(record.data()), data);
        }
        if ("LANGUAGE".equals(record.kind())) {
            return new CreationChoiceOption(record.slug(), record.name(), record.sourceBook(), Dnd5eOptionSummaries.language(record.data()));
        }
        return new CreationChoiceOption(record.slug(), record.name(), record.sourceBook(), null);
    }

    private CreationChoiceOption itemOption(String slug) {
        return recordOption("ITEM", slug);
    }

    private CreationChoiceOption recordOption(String kind, String slug) {
        return catalogue.find(kind, slug).map(Dnd5eChoiceOptions::option)
                .orElse(new CreationChoiceOption(slug, labelFromSlug(slug), null, null));
    }

    private List<CreationChoiceOption> mundaneItems(Predicate<CatalogueRecord> filter) {
        return sorted(catalogue.list("ITEM").stream()
                .filter(item -> "none".equals(item.data().path("rarity").asString("")))
                .filter(filter)
                .map(Dnd5eChoiceOptions::option)
                .toList());
    }

    private static List<CreationChoiceOption> sorted(List<CreationChoiceOption> options) {
        List<CreationChoiceOption> copy = new ArrayList<>(options);
        copy.sort(Comparator.comparing(CreationChoiceOption::label));
        return copy;
    }

    static String labelFromSlug(String slug) {
        String spaced = slug.replace('-', ' ');
        return capitalize(spaced);
    }

    static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }
}

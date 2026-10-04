package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools spell entry into this project's own catalogue shape —
 * systems/dnd-5e/features/5etools-ingestion.md's field mapping table, plus the six {@code data}
 * fields that table left out ({@code attackRoll}/{@code damageDiceCount}/
 * {@code damageDiceSides}/{@code notes}, all derived below; {@code effectSummary},
 * a best-effort heuristic the owner explicitly accepted may drift from D&D Beyond's
 * own hand-curated "Effect" column, since it isn't a 5etools fact to begin with;
 * {@code higherLevelsDescription}, open question 5, resolved 2026-09-17 — see
 * {@link #higherLevelsDescription}).
 */
final class SpellConverter implements FiveEToolsConverter {

    private static final Map<String, String> SCHOOL_NAMES = Map.of(
            "A", "abjuration", "C", "conjuration", "D", "divination", "E", "enchantment",
            "V", "evocation", "I", "illusion", "N", "necromancy", "T", "transmutation");

    private static final Pattern FIRST_DAMAGE_DICE = Pattern.compile("\\{@damage\\s+(\\d+)d(\\d+)");
    /** Healing spells notate their own roll with {@code {@dice}}, never {@code {@damage}} — they deal no damage. */
    private static final Pattern FIRST_DICE = Pattern.compile("\\{@dice\\s+(\\d+)d(\\d+)");
    /** {@code {@scaledamage}}/{@code {@scaledice}}'s own 3rd pipe segment — the per-slot-level-above-base increment, always plain dice notation (confirmed 2026-09-17 against real data, see TagMarkupStripper). */
    private static final Pattern SCALING_PER_LEVEL_DICE =
            Pattern.compile("\\{@(?:scaledamage|scaledice)\\s+[^|{}]*\\|[^|{}]*\\|(\\d+)d(\\d+)");

    private static final String CLASS_LISTS_FILE = "spells/sources.json";
    private static final String SOURCE_LOOKUP_FILE = "generated/gendata-spell-source-lookup.json";
    private static final String CLASS_LISTS_FIELD = "_classLists";

    private final ObjectMapper objectMapper;
    private final FiveEToolsMechanicsOverlay overlay;

    SpellConverter(ObjectMapper objectMapper) {
        this(objectMapper, FiveEToolsMechanicsOverlay.empty());
    }

    SpellConverter(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        this.objectMapper = objectMapper;
        this.overlay = overlay;
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.SPELL;
    }

    /**
     * Each raw spell carries its class lists from {@code spells/sources.json} under
     * {@value #CLASS_LISTS_FIELD}: {@code classes} (lists it is on) and
     * {@code optionalClasses} (lists an optional class feature from another book adds
     * it to, e.g. TCE's expanded lists). References to 2024-edition classes are dropped.
     */
    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        JsonNode sources = dataSource.hasDataFile(CLASS_LISTS_FILE) ? dataSource.readDataFile(CLASS_LISTS_FILE) : null;
        JsonNode lookup = dataSource.hasDataFile(SOURCE_LOOKUP_FILE) ? dataSource.readDataFile(SOURCE_LOOKUP_FILE) : null;
        ClassEditions editions = ClassEditions.load(dataSource);
        List<JsonNode> spells = new ArrayList<>();
        for (JsonNode file : dataSource.readDataFiles("spells", "spells-*.json")) {
            JsonNode spellArray = file.get("spell");
            if (spellArray != null) {
                for (JsonNode spell : spellArray) {
                    spells.add(withClassLists(spell, sources, lookup, editions));
                }
            }
        }
        overlay.matchEntries(FiveEToolsMechanicsOverlay.SPELL, spells);
        return spells;
    }

    /**
     * Subclass spell lists ({@code subclasses}: class name and source, subclass short
     * name and source) come from 5etools' generated lookup, keyed by lower-case source
     * and name, as {@code subclass → class source → class → subclass source → short name}.
     */
    private ArrayNode subclassReferences(JsonNode spell, JsonNode lookup, ClassEditions editions) {
        ArrayNode subclasses = objectMapper.createArrayNode();
        if (lookup == null) {
            return subclasses;
        }
        JsonNode bySource = lookup.path(spell.path("source").asString().toLowerCase(Locale.ROOT))
                .path(spell.path("name").asString().toLowerCase(Locale.ROOT)).path("subclass");
        for (Map.Entry<String, JsonNode> classSource : bySource.properties()) {
            for (Map.Entry<String, JsonNode> playerClass : classSource.getValue().properties()) {
                ObjectNode classReference = objectMapper.createObjectNode()
                        .put("name", playerClass.getKey()).put("source", classSource.getKey());
                if (editions.isNewEdition(classReference)) {
                    continue;
                }
                for (Map.Entry<String, JsonNode> subclassSource : playerClass.getValue().properties()) {
                    for (String shortName : subclassSource.getValue().propertyNames()) {
                        subclasses.addObject()
                                .put("className", playerClass.getKey()).put("classSource", classSource.getKey())
                                .put("subclassShortName", shortName).put("subclassSource", subclassSource.getKey());
                    }
                }
            }
        }
        return subclasses;
    }

    private JsonNode withClassLists(JsonNode spell, JsonNode sources, JsonNode lookup, ClassEditions editions) {
        JsonNode lists = sources == null ? null : sources.path(spell.path("source").asString()).path(spell.path("name").asString());
        ArrayNode classes = objectMapper.createArrayNode();
        ArrayNode optionalClasses = objectMapper.createArrayNode();
        if (lists != null) {
            for (JsonNode reference : lists.path("class")) {
                addReference(classes, reference, editions);
            }
            for (JsonNode reference : lists.path("classVariant")) {
                String book = reference.path("definedInSource").asString();
                boolean optional = !book.equals(spell.path("source").asString()) && editions.hasOptionalFeaturesFrom(reference, book);
                addReference(optional ? optionalClasses : classes, reference, editions);
            }
        }
        ObjectNode annotated = (ObjectNode) spell.deepCopy();
        ObjectNode classLists = annotated.putObject(CLASS_LISTS_FIELD);
        classLists.set("classes", classes);
        classLists.set("optionalClasses", optionalClasses);
        classLists.set("subclasses", subclassReferences(spell, lookup, editions));
        return annotated;
    }

    private static void addReference(ArrayNode target, JsonNode reference, ClassEditions editions) {
        if (editions.isNewEdition(reference)) {
            return;
        }
        String name = reference.path("name").asString();
        String source = reference.path("source").asString();
        for (JsonNode existing : target) {
            if (existing.path("name").asString().equals(name) && existing.path("source").asString().equals(source)) {
                return;
            }
        }
        target.addObject().put("name", name).put("source", source);
    }

    /** Which 5etools classes are 2024-edition, and which books add optional class features (TCE's variants) to each class. */
    private record ClassEditions(Set<String> newEdition, Map<String, Set<String>> optionalFeatureSources) {

        static ClassEditions load(FiveEToolsDataSource dataSource) {
            Set<String> newEdition = new HashSet<>();
            Map<String, Set<String>> optionalSources = new HashMap<>();
            if (!dataSource.hasDataFile("class/index.json")) {
                return new ClassEditions(newEdition, optionalSources);
            }
            for (JsonNode file : dataSource.readDataFiles("class", "class-*.json")) {
                for (JsonNode playerClass : file.path("class")) {
                    if ("one".equals(playerClass.path("edition").asString(""))) {
                        newEdition.add(key(playerClass.path("name").asString(), playerClass.path("source").asString()));
                    }
                }
                for (JsonNode feature : file.path("classFeature")) {
                    if (feature.path("isClassFeatureVariant").asBoolean(false)) {
                        optionalSources.computeIfAbsent(key(feature.path("className").asString(), feature.path("classSource").asString()),
                                ignored -> new HashSet<>()).add(feature.path("source").asString());
                    }
                }
            }
            return new ClassEditions(newEdition, optionalSources);
        }

        boolean isNewEdition(JsonNode reference) {
            return newEdition.contains(key(reference.path("name").asString(), reference.path("source").asString()));
        }

        boolean hasOptionalFeaturesFrom(JsonNode reference, String book) {
            return optionalFeatureSources.getOrDefault(key(reference.path("name").asString(), reference.path("source").asString()), Set.of())
                    .contains(book);
        }

        private static String key(String name, String source) {
            return name + "|" + source;
        }
    }

    @Override
    public CatalogueEntryImport convert(JsonNode spell) {
        String name = spell.get("name").asString();
        String rawSource = spell.get("source").asString();
        DurationResult durationResult = duration(spell.get("duration"));
        ComponentsResult componentsResult = components(spell.get("components"));
        String description = description(spell.get("entries"));

        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.SPELL,
                FiveEToolsNaming.slug(name),
                name,
                rawSource,
                spell.get("page").asInt(),
                tags(spell.get("miscTags"), spell.get("conditionInflict")),
                description,
                data(spell, durationResult, componentsResult, description));
    }

    private JsonNode data(JsonNode spell, DurationResult durationResult, ComponentsResult componentsResult, String description) {
        String damageType = firstOrNull(spell.get("damageInflict"));
        boolean attackRoll = FiveEToolsNaming.isNonEmptyArray(spell.get("spellAttack"));
        boolean isHealing = containsText(spell.get("miscTags"), "HL");
        DamageDice damageDice = damageDice(spell.get("entries"), isHealing);
        DamageDice scalingDice = scalingPerLevelDice(spell.get("entriesHigherLevel"));
        String effectSummary = effectSummary(damageType, isHealing, spell.get("conditionInflict"), spell.get("school"));

        ObjectNode data = objectMapper.createObjectNode();
        data.put("level", spell.get("level").asInt());
        data.put("school", schoolName(spell.get("school").asString()));
        data.put("castingTime", castingTime(spell.get("time")));
        data.put("range", range(spell.get("range")));
        data.put("concentration", durationResult.concentration());
        data.put("ritual", ritual(spell.get("meta")));
        data.put("attackRoll", attackRoll);
        putIntOrNull(data, "damageDiceCount", damageDice.count());
        putIntOrNull(data, "damageDiceSides", damageDice.sides());
        putStringOrNull(data, "damageType", damageType);
        data.put("notes", notes(durationResult, componentsResult));
        putStringOrNull(data, "effectSummary", effectSummary);
        putStringOrNull(data, "saveAbility", firstOrNull(spell.get("savingThrow")));
        data.put("components", componentsResult.components());
        putStringOrNull(data, "materialComponent", componentsResult.materialComponent());
        data.put("duration", durationResult.duration());
        putStringOrNull(data, "higherLevelsDescription", higherLevelsDescription(spell.get("entriesHigherLevel")));
        putIntOrNull(data, "higherLevelsDamageDiceCount", scalingDice.count());
        putIntOrNull(data, "higherLevelsDamageDiceSides", scalingDice.sides());
        data.put("sourceCode", spell.path("source").asString());
        JsonNode classLists = spell.path(CLASS_LISTS_FIELD);
        data.set("classes", classLists.has("classes") ? classLists.get("classes").deepCopy() : objectMapper.createArrayNode());
        data.set("optionalClasses",
                classLists.has("optionalClasses") ? classLists.get("optionalClasses").deepCopy() : objectMapper.createArrayNode());
        data.set("subclasses", classLists.has("subclasses") ? classLists.get("subclasses").deepCopy() : objectMapper.createArrayNode());
        JsonNode mechanics = overlay.findEntry(FiveEToolsMechanicsOverlay.SPELL, spell.get("name").asString(), spell.path("source").asString());
        data.set("effect", mechanics == null || !mechanics.hasNonNull("effect") ? objectMapper.nullNode() : mechanics.get("effect").deepCopy());
        return data;
    }

    private static String schoolName(String code) {
        String name = SCHOOL_NAMES.get(code.toUpperCase(Locale.ROOT));
        if (name == null) {
            throw new FiveEToolsIngestException("Unknown 5etools school code: " + code);
        }
        return name;
    }

    private static String castingTime(JsonNode timeArray) {
        JsonNode first = timeArray.get(0);
        int number = first.get("number").asInt();
        String unit = first.get("unit").asString();
        String label = switch (unit) {
            case "action" -> "Action";
            case "bonus" -> "Bonus Action";
            case "reaction" -> "Reaction";
            case "minute" -> number == 1 ? "Minute" : "Minutes";
            case "hour" -> number == 1 ? "Hour" : "Hours";
            default -> FiveEToolsNaming.capitalize(unit);
        };
        return number + " " + label;
    }

    private static String range(JsonNode rangeNode) {
        String type = rangeNode.get("type").asString();
        JsonNode distance = rangeNode.get("distance");
        return switch (type) {
            case "point" -> pointRange(distance);
            case "special" -> "Special";
            default -> "Self (" + distance.get("amount").asInt() + "-foot " + type + ")";
        };
    }

    private static String pointRange(JsonNode distance) {
        String distanceType = distance.get("type").asString();
        return switch (distanceType) {
            case "self" -> "Self";
            case "touch" -> "Touch";
            case "feet" -> distance.get("amount").asInt() + " ft.";
            case "miles" -> distance.get("amount").asInt() + " mi.";
            default -> FiveEToolsNaming.capitalize(distanceType);
        };
    }

    private record DurationResult(String duration, boolean concentration) {
    }

    private static DurationResult duration(JsonNode durationArray) {
        JsonNode first = durationArray.get(0);
        String type = first.get("type").asString();
        boolean concentration = first.get("concentration") != null && first.get("concentration").asBoolean();

        String text = switch (type) {
            case "instant" -> "Instantaneous";
            case "permanent" -> "Until dispelled";
            case "special" -> "Special";
            case "timed" -> timedDuration(first.get("duration"), concentration);
            default -> FiveEToolsNaming.capitalize(type);
        };
        return new DurationResult(text, concentration);
    }

    private static String timedDuration(JsonNode timedNode, boolean concentration) {
        int amount = timedNode.get("amount").asInt();
        String unit = timedNode.get("type").asString();
        boolean upTo = timedNode.get("upTo") != null && timedNode.get("upTo").asBoolean();
        String unitLabel = switch (unit) {
            case "round" -> amount == 1 ? "round" : "rounds";
            case "minute" -> amount == 1 ? "minute" : "minutes";
            case "hour" -> amount == 1 ? "hour" : "hours";
            case "day" -> amount == 1 ? "day" : "days";
            default -> unit;
        };
        String amountText = amount + " " + unitLabel;
        if (concentration) {
            return upTo ? "Concentration, up to " + amountText : "Concentration, " + amountText;
        }
        return upTo ? "Up to " + amountText : amountText;
    }

    private record ComponentsResult(String components, String materialComponent) {
    }

    private static ComponentsResult components(JsonNode componentsNode) {
        List<String> parts = new ArrayList<>();
        if (isTrue(componentsNode.get("v"))) {
            parts.add("V");
        }
        if (isTrue(componentsNode.get("s"))) {
            parts.add("S");
        }
        JsonNode material = componentsNode.get("m");
        String materialText = null;
        if (material != null) {
            parts.add("M");
            if (material.isString()) {
                materialText = material.asString();
            } else if (material.isObject() && material.get("text") != null) {
                materialText = material.get("text").asString();
            }
        }
        return new ComponentsResult(String.join(", ", parts), materialText);
    }

    private static String notes(DurationResult durationResult, ComponentsResult componentsResult) {
        String componentsWithMaterial = componentsResult.materialComponent() == null
                ? componentsResult.components()
                : componentsResult.components() + " (" + componentsResult.materialComponent() + ")";
        return durationResult.concentration()
                ? durationResult.duration() + ", " + componentsWithMaterial
                : componentsWithMaterial;
    }

    private static boolean ritual(JsonNode meta) {
        return meta != null && isTrue(meta.get("ritual"));
    }

    private record DamageDice(Integer count, Integer sides) {
        private static final DamageDice NONE = new DamageDice(null, null);
    }

    /**
     * A damage spell notates its roll with {@code {@damage}}; a healing spell (no
     * damage type of its own) notates the exact same shape of roll with
     * {@code {@dice}} instead — found live 2026-09-17, tracing why Cure Wounds'
     * healing die went missing: this method originally only looked for
     * {@code {@damage}}. Gated on {@code isHealing} rather than always falling back
     * to {@code {@dice}}, since a non-healing spell's entries can reference an
     * unrelated die roll in passing without it being the spell's own primary roll.
     */
    private static DamageDice damageDice(JsonNode entriesArray, boolean isHealing) {
        String text = FiveEToolsNaming.rawEntriesText(entriesArray);
        Matcher damageMatcher = FIRST_DAMAGE_DICE.matcher(text);
        if (damageMatcher.find()) {
            return new DamageDice(Integer.valueOf(damageMatcher.group(1)), Integer.valueOf(damageMatcher.group(2)));
        }
        if (isHealing) {
            Matcher diceMatcher = FIRST_DICE.matcher(text);
            if (diceMatcher.find()) {
                return new DamageDice(Integer.valueOf(diceMatcher.group(1)), Integer.valueOf(diceMatcher.group(2)));
            }
        }
        return DamageDice.NONE;
    }

    /**
     * The per-slot-level-above-base increment to {@code damageDiceCount}/
     * {@code damageDiceSides} when cast at a higher level — systems/dnd-5e/features/
     * 5etools-ingestion.md's open question 5. Parsed structurally from the same
     * {@code {@scaledamage}}/{@code {@scaledice}} tag {@link #higherLevelsDescription}
     * renders as prose, so a caller that needs the actual numbers (scaling a roll)
     * isn't stuck re-parsing display text.
     */
    private static DamageDice scalingPerLevelDice(JsonNode entriesHigherLevelArray) {
        if (!FiveEToolsNaming.isNonEmptyArray(entriesHigherLevelArray)) {
            return DamageDice.NONE;
        }
        JsonNode entries = entriesHigherLevelArray.get(0).get("entries");
        if (entries == null) {
            return DamageDice.NONE;
        }
        Matcher matcher = SCALING_PER_LEVEL_DICE.matcher(FiveEToolsNaming.rawEntriesText(entries));
        if (matcher.find()) {
            return new DamageDice(Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));
        }
        return DamageDice.NONE;
    }

    private static String description(JsonNode entriesArray) {
        return TagMarkupStripper.strip(FiveEToolsNaming.rawEntriesText(entriesArray));
    }

    /**
     * 5etools' own "At Higher Levels" scaling text — systems/dnd-5e/features/5etools-ingestion.md's
     * open question 5, resolved: {@code entriesHigherLevel} is an array (almost
     * always one element) shaped {@code {"type":"entries","name":"At Higher
     * Levels"|"Using a Higher-Level Spell Slot","entries":[...]}}; only the nested
     * {@code entries} strings are used, the {@code name} label itself is not — some
     * non-PHB sourcebooks use the alternate label for the same thing (confirmed
     * 2026-09-17 against real downloaded data), so keying off a specific label string
     * would silently drop those. Null for a spell with no such text (most cantrips,
     * and some leveled spells).
     */
    private static String higherLevelsDescription(JsonNode entriesHigherLevelArray) {
        if (!FiveEToolsNaming.isNonEmptyArray(entriesHigherLevelArray)) {
            return null;
        }
        JsonNode entries = entriesHigherLevelArray.get(0).get("entries");
        if (entries == null) {
            return null;
        }
        String text = TagMarkupStripper.strip(FiveEToolsNaming.rawEntriesText(entries));
        return text.isEmpty() ? null : text;
    }

    private static String effectSummary(String damageType, boolean isHealing, JsonNode conditionInflict, JsonNode school) {
        if (damageType != null) {
            return "Damage";
        }
        if (isHealing) {
            return "Healing";
        }
        if (FiveEToolsNaming.isNonEmptyArray(conditionInflict)) {
            return "Control";
        }
        if (school != null && "D".equalsIgnoreCase(school.asString())) {
            return "Detection";
        }
        return "Buff";
    }

    private static List<String> tags(JsonNode miscTags, JsonNode conditionInflict) {
        List<String> tags = new ArrayList<>();
        if (containsText(miscTags, "HL")) {
            tags.add("Healing");
        }
        if (conditionInflict != null) {
            for (JsonNode condition : conditionInflict) {
                tags.add(FiveEToolsNaming.capitalize(condition.asString()));
            }
        }
        return tags;
    }

    private static boolean isTrue(JsonNode node) {
        return node != null && node.asBoolean();
    }

    private static boolean containsText(JsonNode arrayNode, String value) {
        if (arrayNode == null) {
            return false;
        }
        for (JsonNode entry : arrayNode) {
            if (value.equals(entry.asString())) {
                return true;
            }
        }
        return false;
    }

    private static String firstOrNull(JsonNode arrayNode) {
        return FiveEToolsNaming.isNonEmptyArray(arrayNode) ? arrayNode.get(0).asString() : null;
    }

    private static void putStringOrNull(ObjectNode node, String field, String value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, value);
        }
    }

    private static void putIntOrNull(ObjectNode node, String field, Integer value) {
        if (value == null) {
            node.putNull(field);
        } else {
            node.put(field, value.intValue());
        }
    }
}

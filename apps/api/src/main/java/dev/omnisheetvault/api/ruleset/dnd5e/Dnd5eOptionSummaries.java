package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

/**
 * What a builder option gives, in one line built from its catalogue data: "CON +2 · Speed 25 ft ·
 * Darkvision 60 ft". Feats and optional features also quote their first benefit sentence, unless
 * the catalogue's prose is redacted (adr-0005).
 */
final class Dnd5eOptionSummaries {

    private static final String SEPARATOR = " · ";
    private static final int MAX_SENTENCE = 220;
    private static final Pattern BENEFITS_LEAD = Pattern.compile("(?i)following benefits:\\s*");
    private static final Pattern PREREQUISITE_LEAD = Pattern.compile("^Prerequisite:[^\\n]*\\n+");
    private static final Pattern FIRST_SENTENCE = Pattern.compile("^(.+?[.!?])(\\s|$)", Pattern.DOTALL);
    private static final Map<String, String> ABILITY_ABBREVIATIONS = Map.of(
            "strength", "STR", "dexterity", "DEX", "constitution", "CON",
            "intelligence", "INT", "wisdom", "WIS", "charisma", "CHA");
    private static final Map<String, String> TOOL_CATEGORY_LABELS = Map.of(
            "gamingSet", "gaming set", "musicalInstrument", "musical instrument",
            "artisansTool", "artisan's tools", "any", "tool");
    private static final List<String> HEAVIER_ARMOR = List.of("light", "medium", "heavy");

    private final CatalogueLookup catalogue;
    private final boolean includeProse;

    Dnd5eOptionSummaries(CatalogueLookup catalogue, boolean includeProse) {
        this.catalogue = catalogue;
        this.includeProse = includeProse;
    }

    /** The summary of a catalogue entry offered as an option; null for kinds that keep a plain line (items). */
    String of(CatalogueRecord record) {
        return switch (record.kind()) {
            case "SPECIES" -> species(record.data());
            case "BACKGROUND" -> background(record.data());
            case "CLASS" -> playerClass(record.data());
            case "SUBCLASS" -> subclass(record.data());
            case "FEAT" -> joined(abilityIncreases(record.data().get("abilityAlternatives")), prose(record.description()));
            case "OPTIONAL_FEATURE" -> prose(record.description());
            case "SPELL" -> spell(record.data());
            case "LANGUAGE" -> language(record.data());
            default -> null;
        };
    }

    /** A species, subspecies or variant node: its ability increases, speed, senses, resistances and level-1 spells. */
    String species(JsonNode node) {
        List<String> parts = new ArrayList<>();
        parts.add(abilityIncreases(node.get("abilityAlternatives")));
        parts.add(speed(node.get("speed")));
        parts.add(senses(node.get("senses")));
        List<String> resistances = strings(node.path("damageResistances").get("fixed"));
        if (!resistances.isEmpty()) {
            parts.add(capitalize(String.join(", ", resistances)) + " resistance");
        }
        parts.add(knownSpells(node.get("additionalSpells")));
        return joined(parts.toArray(String[]::new));
    }

    static String spell(JsonNode data) {
        int level = data.path("level").asInt();
        String school = data.path("school").asString("");
        String header = level == 0 ? capitalize(school) + " cantrip" : ordinal(level) + "-level " + school;
        if (data.path("ritual").asBoolean(false)) {
            header += " (ritual)";
        }
        String duration = data.path("duration").asString(null);
        if (duration != null && data.path("concentration").asBoolean(false) && !duration.startsWith("Concentration")) {
            duration = "Concentration, " + duration;
        }
        String damage = null;
        if (data.hasNonNull("damageDiceCount") && data.hasNonNull("damageDiceSides")) {
            damage = data.get("damageDiceCount").asInt() + "d" + data.get("damageDiceSides").asInt()
                    + (data.hasNonNull("damageType") ? " " + data.get("damageType").asString() : "");
        }
        String castingTime = data.path("castingTime").asString(null);
        return joined(header, castingTime == null ? null : castingTime.toLowerCase(Locale.ROOT), data.path("range").asString(null),
                data.path("components").asString(null), duration, damage);
    }

    static String language(JsonNode data) {
        String type = data.path("type").asString(null);
        String script = data.path("script").asString(null);
        return joined(type == null ? null : capitalize(type), script == null ? null : script + " script");
    }

    private String background(JsonNode data) {
        List<String> features = StreamSupport.stream(data.path("features").spliterator(), false)
                .map(feature -> feature.path("name").asString(null)).filter(name -> name != null).toList();
        return joined(skills(data.get("skillAlternatives")), tools(data.get("toolAlternatives")), languages(data.get("languageAlternatives")),
                listOrNull(features));
    }

    /** "Athletics, Intimidation", plus any skills of the player's choice. */
    static String skills(JsonNode alternatives) {
        JsonNode skills = alternative(alternatives);
        List<String> parts = new ArrayList<>(strings(skills.get("fixed")).stream().map(key -> Dnd5eSkills.LABELS.getOrDefault(key, key)).toList());
        for (JsonNode choice : skills.path("choices")) {
            parts.add(choice.path("count").asInt(1) + " skill(s) of your choice");
        }
        return listOrNull(parts);
    }

    /** "Vehicles (Land), 1 gaming set". */
    String tools(JsonNode alternatives) {
        JsonNode tools = alternative(alternatives);
        List<String> parts = new ArrayList<>(strings(tools.get("fixed")).stream().map(this::toolName).toList());
        for (JsonNode choice : tools.path("choices")) {
            String category = TOOL_CATEGORY_LABELS.getOrDefault(choice.path("category").asString(""), "tool");
            parts.add(choice.path("count").asInt(1) + " " + category);
        }
        return listOrNull(parts);
    }

    /** "Common, Dwarvish", plus any languages of the player's choice. */
    static String languages(JsonNode alternatives) {
        JsonNode languages = alternative(alternatives);
        List<String> parts = new ArrayList<>(strings(languages.get("fixed")).stream().map(Dnd5eOptionSummaries::capitalize).toList());
        for (JsonNode choice : languages.path("choices")) {
            int count = choice.path("count").asInt(1);
            parts.add(count + (count == 1 ? " language" : " languages") + " of your choice");
        }
        return listOrNull(parts);
    }

    /** A block's first alternative: 5etools writes some as a list of alternatives, others as the one block. */
    private static JsonNode alternative(JsonNode alternatives) {
        return alternatives != null && alternatives.isArray() ? first(alternatives) : alternatives == null ? MissingNode.getInstance() : alternatives;
    }

    private static String playerClass(JsonNode data) {
        String hitDie = data.hasNonNull("hitDie") ? "d" + data.get("hitDie").asInt() : null;
        List<String> saves = strings(data.get("savingThrows")).stream().map(ability -> ABILITY_ABBREVIATIONS.getOrDefault(ability, ability)).toList();
        JsonNode proficiencies = data.path("proficiencies");
        return joined(hitDie, saves.isEmpty() ? null : "Saves " + String.join(", ", saves),
                armor(strings(proficiencies.get("armor"))), weapons(strings(proficiencies.get("weaponCategories"))));
    }

    /** The features a subclass grants at its first level, leaving out the "… Options" lists that only index choices. */
    private static String subclass(JsonNode data) {
        int firstLevel = StreamSupport.stream(data.path("features").spliterator(), false)
                .mapToInt(feature -> feature.path("level").asInt(Integer.MAX_VALUE)).min().orElse(Integer.MAX_VALUE);
        if (firstLevel == Integer.MAX_VALUE) {
            return null;
        }
        List<String> names = StreamSupport.stream(data.path("features").spliterator(), false)
                .filter(feature -> feature.path("level").asInt() == firstLevel)
                .map(feature -> feature.path("name").asString(""))
                .filter(name -> !name.isEmpty() && !name.endsWith(" Options"))
                .toList();
        return names.isEmpty() ? null : "Level " + firstLevel + ": " + String.join(", ", names);
    }

    /** The catalogue's full prose, or null when it is redacted. */
    String fullProse(String description) {
        return includeProse && description != null && !description.isBlank() ? description.strip() : null;
    }

    /** The first benefit sentence of a description, after its "Prerequisite:" line and its "following benefits:" lead; null when prose is redacted. */
    private String prose(String description) {
        if (!includeProse || description == null || description.isBlank()) {
            return null;
        }
        String text = PREREQUISITE_LEAD.matcher(description.strip()).replaceFirst("");
        Matcher benefits = BENEFITS_LEAD.matcher(text);
        if (benefits.find()) {
            text = text.substring(benefits.end());
        }
        Matcher sentence = FIRST_SENTENCE.matcher(text.strip());
        String first = sentence.find() ? sentence.group(1) : text.strip();
        first = first.replaceAll("\\s+", " ");
        return first.length() > MAX_SENTENCE ? first.substring(0, MAX_SENTENCE - 1).strip() + "…" : first;
    }

    static String abilityIncreases(JsonNode alternatives) {
        JsonNode alternative = alternatives != null && alternatives.isArray() ? first(alternatives) : alternatives;
        if (alternative == null || alternative.isMissingNode() || alternative.isNull()) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        alternative.path("fixed").properties().forEach(entry ->
                parts.add(ABILITY_ABBREVIATIONS.getOrDefault(entry.getKey(), entry.getKey()) + " " + signed(entry.getValue().asInt())));
        for (JsonNode choice : alternative.path("choices")) {
            int amount = choice.path("amount").asInt(1);
            int count = choice.path("count").asInt(1);
            List<String> from = strings(choice.get("from"));
            String target = from.isEmpty() || from.size() == 6
                    ? (count == 1 ? "one ability" : count + " abilities") + " of your choice"
                    : (count == 1 ? "one of " : count + " of ") + from.stream().map(ability -> ABILITY_ABBREVIATIONS.getOrDefault(ability, ability)).collect(Collectors.joining(", "));
            parts.add(signed(amount) + " to " + target);
        }
        return listOrNull(parts);
    }

    static String speed(JsonNode speed) {
        if (speed == null || speed.isNull() || !speed.has("walk")) {
            return null;
        }
        List<String> parts = new ArrayList<>(List.of("Speed " + speed.get("walk").asInt() + " ft"));
        speed.properties().forEach(entry -> {
            if (!entry.getKey().equals("walk")) {
                JsonNode value = entry.getValue();
                parts.add(entry.getKey() + (value.isNumber() ? " " + value.asInt() + " ft" : ""));
            }
        });
        return String.join(", ", parts);
    }

    static String senses(JsonNode senses) {
        List<String> parts = new ArrayList<>();
        List<JsonNode> list = senses == null ? List.of() : senses.isArray() ? StreamSupport.stream(senses.spliterator(), false).toList() : List.of(senses);
        for (JsonNode sense : list) {
            if (sense.hasNonNull("type")) {
                parts.add(capitalize(sense.get("type").asString()) + (sense.hasNonNull("range") ? " " + sense.get("range").asInt() + " ft" : ""));
            }
        }
        return listOrNull(parts);
    }

    /** Spells a species grants from level 1, e.g. {@code "produce flame#c"} → "Produce Flame". */
    private static String knownSpells(JsonNode additionalSpells) {
        JsonNode block = additionalSpells != null && additionalSpells.isArray() ? first(additionalSpells) : additionalSpells;
        if (block == null || block.isMissingNode() || block.isNull()) {
            return null;
        }
        List<String> names = new ArrayList<>();
        for (String reference : strings(block.path("known").get("1"))) {
            names.add(titleCase(reference.replaceAll("#.*$", "").replaceAll("\\|.*$", "")));
        }
        return listOrNull(names);
    }

    private static String armor(List<String> armor) {
        if (armor.isEmpty()) {
            return null;
        }
        boolean shields = armor.contains("shield");
        List<String> kinds = armor.stream().filter(HEAVIER_ARMOR::contains).toList();
        String body = kinds.containsAll(HEAVIER_ARMOR) ? "All armor" : kinds.isEmpty() ? null : capitalize(String.join(", ", kinds)) + " armor";
        if (body == null) {
            return shields ? "Shields" : null;
        }
        return shields ? body + ", shields" : body;
    }

    private static String weapons(List<String> categories) {
        return categories.isEmpty() ? null : capitalize(String.join(" & ", categories)) + " weapons";
    }

    private String toolName(String slug) {
        if (slug.startsWith("vehicles-")) {
            return "Vehicles (" + titleCase(slug.substring("vehicles-".length())) + ")";
        }
        return catalogue.find("ITEM", slug).map(CatalogueRecord::name).orElse(titleCase(slug));
    }

    private static JsonNode first(JsonNode array) {
        return array != null && array.isArray() && !array.isEmpty() ? array.get(0) : MissingNode.getInstance();
    }

    private static List<String> strings(JsonNode array) {
        if (array == null || !array.isArray()) {
            return List.of();
        }
        return StreamSupport.stream(array.spliterator(), false).filter(JsonNode::isString).map(JsonNode::asString).toList();
    }

    private static String listOrNull(List<String> parts) {
        List<String> present = parts.stream().filter(part -> part != null && !part.isBlank()).toList();
        return present.isEmpty() ? null : String.join(", ", present);
    }

    private static String joined(String... parts) {
        String line = Arrays.stream(parts).filter(part -> part != null && !part.isBlank()).collect(Collectors.joining(SEPARATOR));
        return line.isEmpty() ? null : line;
    }

    private static String signed(int value) {
        return value >= 0 ? "+" + value : String.valueOf(value);
    }

    private static String ordinal(int level) {
        return switch (level) {
            case 1 -> "1st";
            case 2 -> "2nd";
            case 3 -> "3rd";
            default -> level + "th";
        };
    }

    private static String capitalize(String text) {
        return Dnd5eChoiceOptions.capitalize(text);
    }

    private static String titleCase(String text) {
        return Arrays.stream(text.replace('-', ' ').split(" "))
                .filter(word -> !word.isEmpty())
                .map(Dnd5eOptionSummaries::capitalize)
                .collect(Collectors.joining(" "));
    }
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.shared.InterfaceLocale;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.MissingNode;

/**
 * What a builder option gives, in one line built from its catalogue data: "CON +2 · Speed 25 ft ·
 * Darkvision 60 ft". Feats and optional features also quote their first benefit sentence, unless
 * the catalogue's prose is redacted (adr-0005). Words come from {@link Dnd5eText}, game terms from its term list.
 */
final class Dnd5eOptionSummaries {

    private static final String SEPARATOR = " · ";
    private static final String COUNT = "count";
    private static final int MAX_SENTENCE = 220;
    private static final Pattern BENEFITS_LEAD = Pattern.compile("(?i)(following benefits|seguintes benefícios):\\s*");
    private static final Pattern PREREQUISITE_LEAD = Pattern.compile("^(Prerequisite|Pré-requisito):[^\\n]*\\n+");
    private static final Pattern FIRST_SENTENCE = Pattern.compile("^(.+?[.!?])(\\s|$)", Pattern.DOTALL);
    private static final Map<String, String> ABILITY_ABBREVIATIONS = Map.of(
            "strength", "STR", "dexterity", "DEX", "constitution", "CON",
            "intelligence", "INT", "wisdom", "WIS", "charisma", "CHA");
    private static final Map<String, String> TOOL_CATEGORY_LABELS = Map.of(
            "gamingSet", "gaming set", "musicalInstrument", "musical instrument",
            "artisansTool", "artisan's tools", "any", "tool");
    private static final Map<String, String> MOVEMENT_TERMS = Map.of(
            "fly", "flying", "swim", "swimming", "climb", "climbing", "burrow", "burrowing");
    private static final List<String> HEAVIER_ARMOR = List.of("light", "medium", "heavy");

    private final CatalogueLookup catalogue;
    private final boolean includeProse;
    private final Dnd5eText text;

    Dnd5eOptionSummaries(CatalogueLookup catalogue, boolean includeProse) {
        this(catalogue, includeProse, Dnd5eText.ENGLISH);
    }

    Dnd5eOptionSummaries(CatalogueLookup catalogue, boolean includeProse, Dnd5eText text) {
        this.catalogue = catalogue;
        this.includeProse = includeProse;
        this.text = text;
    }

    /** The words and game terms these summaries are written in. */
    Dnd5eText text() {
        return text;
    }

    /** The summary of a catalogue entry offered as an option; null for kinds that keep a plain line (items). */
    String of(CatalogueRecord record) {
        Locale locale = InterfaceLocale.current();
        return switch (record.kind()) {
            case "SPECIES" -> species(record.data());
            case "BACKGROUND" -> background(record);
            case "CLASS" -> playerClass(record.data());
            case "SUBCLASS" -> subclass(record);
            case "FEAT" -> joined(abilityIncreases(record.data().get("abilityAlternatives")), prose(record.localizedDescription(locale)));
            case "OPTIONAL_FEATURE" -> prose(record.localizedDescription(locale));
            case "SPELL" -> spell(record.data());
            case "LANGUAGE" -> language(record.data());
            default -> null;
        };
    }

    /** A species, subspecies or variant node: its ability increases, speed, senses, resistances and level-1 spells. */
    String species(JsonNode node) {
        List<String> parts = new ArrayList<>();
        parts.add(abilityIncreases(node.get("abilityAlternatives")));
        String speed = speedValue(node.get("speed"));
        parts.add(speed == null ? null : text.term("labels", "speed", "Speed") + " " + speed);
        parts.add(senses(node.get("senses")));
        List<String> resistances = strings(node.path("damageResistances").get("fixed")).stream()
                .map(type -> text.term("damageTypes", type, type)).toList();
        if (!resistances.isEmpty()) {
            parts.add(capitalize(text.text("summary.resistance", String.join(", ", resistances))));
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

    private String background(CatalogueRecord record) {
        JsonNode data = record.data();
        UnaryOperator<String> translate = record.texts(InterfaceLocale.current());
        List<String> features = StreamSupport.stream(data.path("features").spliterator(), false)
                .map(feature -> feature.path("name").asString(null)).filter(name -> name != null).map(translate).toList();
        return joined(skills(data.get("skillAlternatives")), tools(data.get("toolAlternatives")), languages(data.get("languageAlternatives")),
                listOrNull(features));
    }

    /** "Athletics, Intimidation", plus any skills of the player's choice. */
    String skills(JsonNode alternatives) {
        JsonNode skills = alternative(alternatives);
        List<String> parts = new ArrayList<>(strings(skills.get("fixed")).stream()
                .map(key -> text.term("skills", key, Dnd5eSkills.LABELS.getOrDefault(key, key))).toList());
        for (JsonNode choice : skills.path("choices")) {
            parts.add(text.text("summary.skillsOfChoice", choice.path(COUNT).asInt(1)));
        }
        return listOrNull(parts);
    }

    /** "Vehicles (Land), 1 gaming set". */
    String tools(JsonNode alternatives) {
        JsonNode tools = alternative(alternatives);
        List<String> parts = new ArrayList<>(strings(tools.get("fixed")).stream().map(this::toolName).toList());
        for (JsonNode choice : tools.path("choices")) {
            String key = TOOL_CATEGORY_LABELS.containsKey(choice.path("category").asString("")) ? choice.path("category").asString() : "any";
            parts.add(choice.path(COUNT).asInt(1) + " " + text.term("toolCategories", key, TOOL_CATEGORY_LABELS.get(key)));
        }
        return listOrNull(parts);
    }

    /** "Common, Dwarvish", plus any languages of the player's choice. */
    String languages(JsonNode alternatives) {
        JsonNode languages = alternative(alternatives);
        List<String> parts = new ArrayList<>(strings(languages.get("fixed")).stream().map(Dnd5eOptionSummaries::capitalize).toList());
        for (JsonNode choice : languages.path("choices")) {
            parts.add(text.text("summary.languagesOfChoice", choice.path(COUNT).asInt(1)));
        }
        return listOrNull(parts);
    }

    /** A block's first alternative: 5etools writes some as a list of alternatives, others as the one block. */
    private static JsonNode alternative(JsonNode alternatives) {
        if (alternatives == null) {
            return MissingNode.getInstance();
        }
        return alternatives.isArray() ? first(alternatives) : alternatives;
    }

    private String playerClass(JsonNode data) {
        String hitDie = data.hasNonNull("hitDie") ? "d" + data.get("hitDie").asInt() : null;
        List<String> saves = strings(data.get("savingThrows")).stream().map(this::abbreviation).toList();
        JsonNode proficiencies = data.path("proficiencies");
        return joined(hitDie, saves.isEmpty() ? null : text.text("summary.saves", String.join(", ", saves)),
                armor(strings(proficiencies.get("armor"))), weapons(strings(proficiencies.get("weaponCategories"))));
    }

    /** The features a subclass grants at its first level, leaving out the "… Options" lists that only index choices. */
    private String subclass(CatalogueRecord record) {
        JsonNode data = record.data();
        int firstLevel = StreamSupport.stream(data.path("features").spliterator(), false)
                .mapToInt(feature -> feature.path("level").asInt(Integer.MAX_VALUE)).min().orElse(Integer.MAX_VALUE);
        if (firstLevel == Integer.MAX_VALUE) {
            return null;
        }
        UnaryOperator<String> translate = record.texts(InterfaceLocale.current());
        List<String> names = StreamSupport.stream(data.path("features").spliterator(), false)
                .filter(feature -> feature.path("level").asInt() == firstLevel)
                .map(feature -> feature.path("name").asString(""))
                .filter(name -> !name.isEmpty() && !name.endsWith(" Options"))
                .map(translate)
                .toList();
        return names.isEmpty() ? null : text.text("summary.levelFeatures", String.valueOf(firstLevel), String.join(", ", names));
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

    /** Every alternative set of ability increases, joined with "or". */
    String abilityIncreases(JsonNode alternatives) {
        if (alternatives == null || alternatives.isMissingNode() || alternatives.isNull()) {
            return null;
        }
        List<JsonNode> all = alternatives.isArray() ? StreamSupport.stream(alternatives.spliterator(), false).toList() : List.of(alternatives);
        List<String> described = all.stream().map(this::abilityIncrease).filter(Objects::nonNull).toList();
        return described.isEmpty() ? null : String.join(" " + text.text("plan.or") + " ", described);
    }

    /** One alternative set of ability increases: its fixed bonuses, then each choice. */
    String abilityIncrease(JsonNode alternative) {
        if (alternative == null || alternative.isMissingNode() || alternative.isNull()) {
            return null;
        }
        List<String> parts = new ArrayList<>();
        alternative.path("fixed").properties().forEach(entry ->
                parts.add(abbreviation(entry.getKey()) + " " + signed(entry.getValue().asInt())));
        for (JsonNode choice : alternative.path("choices")) {
            parts.add(abilityChoice(choice));
        }
        return listOrNull(parts);
    }

    private String abilityChoice(JsonNode choice) {
        List<Integer> weights = StreamSupport.stream(choice.path("weights").spliterator(), false).map(JsonNode::asInt).toList();
        int count = weights.isEmpty() ? choice.path(COUNT).asInt(1) : weights.size();
        List<String> from = strings(choice.get("from"));
        boolean anyAbility = from.isEmpty() || from.size() == 6;
        if (weights.stream().distinct().count() > 1) {
            String bonuses = weights.stream().map(Dnd5eOptionSummaries::signed).collect(Collectors.joining(" " + text.text("summary.and") + " "));
            return anyAbility
                    ? text.text("summary.abilityWeightedAny", bonuses, count)
                    : text.text("summary.abilityWeightedFrom", bonuses, count, abbreviations(from));
        }
        String amount = signed(weights.isEmpty() ? choice.path("amount").asInt(1) : weights.getFirst());
        if (anyAbility) {
            return count == 1 ? text.text("summary.abilityOneAny", amount) : text.text("summary.abilityManyAny", amount, count);
        }
        return count == 1 ? text.text("summary.abilityOneFrom", amount, abbreviations(from))
                : text.text("summary.abilityManyFrom", amount, count, abbreviations(from));
    }

    private String abbreviations(List<String> abilities) {
        return abilities.stream().map(this::abbreviation).collect(Collectors.joining(", "));
    }

    private String abbreviation(String ability) {
        return text.term("abilityAbbreviations", ability, ABILITY_ABBREVIATIONS.getOrDefault(ability, ability));
    }

    /** "25 ft, fly 25 ft": walking speed first, then any other movement (a movement without a number equals walking). */
    String speedValue(JsonNode speed) {
        if (speed == null || speed.isNull() || !speed.has("walk")) {
            return null;
        }
        List<String> parts = new ArrayList<>(List.of(text.distance(speed.get("walk").asInt())));
        speed.properties().forEach(entry -> {
            if (!entry.getKey().equals("walk")) {
                JsonNode value = entry.getValue();
                String movement = text.term("movements", MOVEMENT_TERMS.getOrDefault(entry.getKey(), entry.getKey()), entry.getKey());
                parts.add(movement + (value.isNumber() ? " " + text.distance(value.asInt()) : ""));
            }
        });
        return String.join(", ", parts);
    }

    String senses(JsonNode senses) {
        List<String> parts = new ArrayList<>();
        for (JsonNode sense : senseList(senses)) {
            if (sense.hasNonNull("type")) {
                String type = sense.get("type").asString();
                String name = text.term("senses", type.toUpperCase(Locale.ROOT), capitalize(type));
                parts.add(name + (sense.hasNonNull("range") ? " " + text.distance(sense.get("range").asInt()) : ""));
            }
        }
        return listOrNull(parts);
    }

    private static List<JsonNode> senseList(JsonNode senses) {
        if (senses == null) {
            return List.of();
        }
        return senses.isArray() ? StreamSupport.stream(senses.spliterator(), false).toList() : List.of(senses);
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

    /** "All armor, shields" or "Light, medium armor"; armor kinds and shields come from the term list. */
    private String armor(List<String> armor) {
        if (armor.isEmpty()) {
            return null;
        }
        String shields = armor.contains("shield") ? text.term("armorCategories", "shield", "shields") : null;
        List<String> kinds = armor.stream().filter(HEAVIER_ARMOR::contains).toList();
        String body = kinds.containsAll(HEAVIER_ARMOR) ? text.text("summary.allArmor")
                : kinds.isEmpty() ? null
                : text.text("summary.armor", kinds.stream().map(kind -> text.term("armorCategories", kind, kind)).collect(Collectors.joining(", ")));
        if (body == null) {
            return shields == null ? null : capitalize(shields);
        }
        return capitalize(shields == null ? body : body + ", " + shields);
    }

    private String weapons(List<String> categories) {
        if (categories.isEmpty()) {
            return null;
        }
        String joined = categories.stream().map(category -> text.term("weaponCategories", category, category))
                .collect(Collectors.joining(" " + text.text("summary.weaponJoin") + " "));
        return capitalize(text.text("summary.weapons", joined));
    }

    private String toolName(String slug) {
        if (slug.startsWith("vehicles-")) {
            String kind = slug.substring("vehicles-".length());
            return text.term("vehicles", kind, "Vehicles (" + titleCase(kind) + ")");
        }
        return catalogue.find("ITEM", slug).map(item -> item.localizedName(InterfaceLocale.current())).orElse(titleCase(slug));
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

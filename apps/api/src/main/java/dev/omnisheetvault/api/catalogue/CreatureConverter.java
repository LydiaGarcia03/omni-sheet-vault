package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools monster from the 2014 Monster Manual ({@code bestiary/bestiary-mm.json}) into a
 * {@code CREATURE} catalogue entry whose data matches an extra's stat block on the sheet: size, type,
 * alignment, armor class, hit points and hit dice, speeds, ability scores with saves, skills, senses,
 * languages, challenge rating, defenses, and named traits, actions, reactions and legendary actions.
 * Spellcasting blocks become a trait (or an action, when 5etools displays them as one). Only the Monster
 * Manual is read; the other bestiaries are adventure and setting books.
 */
final class CreatureConverter implements FiveEToolsConverter {

    private static final String MONSTER_MANUAL_FILE = "bestiary/bestiary-mm.json";
    private static final String ALIGNMENT = "alignment";
    private static final String FAMILIAR = "familiar";
    private static final String SPECIAL = "special";
    private static final int DEFAULT_ARMOR_CLASS = 10;
    private static final Map<String, String> SIZES = Map.of(
            "T", "Tiny", "S", "Small", "M", "Medium", "L", "Large", "H", "Huge", "G", "Gargantuan");
    private static final List<String> ABILITIES = List.of("str", "dex", "con", "int", "wis", "cha");
    private static final Map<String, String> ABILITY_NAMES = Map.of(
            "str", "strength", "dex", "dexterity", "con", "constitution",
            "int", "intelligence", "wis", "wisdom", "cha", "charisma");
    private static final List<String> EXTRA_SPEEDS = List.of("burrow", "climb", "fly", "swim");
    private static final List<String> SPELL_LEVEL_NAMES = List.of(
            "Cantrips", "1st level", "2nd level", "3rd level", "4th level", "5th level",
            "6th level", "7th level", "8th level", "9th level");

    private final ObjectMapper objectMapper;

    CreatureConverter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.CREATURE;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        List<JsonNode> monsters = new ArrayList<>();
        if (dataSource.hasDataFile(MONSTER_MANUAL_FILE)) {
            JsonNode array = dataSource.readDataFile(MONSTER_MANUAL_FILE).get("monster");
            if (array != null) {
                array.forEach(monsters::add);
            }
        }
        return monsters;
    }

    @Override
    public CatalogueEntryImport convert(JsonNode monster) {
        String name = monster.get("name").asString();
        ObjectNode data = objectMapper.createObjectNode();
        data.put("size", sizes(monster.get("size")));
        data.put("creatureType", creatureType(monster.get("type")));
        data.put(ALIGNMENT, Alignments.text(monster.get(ALIGNMENT)));
        putArmorClass(data, monster.get("ac"));
        putHitPoints(data, monster.get("hp"));
        putSpeeds(data, monster.get("speed"));
        data.set("abilityScores", abilityScores(monster));
        data.set("skills", skills(monster.get("skill")));
        data.put("senses", senses(monster));
        data.put("languages", languages(monster.get("languages")));
        data.put("challengeRating", challengeRating(monster.get("cr")));
        data.put("damageVulnerabilities", defenses(monster.get("vulnerable"), "vulnerable"));
        data.put("damageResistances", defenses(monster.get("resist"), "resist"));
        data.put("damageImmunities", defenses(monster.get("immune"), "immune"));
        data.put("conditionImmunities", defenses(monster.get("conditionImmune"), "conditionImmune"));
        ArrayNode traits = statEntries(monster.get("trait"));
        ArrayNode actions = statEntries(monster.get("action"));
        addSpellcasting(monster.get("spellcasting"), traits, actions);
        data.set("traits", traits);
        data.set("actions", actions);
        data.set("reactions", statEntries(monster.get("reaction")));
        data.set("legendaryActions", statEntries(monster.get("legendary")));
        data.put(FAMILIAR, monster.path(FAMILIAR).asBoolean(false));
        ArrayNode environments = data.putArray("environments");
        monster.path("environment").forEach(environment -> environments.add(environment.asString()));
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.CREATURE,
                FiveEToolsNaming.slug(name),
                name,
                monster.get("source").asString(),
                monster.get("page") == null || !monster.get("page").isNumber() ? null : monster.get("page").asInt(),
                tags(monster),
                null,
                data);
    }

    private static String sizes(JsonNode size) {
        if (size == null || !size.isArray()) {
            return "Medium";
        }
        List<String> names = new ArrayList<>();
        size.forEach(code -> names.add(SIZES.getOrDefault(code.asString(), code.asString())));
        return String.join(" or ", names);
    }

    /** "Beast", "Humanoid (any race)", "Swarm of Tiny beasts", "Fiend (demon, shapechanger)". */
    static String creatureType(JsonNode type) {
        if (type == null || type.isNull()) {
            return "";
        }
        if (type.isString()) {
            return capitalize(type.asString());
        }
        String typeName = baseTypeName(type.get("type"));
        if (type.has("swarmSize")) {
            return "Swarm of " + SIZES.getOrDefault(type.get("swarmSize").asString(), "") + " " + typeName + "s";
        }
        List<String> tags = new ArrayList<>();
        type.path("tags").forEach(tag -> tags.add(tag.isString() ? tag.asString()
                : (tag.path("prefix").asString("") + " " + tag.path("tag").asString("")).trim()));
        return capitalize(typeName) + (tags.isEmpty() ? "" : " (" + String.join(", ", tags) + ")");
    }

    private static String baseTypeName(JsonNode base) {
        if (base == null) {
            return "";
        }
        if (base.isObject() && base.has("choose")) {
            return stream(base.get("choose")).map(JsonNode::asString).collect(Collectors.joining(" or "));
        }
        return base.asString();
    }

    /** The first armor class is the number; the text keeps every alternative with its source ("15 (natural armor)"). */
    private static void putArmorClass(ObjectNode data, JsonNode ac) {
        List<JsonNode> entries = ac != null && ac.isArray() ? stream(ac).toList() : List.of();
        int armorClass = entries.isEmpty() ? DEFAULT_ARMOR_CLASS : armorClassNumber(entries.getFirst());
        List<String> parts = entries.stream().map(CreatureConverter::armorClassPart).filter(Objects::nonNull).toList();
        data.put("armorClass", armorClass);
        data.put("armorClassText", parts.isEmpty() ? String.valueOf(armorClass) : String.join(", ", parts));
    }

    private static int armorClassNumber(JsonNode entry) {
        if (entry.isNumber()) {
            return entry.asInt();
        }
        return entry.has("ac") ? entry.get("ac").asInt() : DEFAULT_ARMOR_CLASS;
    }

    private static String armorClassPart(JsonNode entry) {
        if (entry.isNumber()) {
            return String.valueOf(entry.asInt());
        }
        if (entry.has("ac")) {
            return entry.get("ac").asInt() + armorSources(entry) + spacedNote(entry, "condition");
        }
        return entry.has(SPECIAL) ? TagMarkupStripper.strip(entry.get(SPECIAL).asString()) : null;
    }

    private static String armorSources(JsonNode entry) {
        if (!entry.has("from")) {
            return "";
        }
        return " (" + stream(entry.get("from")).map(node -> TagMarkupStripper.strip(node.asString())).collect(Collectors.joining(", ")) + ")";
    }

    /** The field's text after a space, or nothing when the node lacks it. */
    private static String spacedNote(JsonNode node, String field) {
        return node.has(field) ? " " + TagMarkupStripper.strip(node.get(field).asString()) : "";
    }

    private static void putHitPoints(ObjectNode data, JsonNode hp) {
        if (hp != null && hp.has("average")) {
            data.put("hitPoints", hp.get("average").asInt());
            data.put("hitDice", hp.path("formula").asString(""));
        } else {
            data.put("hitPoints", 0);
            data.put("hitDice", hp == null ? "" : TagMarkupStripper.strip(hp.path(SPECIAL).asString("")));
        }
    }

    /** The walking speed as a number; every other speed as text ("Climb 30 ft., Fly 60 ft. (hover)"). */
    private static void putSpeeds(ObjectNode data, JsonNode speed) {
        data.put("speed", speed == null ? 0 : speedFeet(speed.get("walk")));
        List<String> others = speed == null ? List.of()
                : EXTRA_SPEEDS.stream().filter(speed::has).map(mode -> extraSpeed(speed, mode)).toList();
        data.put("additionalSpeeds", others.isEmpty() ? null : String.join(", ", others));
    }

    private static String extraSpeed(JsonNode speed, String mode) {
        JsonNode value = speed.get(mode);
        String condition = spacedNote(value, "condition");
        String hover = "fly".equals(mode) && speed.path("canHover").asBoolean(false) && condition.isEmpty() ? " (hover)" : "";
        return capitalize(mode) + " " + speedFeet(value) + " ft." + condition + hover;
    }

    private static int speedFeet(JsonNode value) {
        if (value == null) {
            return 0;
        }
        return value.isObject() ? value.path("number").asInt(0) : value.asInt(0);
    }

    /** Each score with its saving throw: the listed save bonus, or the plain modifier. */
    private ArrayNode abilityScores(JsonNode monster) {
        ArrayNode scores = objectMapper.createArrayNode();
        JsonNode saves = monster.get("save");
        for (String ability : ABILITIES) {
            int score = monster.path(ability).asInt(10);
            int modifier = Math.floorDiv(score - 10, 2);
            int save = saves != null && saves.has(ability) ? signedNumber(saves.get(ability).asString(), modifier) : modifier;
            scores.addObject().put("abilityKey", ABILITY_NAMES.get(ability)).put("score", score).put("save", save);
        }
        return scores;
    }

    private ArrayNode skills(JsonNode skill) {
        ArrayNode skills = objectMapper.createArrayNode();
        if (skill == null) {
            return skills;
        }
        for (Map.Entry<String, JsonNode> entry : skill.properties()) {
            if (entry.getValue().isString()) {
                skills.addObject().put("name", titleCase(entry.getKey())).put("bonus", signedNumber(entry.getValue().asString(), 0));
            }
        }
        return skills;
    }

    private static String senses(JsonNode monster) {
        List<String> senses = new ArrayList<>();
        monster.path("senses").forEach(sense -> senses.add(TagMarkupStripper.strip(sense.asString())));
        if (monster.has("passive")) {
            senses.add("passive Perception " + monster.get("passive").asString());
        }
        return String.join(", ", senses);
    }

    private static String languages(JsonNode languages) {
        if (languages == null || languages.isEmpty()) {
            return "—";
        }
        return stream(languages).map(language -> TagMarkupStripper.strip(language.asString())).collect(Collectors.joining(", "));
    }

    private static String challengeRating(JsonNode cr) {
        if (cr == null) {
            return "—";
        }
        return cr.isObject() ? cr.path("cr").asString("—") : cr.asString();
    }

    /** "poison; bludgeoning, piercing, and slashing from nonmagical attacks" — 5etools' groups, each with its note. */
    static String defenses(JsonNode list, String innerField) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        List<String> plain = stream(list).filter(JsonNode::isString).map(entry -> TagMarkupStripper.strip(entry.asString())).toList();
        List<String> all = new ArrayList<>();
        if (!plain.isEmpty()) {
            all.add(String.join(", ", plain));
        }
        stream(list).filter(entry -> !entry.isString()).map(entry -> defenseGroup(entry, innerField)).filter(Objects::nonNull).forEach(all::add);
        return all.isEmpty() ? null : String.join("; ", all);
    }

    private static String defenseGroup(JsonNode entry, String innerField) {
        if (entry.has(SPECIAL)) {
            return TagMarkupStripper.strip(entry.get(SPECIAL).asString());
        }
        String inner = entry.has(innerField) ? joinedWithAnd(entry.get(innerField)) : null;
        if (inner == null) {
            return null;
        }
        String preNote = entry.has("preNote") ? TagMarkupStripper.strip(entry.get("preNote").asString()) + " " : "";
        return preNote + inner + spacedNote(entry, "note");
    }

    /** A noted group reads as a phrase: "bludgeoning, piercing, and slashing". */
    private static String joinedWithAnd(JsonNode list) {
        List<String> names = stream(list).filter(JsonNode::isString).map(node -> TagMarkupStripper.strip(node.asString())).toList();
        if (names.isEmpty()) {
            return null;
        }
        if (names.size() <= 2) {
            return String.join(" and ", names);
        }
        return String.join(", ", names.subList(0, names.size() - 1)) + ", and " + names.getLast();
    }

    private ArrayNode statEntries(JsonNode entries) {
        ArrayNode result = objectMapper.createArrayNode();
        if (entries == null) {
            return result;
        }
        for (JsonNode entry : entries) {
            String name = TagMarkupStripper.strip(entry.path("name").asString(""));
            result.addObject().put("name", name).put("description", TagMarkupStripper.strip(FiveEToolsEntries.flatten(entry.get("entries"))));
        }
        return result;
    }

    /** "At will: mage hand; 3/day each: fog cloud; 1st level (4 slots): shield", under the block's own header. */
    private void addSpellcasting(JsonNode spellcasting, ArrayNode traits, ArrayNode actions) {
        if (spellcasting == null) {
            return;
        }
        for (JsonNode block : spellcasting) {
            ObjectNode entry = ("action".equals(block.path("displayAs").asString("")) ? actions : traits).addObject();
            entry.put("name", TagMarkupStripper.strip(block.path("name").asString("Spellcasting")));
            entry.put("description", String.join("\n", spellcastingLines(block)));
        }
    }

    private static List<String> spellcastingLines(JsonNode block) {
        List<String> lines = new ArrayList<>();
        addFlattened(lines, block.get("headerEntries"));
        if (block.has("will")) {
            lines.add("At will: " + spellNames(block.get("will")));
        }
        block.path("daily").properties().forEach(daily -> lines.add(dailySpellsLine(daily)));
        block.path("spells").properties().forEach(level -> lines.add(spellLevelLine(level)));
        addFlattened(lines, block.get("footerEntries"));
        return lines;
    }

    private static void addFlattened(List<String> lines, JsonNode entries) {
        String text = TagMarkupStripper.strip(FiveEToolsEntries.flatten(entries));
        if (!text.isBlank()) {
            lines.add(text);
        }
    }

    private static String dailySpellsLine(Map.Entry<String, JsonNode> daily) {
        String key = daily.getKey();
        return key.replace("e", "") + "/day" + (key.endsWith("e") ? " each" : "") + ": " + spellNames(daily.getValue());
    }

    private static String spellLevelLine(Map.Entry<String, JsonNode> level) {
        int levelNumber = Integer.parseInt(level.getKey());
        JsonNode spells = level.getValue();
        return SPELL_LEVEL_NAMES.get(levelNumber) + slotsNote(spells, levelNumber) + ": " + spellNames(spells.get("spells"));
    }

    private static String slotsNote(JsonNode spells, int levelNumber) {
        if (spells.has("slots")) {
            int slots = spells.get("slots").asInt();
            return " (" + slots + (slots == 1 ? " slot)" : " slots)");
        }
        return levelNumber == 0 ? " (at will)" : "";
    }

    private static String spellNames(JsonNode spells) {
        if (spells == null) {
            return "";
        }
        return stream(spells).map(spell -> TagMarkupStripper.strip(spell.isString() ? spell.asString() : spell.path("entry").asString("")))
                .collect(Collectors.joining(", "));
    }

    /** "familiar" for the ones Find Familiar can summon, plus each environment the creature lives in. */
    private static List<String> tags(JsonNode monster) {
        Set<String> tags = new LinkedHashSet<>();
        if (monster.path(FAMILIAR).asBoolean(false)) {
            tags.add(FAMILIAR);
        }
        monster.path("environment").forEach(environment -> tags.add(environment.asString()));
        return List.copyOf(tags);
    }

    /** "+5" → 5, "-1" → -1; text that isn't a plain signed number keeps {@code fallback}. */
    private static int signedNumber(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim().replace("+", ""));
        } catch (NumberFormatException _) {
            return fallback;
        }
    }

    private static String capitalize(String text) {
        return text == null || text.isEmpty() ? "" : text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }

    private static String titleCase(String text) {
        return Arrays.stream(text.split(" ")).map(CreatureConverter::capitalize).collect(Collectors.joining(" "));
    }

    private static Stream<JsonNode> stream(JsonNode array) {
        return StreamSupport.stream(array.spliterator(), false);
    }

    /** 5etools' alignment codes as words: "chaotic evil", "unaligned", "any non-good alignment". */
    static final class Alignments {

        private static final String NEUTRAL = "neutral";
        private static final String ALIGNMENT_SUFFIX = " " + ALIGNMENT;
        private static final Map<String, String> WORDS = Map.of(
                "L", "lawful", "N", NEUTRAL, "NX", NEUTRAL, "NY", NEUTRAL, "C", "chaotic", "G", "good", "E", "evil");
        private static final Set<String> LAW_AXIS = Set.of("L", "NX", "C");
        private static final Set<String> MORAL_AXIS = Set.of("G", "NY", "E");

        private Alignments() {
        }

        private static String outside(Set<String> codes, Set<String> axis) {
            return codes.stream().filter(code -> !axis.contains(code)).findFirst().orElse("");
        }

        static String text(JsonNode alignment) {
            if (alignment == null || alignment.isEmpty()) {
                return "unaligned";
            }
            if (alignment.get(0).isObject()) {
                return stream(alignment).map(Alignments::alternative).collect(Collectors.joining(" or "));
            }
            return codes(stream(alignment).map(JsonNode::asString).toList());
        }

        private static String alternative(JsonNode entry) {
            if (entry.has(SPECIAL)) {
                return TagMarkupStripper.strip(entry.get(SPECIAL).asString());
            }
            String text = codes(stream(entry.path(ALIGNMENT)).map(JsonNode::asString).toList());
            return entry.has("chance") ? text + " (" + entry.get("chance").asInt() + "%)" : text;
        }

        static String codes(List<String> codes) {
            Set<String> set = Set.copyOf(codes);
            if (set.contains("A")) {
                return "any alignment";
            }
            if (set.contains("U")) {
                return "unaligned";
            }
            if (set.size() == 4 && set.containsAll(LAW_AXIS)) {
                return "any " + WORDS.get(outside(set, LAW_AXIS)) + ALIGNMENT_SUFFIX;
            }
            if (set.size() == 4 && set.containsAll(MORAL_AXIS)) {
                return "any " + WORDS.get(outside(set, MORAL_AXIS)) + ALIGNMENT_SUFFIX;
            }
            if (set.size() == 5) {
                String missing = List.of("L", "C", "G", "E").stream().filter(code -> !set.contains(code)).findFirst().orElse("");
                return "any non-" + WORDS.getOrDefault(missing, "") + ALIGNMENT_SUFFIX;
            }
            if (set.equals(Set.of("N"))) {
                return NEUTRAL;
            }
            return codes.stream().map(code -> WORDS.getOrDefault(code, code)).distinct().collect(Collectors.joining(" "));
        }
    }
}

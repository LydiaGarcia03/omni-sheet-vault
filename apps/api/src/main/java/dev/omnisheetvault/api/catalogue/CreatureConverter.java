package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
        data.put("alignment", Alignments.text(monster.get("alignment")));
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
        data.put("familiar", monster.path("familiar").asBoolean(false));
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
        JsonNode base = type.get("type");
        String typeName = base != null && base.isObject() && base.has("choose")
                ? stream(base.get("choose")).map(JsonNode::asString).collect(Collectors.joining(" or "))
                : base == null ? "" : base.asString();
        if (type.has("swarmSize")) {
            return "Swarm of " + SIZES.getOrDefault(type.get("swarmSize").asString(), "") + " " + typeName + "s";
        }
        List<String> tags = new ArrayList<>();
        type.path("tags").forEach(tag -> tags.add(tag.isString() ? tag.asString()
                : (tag.path("prefix").asString("") + " " + tag.path("tag").asString("")).trim()));
        return capitalize(typeName) + (tags.isEmpty() ? "" : " (" + String.join(", ", tags) + ")");
    }

    /** The first armor class is the number; the text keeps every alternative with its source ("15 (natural armor)"). */
    private static void putArmorClass(ObjectNode data, JsonNode ac) {
        int armorClass = 10;
        List<String> parts = new ArrayList<>();
        if (ac != null && ac.isArray()) {
            for (JsonNode entry : ac) {
                if (entry.isNumber()) {
                    parts.add(String.valueOf(entry.asInt()));
                } else if (entry.has("ac")) {
                    String from = entry.has("from") ? " (" + stream(entry.get("from")).map(node -> TagMarkupStripper.strip(node.asString()))
                            .collect(Collectors.joining(", ")) + ")" : "";
                    String condition = entry.has("condition") ? " " + TagMarkupStripper.strip(entry.get("condition").asString()) : "";
                    parts.add(entry.get("ac").asInt() + from + condition);
                } else if (entry.has("special")) {
                    parts.add(TagMarkupStripper.strip(entry.get("special").asString()));
                }
            }
            JsonNode first = ac.isEmpty() ? null : ac.get(0);
            if (first != null && first.isNumber()) {
                armorClass = first.asInt();
            } else if (first != null && first.has("ac")) {
                armorClass = first.get("ac").asInt();
            }
        }
        data.put("armorClass", armorClass);
        data.put("armorClassText", parts.isEmpty() ? String.valueOf(armorClass) : String.join(", ", parts));
    }

    private static void putHitPoints(ObjectNode data, JsonNode hp) {
        if (hp != null && hp.has("average")) {
            data.put("hitPoints", hp.get("average").asInt());
            data.put("hitDice", hp.path("formula").asString(""));
        } else {
            data.put("hitPoints", 0);
            data.put("hitDice", hp == null ? "" : TagMarkupStripper.strip(hp.path("special").asString("")));
        }
    }

    /** The walking speed as a number; every other speed as text ("Climb 30 ft., Fly 60 ft. (hover)"). */
    private static void putSpeeds(ObjectNode data, JsonNode speed) {
        data.put("speed", speed == null ? 0 : speedFeet(speed.get("walk")));
        List<String> others = new ArrayList<>();
        if (speed != null) {
            for (String mode : EXTRA_SPEEDS) {
                JsonNode value = speed.get(mode);
                if (value == null) {
                    continue;
                }
                String condition = value.isObject() && value.has("condition") ? " " + TagMarkupStripper.strip(value.get("condition").asString()) : "";
                String hover = "fly".equals(mode) && speed.path("canHover").asBoolean(false) && condition.isEmpty() ? " (hover)" : "";
                others.add(capitalize(mode) + " " + speedFeet(value) + " ft." + condition + hover);
            }
        }
        data.put("additionalSpeeds", others.isEmpty() ? null : String.join(", ", others));
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
        List<String> plain = new ArrayList<>();
        List<String> groups = new ArrayList<>();
        for (JsonNode entry : list) {
            if (entry.isString()) {
                plain.add(TagMarkupStripper.strip(entry.asString()));
            } else if (entry.has("special")) {
                groups.add(TagMarkupStripper.strip(entry.get("special").asString()));
            } else if (entry.has(innerField)) {
                String inner = joinedWithAnd(entry.get(innerField));
                String preNote = entry.has("preNote") ? TagMarkupStripper.strip(entry.get("preNote").asString()) + " " : "";
                String note = entry.has("note") ? " " + TagMarkupStripper.strip(entry.get("note").asString()) : "";
                if (inner != null) {
                    groups.add(preNote + inner + note);
                }
            }
        }
        List<String> all = new ArrayList<>();
        if (!plain.isEmpty()) {
            all.add(String.join(", ", plain));
        }
        all.addAll(groups);
        return all.isEmpty() ? null : String.join("; ", all);
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
            List<String> lines = new ArrayList<>();
            String header = TagMarkupStripper.strip(FiveEToolsEntries.flatten(block.get("headerEntries")));
            if (!header.isBlank()) {
                lines.add(header);
            }
            if (block.has("will")) {
                lines.add("At will: " + spellNames(block.get("will")));
            }
            for (Map.Entry<String, JsonNode> daily : block.path("daily").properties()) {
                String key = daily.getKey();
                boolean each = key.endsWith("e");
                lines.add(key.replace("e", "") + "/day" + (each ? " each" : "") + ": " + spellNames(daily.getValue()));
            }
            for (Map.Entry<String, JsonNode> level : block.path("spells").properties()) {
                int levelNumber = Integer.parseInt(level.getKey());
                JsonNode spells = level.getValue();
                String slots = spells.has("slots") ? " (" + spells.get("slots").asInt() + " slot" + (spells.get("slots").asInt() == 1 ? "" : "s") + ")"
                        : levelNumber == 0 ? " (at will)" : "";
                lines.add(SPELL_LEVEL_NAMES.get(levelNumber) + slots + ": " + spellNames(spells.get("spells")));
            }
            String footer = TagMarkupStripper.strip(FiveEToolsEntries.flatten(block.get("footerEntries")));
            if (!footer.isBlank()) {
                lines.add(footer);
            }
            ObjectNode entry = ("action".equals(block.path("displayAs").asString("")) ? actions : traits).addObject();
            entry.put("name", TagMarkupStripper.strip(block.path("name").asString("Spellcasting")));
            entry.put("description", String.join("\n", lines));
        }
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
        if (monster.path("familiar").asBoolean(false)) {
            tags.add("familiar");
        }
        monster.path("environment").forEach(environment -> tags.add(environment.asString()));
        return List.copyOf(tags);
    }

    /** "+5" → 5, "-1" → -1; text that isn't a plain signed number keeps {@code fallback}. */
    private static int signedNumber(String text, int fallback) {
        try {
            return Integer.parseInt(text.trim().replace("+", ""));
        } catch (NumberFormatException e) {
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

        private static final Map<String, String> WORDS = Map.of(
                "L", "lawful", "N", "neutral", "NX", "neutral", "NY", "neutral", "C", "chaotic", "G", "good", "E", "evil");
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
            if (entry.has("special")) {
                return TagMarkupStripper.strip(entry.get("special").asString());
            }
            String text = codes(stream(entry.path("alignment")).map(JsonNode::asString).toList());
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
                return "any " + WORDS.get(outside(set, LAW_AXIS)) + " alignment";
            }
            if (set.size() == 4 && set.containsAll(MORAL_AXIS)) {
                return "any " + WORDS.get(outside(set, MORAL_AXIS)) + " alignment";
            }
            if (set.size() == 5) {
                String missing = List.of("L", "C", "G", "E").stream().filter(code -> !set.contains(code)).findFirst().orElse("");
                return "any non-" + WORDS.getOrDefault(missing, "") + " alignment";
            }
            if (set.equals(Set.of("N"))) {
                return "neutral";
            }
            return codes.stream().map(code -> WORDS.getOrDefault(code, code)).distinct().collect(Collectors.joining(" "));
        }
    }
}

package dev.omnisheetvault.api.catalogue;

import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Maps 5etools proficiency blocks ({@code armor}, {@code weapons},
 * {@code toolProficiencies}, {@code skills}) into this project's catalogue shape.
 * Tool and skill blocks are lists of alternatives: the character gets exactly one of
 * them, and each alternative mixes fixed grants with "choose N" picks.
 */
final class FiveEToolsProficiencies {

    private static final Pattern ITEM_TAG = Pattern.compile("\\{@item ([^|}]+)");
    private static final String ANY_PREFIX = "any";

    private final ObjectMapper objectMapper;

    FiveEToolsProficiencies(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ObjectNode map(JsonNode block) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode armor = result.putArray("armor");
        ArrayNode weaponCategories = result.putArray("weaponCategories");
        ArrayNode weaponItems = result.putArray("weaponItems");
        ArrayNode optionalWeaponCategories = result.putArray("optionalWeaponCategories");
        ArrayNode notes = result.putArray("notes");
        if (block != null) {
            mapArmor(asArray(block.get("armor")), armor, notes);
            mapWeapons(asArray(block.get("weapons")), weaponCategories, weaponItems, optionalWeaponCategories);
        }
        result.set("toolAlternatives", toolAlternatives(block == null ? null : block.get("toolProficiencies")));
        result.set("skillAlternatives", skillAlternatives(block == null ? null : block.get("skills")));
        return result;
    }

    ArrayNode toolAlternatives(JsonNode toolProficiencies) {
        return grantAlternatives(toolProficiencies, FiveEToolsNaming::slug);
    }

    ArrayNode skillAlternatives(JsonNode skills) {
        return grantAlternatives(skills, FiveEToolsNaming::camelCaseKey);
    }

    /**
     * Maps a 5etools grant block (skills, tools, languages, weapons, armor, feats) into
     * alternatives of {@code {fixed: [keys], choices: [choice]}}. A choice is
     * {@code {from, category, fromFilter, count, amount}}: {@code from} lists the keys to
     * pick among; {@code category} is set instead for 5etools' {@code any}/{@code anyX}
     * grants ({@code "any"}, {@code "musicalInstrument"}, {@code "standard"}, …).
     */
    ArrayNode grantAlternatives(JsonNode block, Function<String, String> keyOf) {
        ArrayNode alternatives = objectMapper.createArrayNode();
        for (JsonNode alternative : asArray(block)) {
            ObjectNode mapped = alternatives.addObject();
            ArrayNode fixed = mapped.putArray("fixed");
            ArrayNode choices = mapped.putArray("choices");
            for (Map.Entry<String, JsonNode> grant : alternative.properties()) {
                String key = grant.getKey();
                if ("choose".equals(key)) {
                    choices.add(choice(grant.getValue(), keyOf));
                } else if (key.startsWith(ANY_PREFIX)) {
                    String category = key.length() == ANY_PREFIX.length() ? ANY_PREFIX : lowerFirst(key.substring(ANY_PREFIX.length()));
                    choices.add(categoryChoice(category, grant.getValue().asInt(1)));
                } else if (grant.getValue().asBoolean(false)) {
                    fixed.add(keyOf.apply(FiveEToolsNaming.stripSourceSuffix(key)));
                }
            }
        }
        return alternatives;
    }

    private ObjectNode choice(JsonNode choose, Function<String, String> keyOf) {
        ObjectNode choice = objectMapper.createObjectNode();
        JsonNode from = choose.get("from");
        if (from == null) {
            choice.putNull("from");
        } else {
            ArrayNode keys = choice.putArray("from");
            from.forEach(key -> keys.add(keyOf.apply(FiveEToolsNaming.stripSourceSuffix(key.asString()))));
        }
        choice.putNull("category");
        choice.put("fromFilter", choose.get("fromFilter") == null ? null : choose.get("fromFilter").asString());
        choice.put("count", choose.path("count").asInt(1));
        choice.put("amount", choose.get("amount") == null ? null : choose.get("amount").asInt());
        return choice;
    }

    private ObjectNode categoryChoice(String category, int count) {
        ObjectNode choice = objectMapper.createObjectNode();
        choice.putNull("from");
        choice.put("category", category);
        choice.putNull("fromFilter");
        choice.put("count", count);
        choice.putNull("amount");
        return choice;
    }

    private static void mapArmor(Iterable<JsonNode> entries, ArrayNode armor, ArrayNode notes) {
        for (JsonNode entry : entries) {
            if (entry.isObject()) {
                armor.add(entry.get("proficiency").asString());
                if (entry.get("full") != null) {
                    notes.add(TagMarkupStripper.strip(entry.get("full").asString()));
                }
            } else {
                armor.add(TagMarkupStripper.strip(entry.asString()));
            }
        }
    }

    private static void mapWeapons(
            Iterable<JsonNode> entries, ArrayNode categories, ArrayNode items, ArrayNode optionalCategories) {
        for (JsonNode entry : entries) {
            if (entry.isObject()) {
                ArrayNode target = entry.path("optional").asBoolean(false) ? optionalCategories : categories;
                target.add(entry.get("proficiency").asString());
                continue;
            }
            String itemSlug = itemSlug(entry.asString());
            if (itemSlug != null) {
                items.add(itemSlug);
            } else {
                categories.add(entry.asString());
            }
        }
    }

    /** The item slug inside a {@code {@item name|source|display}} tag, or null when the text isn't one. */
    static String itemSlug(String text) {
        Matcher matcher = ITEM_TAG.matcher(text);
        return matcher.find() ? FiveEToolsNaming.slug(matcher.group(1)) : null;
    }

    Iterable<JsonNode> asArray(JsonNode node) {
        if (node == null || node.isNull()) {
            return objectMapper.createArrayNode();
        }
        if (node.isArray()) {
            return node;
        }
        ArrayNode single = objectMapper.createArrayNode();
        single.add(node);
        return single;
    }

    private static String lowerFirst(String text) {
        return text.isEmpty() ? text : Character.toLowerCase(text.charAt(0)) + text.substring(1);
    }
}

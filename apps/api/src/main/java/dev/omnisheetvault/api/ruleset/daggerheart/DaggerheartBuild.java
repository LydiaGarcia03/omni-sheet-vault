package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A Daggerheart build document: the player's answers to character creation (SRD pp. 4–6), by field.
 * A heritage is either one {@code ancestry} or a {@code mixedAncestry} (the top feature of one, the
 * bottom feature of another).
 */
final class DaggerheartBuild {

    static final List<String> TRAITS = List.of("agility", "strength", "finesse", "instinct", "presence", "knowledge");

    static final String EMPTY = """
            {"class":"","subclass":"","ancestry":"","mixedAncestry":null,"community":"",\
            "transformationsAllowed":false,"transformation":"","traits":{},"experiences":["",""],\
            "domainCards":[],"primaryWeapon":"","secondaryWeapon":"","armor":"","potion":"","classItem":""}""";

    private final JsonNode node;

    private DaggerheartBuild(JsonNode node) {
        this.node = node;
    }

    static DaggerheartBuild read(String json, ObjectMapper objectMapper) {
        try {
            JsonNode node = objectMapper.readTree(json == null ? EMPTY : json);
            if (!node.isObject()) {
                throw new InvalidBuildException("A Daggerheart build must be a JSON object.");
            }
            return new DaggerheartBuild(node);
        } catch (JacksonException malformed) {
            throw new InvalidBuildException("Malformed Daggerheart build", malformed);
        }
    }

    JsonNode node() {
        return node;
    }

    String classSlug() {
        return text(node, "class");
    }

    String subclassSlug() {
        return text(node, "subclass");
    }

    String ancestrySlug() {
        return text(node, "ancestry");
    }

    boolean isMixedAncestry() {
        return node.path("mixedAncestry").isObject();
    }

    /** The name a mixed heritage goes by ("goblin-orc"); empty when the player gave none. */
    String mixedAncestryName() {
        return text(node.path("mixedAncestry"), "name");
    }

    /** The ancestry the mixed heritage's top (first-listed) feature comes from. */
    String mixedTopSlug() {
        return text(node.path("mixedAncestry"), "top");
    }

    /** The ancestry the mixed heritage's bottom (second-listed) feature comes from. */
    String mixedBottomSlug() {
        return text(node.path("mixedAncestry"), "bottom");
    }

    String communitySlug() {
        return text(node, "community");
    }

    boolean transformationsAllowed() {
        return node.path("transformationsAllowed").asBoolean(false);
    }

    String transformationSlug() {
        return transformationsAllowed() ? text(node, "transformation") : "";
    }

    /** The trait modifiers assigned so far, in the SRD's trait order. */
    Map<String, Integer> traits() {
        Map<String, Integer> traits = new LinkedHashMap<>();
        node.path("traits").properties().forEach(entry -> {
            if (entry.getValue().isInt()) {
                traits.put(entry.getKey(), entry.getValue().asInt());
            }
        });
        return traits;
    }

    List<String> experiences() {
        List<String> experiences = new ArrayList<>();
        node.path("experiences").forEach(experience -> experiences.add(experience.asString("").trim()));
        return experiences;
    }

    /** Which Experience (0 or 1) a feature like Purposeful Design adds +1 to; -1 when unanswered. */
    int boostedExperience() {
        JsonNode index = node.path("boostedExperience");
        return index.isInt() ? index.asInt() : -1;
    }

    List<String> domainCards() {
        List<String> cards = new ArrayList<>();
        node.path("domainCards").forEach(card -> cards.add(card.asString("").trim()));
        return cards;
    }

    String primaryWeapon() {
        return text(node, "primaryWeapon");
    }

    String secondaryWeapon() {
        return text(node, "secondaryWeapon");
    }

    String armor() {
        return text(node, "armor");
    }

    String potion() {
        return text(node, "potion");
    }

    String classItem() {
        return text(node, "classItem");
    }

    /** The levels taken after the first, in order: entry 0 is level 2 (see {@link DaggerheartLevels}). */
    List<JsonNode> levels() {
        List<JsonNode> levels = new ArrayList<>();
        node.path("levels").forEach(levels::add);
        return levels;
    }

    int level() {
        return 1 + levels().size();
    }

    static String text(JsonNode node, String field) {
        JsonNode value = node.path(field);
        return value.isString() ? value.asString().trim() : "";
    }
}

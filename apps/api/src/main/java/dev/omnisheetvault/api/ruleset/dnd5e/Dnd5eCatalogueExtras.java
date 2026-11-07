package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import tools.jackson.databind.JsonNode;

/**
 * Turns a catalogue creature into extras: the stat block copied once from the entry, full hit points, a fresh key per
 * copy, and the book it came from. A familiar takes Find Familiar's "Fey, Fiend, or Celestial" in place of its book
 * type, as D&D Beyond shows it.
 */
final class Dnd5eCatalogueExtras {

    static final String FAMILIAR_TYPE = "(Fey, Fiend, or Celestial)";
    private static final String NONE = "—";

    private Dnd5eCatalogueExtras() {
    }

    static List<Dnd5eExtra> fromCreature(String name, String source, Dnd5eExtraCategory category, JsonNode creature, int quantity) {
        List<Dnd5eExtra> extras = new ArrayList<>();
        for (int copy = 0; copy < quantity; copy++) {
            int hitPoints = creature.path("hitPoints").asInt(0);
            extras.add(new Dnd5eExtra(
                    "extra-" + UUID.randomUUID(), name, category, creature.path("armorClass").asInt(10), hitPoints, hitPoints, 0,
                    creature.path("speed").asInt(0), statBlock(category, creature), source, null));
        }
        return extras;
    }

    private static Dnd5eExtraStatBlock statBlock(Dnd5eExtraCategory category, JsonNode creature) {
        List<Dnd5eExtraAbilityScore> abilities = new ArrayList<>();
        creature.path("abilityScores").forEach(score -> abilities.add(new Dnd5eExtraAbilityScore(
                score.path("abilityKey").asString(), score.path("score").asInt(10), score.path("save").asInt(0))));
        List<Dnd5eExtraSkill> skills = new ArrayList<>();
        creature.path("skills").forEach(skill -> skills.add(new Dnd5eExtraSkill(skill.path("name").asString(), skill.path("bonus").asInt(0))));
        return new Dnd5eExtraStatBlock(
                text(creature, "size"),
                category == Dnd5eExtraCategory.FAMILIAR ? FAMILIAR_TYPE : text(creature, "creatureType"),
                text(creature, "alignment"),
                text(creature, "hitDice"),
                creature.path("additionalSpeeds").isString() ? creature.get("additionalSpeeds").asString() : null,
                abilities,
                skills,
                text(creature, "senses"),
                text(creature, "languages"),
                text(creature, "challengeRating"),
                entries(creature.path("traits")),
                entries(creature.path("actions")),
                entries(creature.path("reactions")),
                entries(creature.path("legendaryActions")));
    }

    private static List<Dnd5eExtraStatEntry> entries(JsonNode list) {
        List<Dnd5eExtraStatEntry> entries = new ArrayList<>();
        list.forEach(entry -> entries.add(new Dnd5eExtraStatEntry(entry.path("name").asString(""), entry.path("description").asString(""))));
        return entries;
    }

    /** The stat block's required texts never come out blank. */
    private static String text(JsonNode creature, String field) {
        String value = creature.path(field).asString("");
        return value.isBlank() ? NONE : value;
    }
}

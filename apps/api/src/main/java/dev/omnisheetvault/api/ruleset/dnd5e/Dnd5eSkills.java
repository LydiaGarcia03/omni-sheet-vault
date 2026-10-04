package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/** Fixed 5e rule: the eighteen skills, keyed by the sheet's camelCase names, with their display names and governing abilities. */
final class Dnd5eSkills {

    static final Map<String, String> LABELS;
    static final Map<String, String> ABILITIES;

    static {
        Map<String, String> labels = new LinkedHashMap<>();
        Map<String, String> abilities = new LinkedHashMap<>();
        add(labels, abilities, "acrobatics", "Acrobatics", "dexterity");
        add(labels, abilities, "animalHandling", "Animal Handling", "wisdom");
        add(labels, abilities, "arcana", "Arcana", "intelligence");
        add(labels, abilities, "athletics", "Athletics", "strength");
        add(labels, abilities, "deception", "Deception", "charisma");
        add(labels, abilities, "history", "History", "intelligence");
        add(labels, abilities, "insight", "Insight", "wisdom");
        add(labels, abilities, "intimidation", "Intimidation", "charisma");
        add(labels, abilities, "investigation", "Investigation", "intelligence");
        add(labels, abilities, "medicine", "Medicine", "wisdom");
        add(labels, abilities, "nature", "Nature", "intelligence");
        add(labels, abilities, "perception", "Perception", "wisdom");
        add(labels, abilities, "performance", "Performance", "charisma");
        add(labels, abilities, "persuasion", "Persuasion", "charisma");
        add(labels, abilities, "religion", "Religion", "intelligence");
        add(labels, abilities, "sleightOfHand", "Sleight of Hand", "dexterity");
        add(labels, abilities, "stealth", "Stealth", "dexterity");
        add(labels, abilities, "survival", "Survival", "wisdom");
        LABELS = Collections.unmodifiableMap(labels);
        ABILITIES = Collections.unmodifiableMap(abilities);
    }

    private Dnd5eSkills() {
    }

    private static void add(Map<String, String> labels, Map<String, String> abilities, String key, String label, String ability) {
        labels.put(key, label);
        abilities.put(key, ability);
    }
}

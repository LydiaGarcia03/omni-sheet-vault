package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Values the player sets by hand over the calculated ones (D&D Beyond's "Customize"); an absent entry means none.
 * {@code passives} is keyed by passive score (passivePerception…), {@code senses} by special sense (DARKVISION…),
 * {@code speeds} by movement (walking, burrowing, climbing, flying, swimming), {@code armorClass} by field (override,
 * baseArmorDex, magicBonus, miscBonus), {@code items} by item key, {@code spells} by spell key and {@code hitPoints} by
 * field (maxModifier, overrideMax); {@code movementDisplay} is the movement the sheet shows; {@code proficiencies} are the
 * armor, weapon, tool and language proficiencies added by hand.
 */
public record Dnd5eCustomizations(
        Map<String, @Valid Dnd5eAbilityCustomization> abilities,
        Map<String, @Valid Dnd5eCheckCustomization> savingThrows,
        Map<String, @Valid Dnd5eCheckCustomization> skills,
        List<@Valid Dnd5eCustomSkill> customSkills,
        Map<String, @Valid Dnd5eNotedValue> passives,
        Map<String, @Valid Dnd5eNotedValue> senses,
        Map<String, @Valid Dnd5eNotedValue> speeds,
        String movementDisplay,
        Map<String, @Valid Dnd5eNotedValue> armorClass,
        List<@Valid Dnd5eCustomDefense> defenses,
        Map<String, @Valid Dnd5eItemCustomization> items,
        Map<String, @Valid Dnd5eSpellCustomization> spells,
        Map<String, @Valid Dnd5eNotedValue> hitPoints,
        List<@Valid Dnd5eCustomProficiency> proficiencies) {

    public static final Dnd5eCustomizations NONE =
            new Dnd5eCustomizations(null, null, null, null, null, null, null, null, null, null, null, null, null, null);

    public Dnd5eCustomizations {
        abilities = abilities == null ? Map.of() : Map.copyOf(abilities);
        savingThrows = savingThrows == null ? Map.of() : Map.copyOf(savingThrows);
        skills = skills == null ? Map.of() : Map.copyOf(skills);
        customSkills = customSkills == null ? List.of() : List.copyOf(customSkills);
        passives = passives == null ? Map.of() : Map.copyOf(passives);
        senses = senses == null ? Map.of() : Map.copyOf(senses);
        speeds = speeds == null ? Map.of() : Map.copyOf(speeds);
        armorClass = armorClass == null ? Map.of() : Map.copyOf(armorClass);
        defenses = defenses == null ? List.of() : List.copyOf(defenses);
        items = items == null ? Map.of() : Map.copyOf(items);
        spells = spells == null ? Map.of() : Map.copyOf(spells);
        hitPoints = hitPoints == null ? Map.of() : Map.copyOf(hitPoints);
        proficiencies = proficiencies == null ? List.of() : List.copyOf(proficiencies);
    }

    public Dnd5eAbilityCustomization ability(String ability) {
        return abilities.getOrDefault(ability, Dnd5eAbilityCustomization.NONE);
    }

    Dnd5eCheckCustomization savingThrow(String ability) {
        return savingThrows.getOrDefault(ability, Dnd5eCheckCustomization.NONE);
    }

    Dnd5eCheckCustomization skill(String skill) {
        return skills.getOrDefault(skill, Dnd5eCheckCustomization.NONE);
    }

    Dnd5eNotedValue passive(String passive) {
        return passives.getOrDefault(passive, Dnd5eNotedValue.NONE);
    }

    Dnd5eNotedValue sense(String sense) {
        return senses.getOrDefault(sense, Dnd5eNotedValue.NONE);
    }

    Dnd5eNotedValue speed(String movement) {
        return speeds.getOrDefault(movement, Dnd5eNotedValue.NONE);
    }

    Dnd5eNotedValue armorClassField(String field) {
        return armorClass.getOrDefault(field, Dnd5eNotedValue.NONE);
    }

    Dnd5eItemCustomization item(String itemKey) {
        return items.getOrDefault(itemKey, Dnd5eItemCustomization.NONE);
    }

    Dnd5eSpellCustomization spell(String spellKey) {
        return spells.getOrDefault(spellKey, Dnd5eSpellCustomization.NONE);
    }

    Dnd5eCustomizations withAbility(String ability, Dnd5eAbilityCustomization customization) {
        return new Dnd5eCustomizations(withEntry(abilities, ability, customization, customization.isEmpty()), savingThrows, skills,
                customSkills, passives, senses, speeds, movementDisplay, armorClass, defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withSavingThrow(String ability, Dnd5eCheckCustomization customization) {
        return new Dnd5eCustomizations(abilities, withEntry(savingThrows, ability, customization, customization.isEmpty()), skills,
                customSkills, passives, senses, speeds, movementDisplay, armorClass, defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withSkill(String skill, Dnd5eCheckCustomization customization) {
        return new Dnd5eCustomizations(abilities, savingThrows, withEntry(skills, skill, customization, customization.isEmpty()),
                customSkills, passives, senses, speeds, movementDisplay, armorClass, defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withPassive(String passive, Dnd5eNotedValue value) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills,
                withEntry(passives, passive, value, value.isEmpty()), senses, speeds, movementDisplay, armorClass, defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withSense(String sense, Dnd5eNotedValue value) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives,
                withEntry(senses, sense, value, value.isEmpty()), speeds, movementDisplay, armorClass, defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withSpeed(String movement, Dnd5eNotedValue value) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses,
                withEntry(speeds, movement, value, value.isEmpty()), movementDisplay, armorClass, defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withMovementDisplay(String movement) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movement, armorClass,
                defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withArmorClassField(String field, Dnd5eNotedValue value) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                withEntry(armorClass, field, value, value.isEmpty()), defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eNotedValue hitPointsField(String field) {
        return hitPoints.getOrDefault(field, Dnd5eNotedValue.NONE);
    }

    Dnd5eCustomizations withHitPointsField(String field, Dnd5eNotedValue value) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                armorClass, defenses, items, spells, withEntry(hitPoints, field, value, value.isEmpty()), proficiencies);
    }

    Dnd5eCustomizations withItem(String itemKey, Dnd5eItemCustomization customization) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                armorClass, defenses, withEntry(items, itemKey, customization, customization.isEmpty()), spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withSpell(String spellKey, Dnd5eSpellCustomization customization) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                armorClass, defenses, items, withEntry(spells, spellKey, customization, customization.isEmpty()), hitPoints, proficiencies);
    }

    /** Adds {@code skill}, or replaces the custom skill with its key. */
    Dnd5eCustomizations withCustomSkill(Dnd5eCustomSkill skill) {
        List<Dnd5eCustomSkill> updated = new ArrayList<>(customSkills);
        int index = indexOfCustomSkill(skill.key());
        if (index < 0) {
            updated.add(skill);
        } else {
            updated.set(index, skill);
        }
        return new Dnd5eCustomizations(abilities, savingThrows, skills, updated, passives, senses, speeds, movementDisplay, armorClass,
                defenses, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withoutCustomSkill(String key) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills,
                customSkills.stream().filter(skill -> !skill.key().equals(key)).toList(), passives, senses, speeds, movementDisplay,
                armorClass, defenses, items, spells, hitPoints, proficiencies);
    }

    int indexOfCustomSkill(String key) {
        for (int index = 0; index < customSkills.size(); index++) {
            if (customSkills.get(index).key().equals(key)) {
                return index;
            }
        }
        return -1;
    }

    /** Adds {@code defense}, or replaces the defense with its key (its notes). */
    Dnd5eCustomizations withDefense(Dnd5eCustomDefense defense) {
        List<Dnd5eCustomDefense> updated = new ArrayList<>(defenses.stream().filter(entry -> !entry.key().equals(defense.key())).toList());
        int index = indexOfDefense(defense.key());
        updated.add(index < 0 ? updated.size() : index, defense);
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                armorClass, updated, items, spells, hitPoints, proficiencies);
    }

    Dnd5eCustomizations withoutDefense(String key) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                armorClass, defenses.stream().filter(defense -> !defense.key().equals(key)).toList(), items, spells, hitPoints, proficiencies);
    }

    int indexOfDefense(String key) {
        for (int index = 0; index < defenses.size(); index++) {
            if (defenses.get(index).key().equals(key)) {
                return index;
            }
        }
        return -1;
    }

    /** Adds {@code proficiency}, or replaces the proficiency with its key (its name or notes). */
    Dnd5eCustomizations withProficiency(Dnd5eCustomProficiency proficiency) {
        List<Dnd5eCustomProficiency> updated = new ArrayList<>(proficiencies);
        int index = indexOfProficiency(proficiency.key());
        if (index < 0) {
            updated.add(proficiency);
        } else {
            updated.set(index, proficiency);
        }
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                armorClass, defenses, items, spells, hitPoints, updated);
    }

    Dnd5eCustomizations withoutProficiency(String key) {
        return new Dnd5eCustomizations(abilities, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay,
                armorClass, defenses, items, spells, hitPoints,
                proficiencies.stream().filter(proficiency -> !proficiency.key().equals(key)).toList());
    }

    int indexOfProficiency(String key) {
        for (int index = 0; index < proficiencies.size(); index++) {
            if (proficiencies.get(index).key().equals(key)) {
                return index;
            }
        }
        return -1;
    }

    /** The named proficiencies the player added of one type, in the order they were added. */
    List<String> proficiencyNames(Dnd5eCustomProficiency.Type type) {
        return proficiencies.stream()
                .filter(proficiency -> proficiency.type() == type && !proficiency.name().isEmpty())
                .map(Dnd5eCustomProficiency::name)
                .toList();
    }

    /** The same customizations without the ability ones, once those are applied to the scores. */
    Dnd5eCustomizations withoutAbilities() {
        return new Dnd5eCustomizations(null, savingThrows, skills, customSkills, passives, senses, speeds, movementDisplay, armorClass,
                defenses, items, spells, hitPoints, proficiencies);
    }

    private static <T> Map<String, T> withEntry(Map<String, T> entries, String key, T value, boolean empty) {
        Map<String, T> updated = new HashMap<>(entries);
        if (empty) {
            updated.remove(key);
        } else {
            updated.put(key, value);
        }
        return updated;
    }
}

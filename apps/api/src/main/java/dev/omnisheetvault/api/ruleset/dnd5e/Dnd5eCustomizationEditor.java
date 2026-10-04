package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.InvalidCustomizationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

/** Reads one customization request (a group, its target and the JSON value) and applies it to the customizations. */
final class Dnd5eCustomizationEditor {

    /** The target that adds a new custom skill or defense. */
    static final String NEW_ENTRY = "new";

    /** A defense to add: its type, damage type or condition, and notes. */
    record NewDefense(@NotNull Dnd5eCustomDefense.Type type, @NotBlank String subtype, @Size(max = 200) String notes) {
    }

    /** A proficiency to add: its type and either the existing proficiency's name or {@code custom} (named later). */
    record NewProficiency(@NotNull Dnd5eCustomProficiency.Type type, @Size(max = 100) String name, Boolean custom) {

        boolean isCustom() {
            return Boolean.TRUE.equals(custom);
        }
    }

    /** A hand-added proficiency's editable fields: a custom one's name, and the source notes. */
    record ProficiencyEdit(@Size(max = 100) String name, @Size(max = 200) String notes) {
    }

    private static final Set<String> ABILITIES = Set.of("strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma");
    private static final Set<String> MOVEMENTS = Set.of("walking", "burrowing", "climbing", "flying", "swimming");
    private static final Set<String> ARMOR_CLASS_FIELDS = Set.of("override", "baseArmorDex", "magicBonus", "miscBonus");
    private static final Set<String> HIT_POINTS_FIELDS = Set.of("maxModifier", "overrideMax");
    private static final Set<String> PASSIVES = Set.of("passivePerception", "passiveInvestigation", "passiveInsight");
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private final ObjectMapper objectMapper;

    Dnd5eCustomizationEditor(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Dnd5eCustomizations apply(Dnd5eSheet sheet, String group, String target, String valueJson) {
        Dnd5eCustomizations current = sheet.customizationsOrEmpty();
        return switch (group) {
            case "abilities" -> current.withAbility(requireAbility(target), read(valueJson, Dnd5eAbilityCustomization.class));
            case "savingThrows" -> current.withSavingThrow(requireAbility(target), savingThrow(valueJson));
            case "skills" -> current.withSkill(requireSkill(target), skill(valueJson));
            case "customSkills" -> customSkill(current, target, valueJson);
            case "passives" -> current.withPassive(requirePassive(target), read(valueJson, Dnd5eNotedValue.class));
            case "senses" -> current.withSense(requireSense(target), read(valueJson, Dnd5eNotedValue.class));
            case "speeds" -> current.withSpeed(requireIn(MOVEMENTS, target, "movement"), read(valueJson, Dnd5eNotedValue.class));
            case "movementDisplay" -> current.withMovementDisplay(requireIn(MOVEMENTS, target, "movement"));
            case "armorClass" -> current.withArmorClassField(requireIn(ARMOR_CLASS_FIELDS, target, "armor class field"),
                    read(valueJson, Dnd5eNotedValue.class));
            case "defenses" -> defense(current, target, valueJson);
            case "items" -> current.withItem(requireItem(sheet, target), read(valueJson, Dnd5eItemCustomization.class));
            case "spells" -> current.withSpell(requireSpell(sheet, target), read(valueJson, Dnd5eSpellCustomization.class));
            case "hitPoints" -> current.withHitPointsField(requireIn(HIT_POINTS_FIELDS, target, "hit points field"),
                    read(valueJson, Dnd5eNotedValue.class));
            case "proficiencies" -> proficiency(current, target, valueJson);
            default -> throw unknownGroup(group);
        };
    }

    private static String requireItem(Dnd5eSheet sheet, String key) {
        if (sheet.items().stream().noneMatch(item -> item.key().equals(key))) {
            throw new InvalidCustomizationException("Unknown item: \"" + key + "\"");
        }
        return key;
    }

    private static String requireSpell(Dnd5eSheet sheet, String key) {
        if (sheet.spells().stream().noneMatch(spell -> spell.key().equals(key))) {
            throw new InvalidCustomizationException("Unknown spell: \"" + key + "\"");
        }
        return key;
    }

    /** {@code new} adds the defense in the value; a key updates that defense's notes. */
    private Dnd5eCustomizations defense(Dnd5eCustomizations current, String target, String valueJson) {
        if (NEW_ENTRY.equals(target)) {
            NewDefense request = read(valueJson, NewDefense.class);
            Dnd5eCustomDefense added = new Dnd5eCustomDefense(UUID.randomUUID().toString(), request.type(), request.subtype(), request.notes());
            if (!added.isOffered()) {
                throw new InvalidCustomizationException("Unknown " + added.type().name().toLowerCase() + ": \"" + added.subtype() + "\"");
            }
            return current.withDefense(added);
        }
        Dnd5eCustomDefense existing = current.defenses().get(requireDefense(current, target));
        Dnd5eNotedValue notes = read(valueJson, Dnd5eNotedValue.class);
        return current.withDefense(new Dnd5eCustomDefense(existing.key(), existing.type(), existing.subtype(), notes.notes()));
    }

    /**
     * {@code new} adds the proficiency in the value (an existing one needs its name; a custom one starts unnamed); a key
     * updates that proficiency's notes, and a custom one's name.
     */
    private Dnd5eCustomizations proficiency(Dnd5eCustomizations current, String target, String valueJson) {
        if (NEW_ENTRY.equals(target)) {
            NewProficiency request = read(valueJson, NewProficiency.class);
            Dnd5eCustomProficiency added = new Dnd5eCustomProficiency(
                    UUID.randomUUID().toString(), request.type(), request.isCustom() ? "" : request.name(), request.isCustom(), null);
            if (!added.isOffered()) {
                throw new InvalidCustomizationException("A custom " + added.type().name().toLowerCase() + " proficiency isn't offered");
            }
            if (!added.custom() && added.name().isEmpty()) {
                throw new InvalidCustomizationException("An existing proficiency needs its name");
            }
            return current.withProficiency(added);
        }
        Dnd5eCustomProficiency existing = current.proficiencies().get(requireProficiency(current, target));
        ProficiencyEdit edit = read(valueJson, ProficiencyEdit.class);
        return current.withProficiency(existing.withNameAndNotes(edit.name(), edit.notes()));
    }

    private static int requireProficiency(Dnd5eCustomizations current, String key) {
        int index = current.indexOfProficiency(key);
        if (index < 0) {
            throw new InvalidCustomizationException("Unknown proficiency: \"" + key + "\"");
        }
        return index;
    }

    private static int requireDefense(Dnd5eCustomizations current, String key) {
        int index = current.indexOfDefense(key);
        if (index < 0) {
            throw new InvalidCustomizationException("Unknown defense: \"" + key + "\"");
        }
        return index;
    }

    private static String requireIn(Set<String> offered, String target, String kind) {
        if (!offered.contains(target)) {
            throw new InvalidCustomizationException("Unknown " + kind + ": \"" + target + "\"");
        }
        return target;
    }

    /** Clears one customization; a custom skill or defense is removed. */
    Dnd5eCustomizations remove(Dnd5eSheet sheet, String group, String target) {
        Dnd5eCustomizations current = sheet.customizationsOrEmpty();
        return switch (group) {
            case "abilities" -> current.withAbility(requireAbility(target), Dnd5eAbilityCustomization.NONE);
            case "savingThrows" -> current.withSavingThrow(requireAbility(target), Dnd5eCheckCustomization.NONE);
            case "skills" -> current.withSkill(requireSkill(target), Dnd5eCheckCustomization.NONE);
            case "customSkills" -> current.withoutCustomSkill(requireCustomSkill(current, target));
            case "passives" -> current.withPassive(requirePassive(target), Dnd5eNotedValue.NONE);
            case "senses" -> current.withSense(requireSense(target), Dnd5eNotedValue.NONE);
            case "speeds" -> current.withSpeed(requireIn(MOVEMENTS, target, "movement"), Dnd5eNotedValue.NONE);
            case "movementDisplay" -> current.withMovementDisplay(null);
            case "armorClass" -> current.withArmorClassField(requireIn(ARMOR_CLASS_FIELDS, target, "armor class field"),
                    Dnd5eNotedValue.NONE);
            case "defenses" -> current.withoutDefense(current.defenses().get(requireDefense(current, target)).key());
            case "items" -> current.withItem(target, Dnd5eItemCustomization.NONE);
            case "spells" -> current.withSpell(target, Dnd5eSpellCustomization.NONE);
            case "hitPoints" -> current.withHitPointsField(requireIn(HIT_POINTS_FIELDS, target, "hit points field"), Dnd5eNotedValue.NONE);
            case "proficiencies" -> current.withoutProficiency(current.proficiencies().get(requireProficiency(current, target)).key());
            default -> throw unknownGroup(group);
        };
    }

    /** A saving throw always uses its own ability, so it takes no stat override. */
    private Dnd5eCheckCustomization savingThrow(String valueJson) {
        Dnd5eCheckCustomization customization = read(valueJson, Dnd5eCheckCustomization.class);
        if (customization.statOverride() != null) {
            throw new InvalidCustomizationException("A saving throw has no stat override");
        }
        return customization;
    }

    private Dnd5eCheckCustomization skill(String valueJson) {
        Dnd5eCheckCustomization customization = read(valueJson, Dnd5eCheckCustomization.class);
        requireAbilityOrNone(customization.statOverride());
        return customization;
    }

    /** {@code new} adds a default custom skill; a key replaces that custom skill's fields. */
    private Dnd5eCustomizations customSkill(Dnd5eCustomizations current, String target, String valueJson) {
        if (NEW_ENTRY.equals(target)) {
            String key = UUID.randomUUID().toString();
            return current.withCustomSkill(Dnd5eCustomSkill.added(key, current.customSkills().size() + 1));
        }
        String key = requireCustomSkill(current, target);
        Dnd5eCustomSkill edited = read(valueJson, Dnd5eCustomSkill.class);
        requireAbilityOrNone(edited.statOverride());
        return current.withCustomSkill(current.customSkills().get(current.indexOfCustomSkill(key)).editedAs(edited));
    }

    private static String requireAbility(String target) {
        if (!ABILITIES.contains(target)) {
            throw new InvalidCustomizationException("Unknown ability: \"" + target + "\"");
        }
        return target;
    }

    private static void requireAbilityOrNone(String ability) {
        if (ability != null) {
            requireAbility(ability);
        }
    }

    private static String requireSkill(String target) {
        if (!Dnd5eSkills.ABILITIES.containsKey(target)) {
            throw new InvalidCustomizationException("Unknown skill: \"" + target + "\"");
        }
        return target;
    }

    private static String requirePassive(String target) {
        if (!PASSIVES.contains(target)) {
            throw new InvalidCustomizationException("Unknown passive score: \"" + target + "\"");
        }
        return target;
    }

    private static String requireSense(String target) {
        try {
            return Dnd5eSenseType.valueOf(target).name();
        } catch (IllegalArgumentException e) {
            throw new InvalidCustomizationException("Unknown sense: \"" + target + "\"");
        }
    }

    private static String requireCustomSkill(Dnd5eCustomizations current, String key) {
        if (current.indexOfCustomSkill(key) < 0) {
            throw new InvalidCustomizationException("Unknown custom skill: \"" + key + "\"");
        }
        return key;
    }

    private static InvalidCustomizationException unknownGroup(String group) {
        return new InvalidCustomizationException("Unknown customization group: \"" + group + "\"");
    }

    private <T> T read(String valueJson, Class<T> type) {
        T value;
        try {
            value = objectMapper.readValue(valueJson, type);
        } catch (JacksonException e) {
            throw new InvalidCustomizationException("Unreadable customization: " + e.getOriginalMessage());
        }
        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(value);
        if (!violations.isEmpty()) {
            throw new InvalidCustomizationException(violations.stream()
                    .map(violation -> violation.getPropertyPath() + " " + violation.getMessage())
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }
        return value;
    }
}

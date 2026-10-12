package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.AttackRow;
import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.CreationPreview;
import dev.omnisheetvault.api.ruleset.FeatureTrait;
import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.ProgressionTable;
import dev.omnisheetvault.api.ruleset.SelectionDetail;
import dev.omnisheetvault.api.ruleset.StartingEquipmentPreview;
import dev.omnisheetvault.api.ruleset.SpecialSense;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The builder's live character summary: ability scores, class progressions and the chosen species'
 * and background's detail cards always, the rest once the draft can form a sheet.
 */
public record DraftPreviewResponse(
        List<AbilityPreview> abilities, VitalsPreview vitals, List<ProgressionTable> progressions, Map<String, SelectionDetail> selections,
        StartingEquipmentResponse startingEquipment) {

    /** The starting items as the Equipment step lists them, and the starting money in coins. */
    public record StartingEquipmentResponse(List<StartingEquipmentPreview.Entry> inventory, int gold, int silver, int copper) {
    }

    private static final String NOT_PROFICIENT = "NONE";

    /** Features nearly every character gets, left out of the summary so it lists what sets this one apart. */
    private static final Set<String> ROUTINE_FEATURES = Set.of("Ability Score Improvement");

    /** Facts whose value stands in for the feature that names them ("Creature Type" → "Fey"). */
    private static final Set<String> VALUE_FEATURES = Set.of("creature type");

    /** Words kept lower-case inside a title. */
    private static final Set<String> MINOR_WORDS = Set.of("of", "and", "the", "a", "an", "in", "on", "to");

    public record AbilityPreview(String ability, int score, int modifier, List<Contribution> contributions) {
    }

    public record Named(String name, int value) {
    }

    public record AttackPreview(String name, int toHit, String damage) {
    }

    public record VitalsPreview(
            int level, CalculatedValue hitPoints, String hitDice, CalculatedValue armorClass, int speed, int initiative,
            int proficiencyBonus, List<Named> savingThrows, int passivePerception, List<AttackPreview> attacks,
            List<String> features, List<String> skills, List<String> armor, List<String> weapons, List<String> tools,
            List<String> languages, List<String> resistances, List<String> senses) {
    }

    static DraftPreviewResponse from(CreationPreview preview, VitalsZone vitals) {
        List<AbilityPreview> abilities = preview.abilityScores().entrySet().stream()
                .map(entry -> new AbilityPreview(entry.getKey(), entry.getValue().value(),
                        Math.floorDiv(entry.getValue().value() - 10, 2), entry.getValue().contributions()))
                .toList();
        StartingEquipmentPreview equipment = preview.startingEquipment() == null ? StartingEquipmentPreview.none() : preview.startingEquipment();
        return new DraftPreviewResponse(abilities, vitals == null ? null : vitals(vitals, factValues(preview.selections())), preview.progressions(),
                preview.selections(), new StartingEquipmentResponse(equipment.inventory(), equipment.gold(), equipment.silver(), equipment.copper()));
    }

    /** Every selection card's facts by lower-cased label ("creature type" → "Fey"). */
    private static Map<String, String> factValues(Map<String, SelectionDetail> selections) {
        Map<String, String> values = new HashMap<>();
        if (selections != null) {
            selections.values().forEach(detail -> detail.facts().forEach(fact -> values.putIfAbsent(fact.label().toLowerCase(Locale.ROOT), fact.text())));
        }
        return values;
    }

    /** "sleightOfHand" → "Sleight of Hand", "animalHandling" → "Animal Handling": a camel-case key as a title. */
    static String words(String key) {
        String[] parts = key.split("(?<=[a-z])(?=[A-Z])|[ _-]+");
        StringBuilder title = new StringBuilder();
        for (int index = 0; index < parts.length; index++) {
            String part = parts[index].toLowerCase(Locale.ROOT);
            if (part.isEmpty()) {
                continue;
            }
            if (!title.isEmpty()) {
                title.append(' ');
            }
            title.append(index > 0 && MINOR_WORDS.contains(part) ? part : part.substring(0, 1).toUpperCase(Locale.ROOT) + part.substring(1));
        }
        return title.toString();
    }

    /**
     * A feature that only names a fact the selection cards already hold: "Creature Type" shows the fact's value
     * ("Fey"); any other ("Languages", "Size") is left out, since the summary shows it elsewhere. Null means leave out.
     */
    private static String featureLabel(String name, Map<String, String> factValues) {
        String key = name.toLowerCase(Locale.ROOT);
        if (!factValues.containsKey(key)) {
            return name;
        }
        return VALUE_FEATURES.contains(key) ? factValues.get(key) : null;
    }

    private static VitalsPreview vitals(VitalsZone vitals, Map<String, String> factValues) {
        return new VitalsPreview(
                vitals.level(), vitals.hitPoints(), hitDice(vitals.hitDice()), vitals.armorClass(), vitals.speed(),
                vitals.initiative().value(), vitals.proficiencyBonus().value(),
                proficient(vitals.savingThrows(), vitals.savingThrowProficiencies()),
                vitals.senses().getOrDefault("passivePerception", new CalculatedValue(0, List.of())).value(),
                vitals.attacks().values().stream().map(DraftPreviewResponse::attack).toList(),
                vitals.featureTraits().stream().map(FeatureTrait::name).filter(name -> !ROUTINE_FEATURES.contains(name))
                        .map(name -> featureLabel(name, factValues)).filter(Objects::nonNull).toList(),
                proficient(vitals.skills(), vitals.skillProficiencies()).stream().map(skill -> words(skill.name())).toList(),
                vitals.armorProficiencies(), vitals.weaponProficiencies(), vitals.toolProficiencies(), vitals.languages(),
                vitals.damageResistances(), vitals.specialSenses().stream().map(DraftPreviewResponse::sense).toList());
    }

    /** "3d10 + 1d8", one term per class die size. */
    private static String hitDice(HitDice hitDice) {
        if (hitDice.pools() == null || hitDice.pools().isEmpty()) {
            return hitDice.max() + "d" + hitDice.dieSize();
        }
        return hitDice.pools().stream().map(pool -> pool.max() + "d" + pool.dieSize()).collect(Collectors.joining(" + "));
    }

    private static List<Named> proficient(Map<String, CalculatedValue> values, Map<String, String> proficiency) {
        return values.entrySet().stream()
                .filter(entry -> !NOT_PROFICIENT.equals(proficiency.getOrDefault(entry.getKey(), NOT_PROFICIENT)))
                .map(entry -> new Named(entry.getKey(), entry.getValue().value()))
                .toList();
    }

    private static AttackPreview attack(AttackRow row) {
        String dice = row.damageDiceCount() > 0 ? row.damageDiceCount() + "d" + row.damageDiceSides() : "";
        String modifier = row.damageModifier() == 0 ? "" : (row.damageModifier() > 0 && !dice.isEmpty() ? "+" : "") + row.damageModifier();
        String damage = (dice + modifier).isEmpty() ? "0" : dice + modifier;
        return new AttackPreview(row.name(), row.toHit().value(), row.damageType() == null ? damage : damage + " " + row.damageType());
    }

    private static String sense(SpecialSense sense) {
        return sense.label() != null ? sense.label() : sense.type() + " " + sense.rangeFeet() + " ft.";
    }
}

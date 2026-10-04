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
import java.util.List;
import java.util.Map;
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
        return new DraftPreviewResponse(abilities, vitals == null ? null : vitals(vitals), preview.progressions(), preview.selections(),
                new StartingEquipmentResponse(equipment.inventory(), equipment.gold(), equipment.silver(), equipment.copper()));
    }

    private static VitalsPreview vitals(VitalsZone vitals) {
        return new VitalsPreview(
                vitals.level(), vitals.hitPoints(), hitDice(vitals.hitDice()), vitals.armorClass(), vitals.speed(),
                vitals.initiative().value(), vitals.proficiencyBonus().value(),
                proficient(vitals.savingThrows(), vitals.savingThrowProficiencies()),
                vitals.senses().getOrDefault("passivePerception", new CalculatedValue(0, List.of())).value(),
                vitals.attacks().values().stream().map(DraftPreviewResponse::attack).toList(),
                vitals.featureTraits().stream().map(FeatureTrait::name).toList(),
                proficient(vitals.skills(), vitals.skillProficiencies()).stream().map(Named::name).toList(),
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

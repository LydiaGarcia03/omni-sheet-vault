package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record CharacterSheetResponse(
        Map<String, Integer> abilityScores,
        Map<String, CalculatedValueResponse> abilityModifiers,
        CalculatedValueResponse proficiencyBonus,
        CalculatedValueResponse armorClass,
        CalculatedValueResponse initiative,
        CalculatedValueResponse hitPoints,
        int speed,
        int level,
        Map<String, CalculatedValueResponse> savingThrows,
        Map<String, String> savingThrowProficiencies,
        Map<String, CalculatedValueResponse> senses,
        List<String> armorProficiencies,
        List<String> weaponProficiencies,
        List<String> toolProficiencies,
        List<String> languages,
        Map<String, CalculatedValueResponse> skills,
        Map<String, String> skillProficiencies,
        Map<String, String> skillGoverningAbilities,
        List<String> damageResistances,
        List<String> damageImmunities,
        List<String> damageVulnerabilities,
        List<String> conditionImmunities,
        Set<String> activeConditions,
        int exhaustionLevel,
        int currentHitPoints,
        int temporaryHitPoints,
        int calculatedMaxHitPoints,
        boolean heroicInspiration,
        Map<String, AttackRowResponse> attacks,
        List<FeatureActionResponse> featureActions,
        List<SpellcastingClassInfoResponse> spellcasting,
        List<SpellResponse> spells,
        List<ItemResponse> items,
        CoinsResponse coins,
        List<FeatureTraitResponse> featureTraits,
        BackgroundResponse background,
        List<ExtraResponse> extras,
        HitDiceResponse hitDice,
        List<SpellSlotLevelResponse> spellSlots,
        List<SpecialSenseResponse> specialSenses,
        List<CustomActionResponse> customActions,
        EncumbranceResponse encumbrance,
        int attacksPerAction,
        Map<String, RollModeResponse> rollModes,
        List<ActiveEffectResponse> activeEffects,
        ProvenanceResponse provenance,
        List<RollNoteResponse> rollNotes,
        DeathSavesResponse deathSaves,
        ExperienceResponse experience,
        String sheetTheme,
        List<CustomSkillResponse> customSkills,
        List<DefenseResponse> defenses) {

    public record CustomSkillResponse(
            String key, String name, String abilityKey, String proficiencyLevel, CalculatedValueResponse value, String notes,
            String description) {
    }

    public record DefenseResponse(String key, String type, String name, String source, boolean custom, String notes) {
    }

    static CharacterSheetResponse from(VitalsZone vitals) {
        return new CharacterSheetResponse(
                vitals.abilityScores(),
                mapValues(vitals.abilityModifiers()),
                CalculatedValueResponse.from(vitals.proficiencyBonus()),
                CalculatedValueResponse.from(vitals.armorClass()),
                CalculatedValueResponse.from(vitals.initiative()),
                CalculatedValueResponse.from(vitals.hitPoints()),
                vitals.speed(),
                vitals.level(),
                mapValues(vitals.savingThrows()),
                vitals.savingThrowProficiencies(),
                mapValues(vitals.senses()),
                vitals.armorProficiencies(),
                vitals.weaponProficiencies(),
                vitals.toolProficiencies(),
                vitals.languages(),
                mapValues(vitals.skills()),
                vitals.skillProficiencies(),
                vitals.skillGoverningAbilities(),
                vitals.damageResistances(),
                vitals.damageImmunities(),
                vitals.damageVulnerabilities(),
                vitals.conditionImmunities(),
                vitals.activeConditions(),
                vitals.exhaustionLevel(),
                vitals.currentHitPoints(),
                vitals.temporaryHitPoints(),
                vitals.calculatedMaxHitPoints(),
                vitals.heroicInspiration(),
                vitals.attacks().entrySet().stream()
                        // LinkedHashMap, not the default HashMap `Collectors.toMap` builds when no map
                        // supplier is given — this is a rendered, ordered table on the frontend, and a
                        // HashMap here would silently undo VitalsZone.attacks()'s own LinkedHashMap
                        // ordering (see that method's doc comment).
                        .collect(Collectors.toMap(
                                Map.Entry::getKey, entry -> AttackRowResponse.from(entry.getValue()), (a, b) -> a, LinkedHashMap::new)),
                vitals.featureActions().stream().map(FeatureActionResponse::from).toList(),
                vitals.spellcasting().stream().map(SpellcastingClassInfoResponse::from).toList(),
                vitals.spells().stream().map(SpellResponse::from).toList(),
                vitals.items().stream().map(ItemResponse::from).toList(),
                CoinsResponse.from(vitals.coins()),
                vitals.featureTraits().stream().map(FeatureTraitResponse::from).toList(),
                BackgroundResponse.from(vitals.background()),
                vitals.extras().stream().map(ExtraResponse::from).toList(),
                HitDiceResponse.from(vitals.hitDice()),
                vitals.spellSlots().stream().map(SpellSlotLevelResponse::from).toList(),
                vitals.specialSenses().stream().map(SpecialSenseResponse::from).toList(),
                vitals.customActions().stream().map(CustomActionResponse::from).toList(),
                EncumbranceResponse.from(vitals.encumbrance()),
                vitals.attacksPerAction(),
                vitals.rollModes().entrySet().stream().collect(Collectors.toMap(
                        Map.Entry::getKey, entry -> RollModeResponse.from(entry.getValue()), (a, b) -> a, LinkedHashMap::new)),
                vitals.activeEffects().stream().map(ActiveEffectResponse::from).toList(),
                ProvenanceResponse.from(vitals.provenance()),
                vitals.rollNotes().stream().map(RollNoteResponse::from).toList(),
                DeathSavesResponse.from(vitals.deathSaves()),
                ExperienceResponse.from(vitals.experience()),
                vitals.sheetTheme(),
                vitals.customSkills().stream()
                        .map(skill -> new CustomSkillResponse(skill.key(), skill.name(), skill.abilityKey(), skill.proficiencyLevel(),
                                CalculatedValueResponse.from(skill.value()), skill.notes(), skill.description()))
                        .toList(),
                vitals.defenses().stream()
                        .map(defense -> new DefenseResponse(defense.key(), defense.type(), defense.name(), defense.source(),
                                defense.custom(), defense.notes()))
                        .toList());
    }

    private static Map<String, CalculatedValueResponse> mapValues(Map<String, CalculatedValue> values) {
        return values.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> CalculatedValueResponse.from(entry.getValue())));
    }
}

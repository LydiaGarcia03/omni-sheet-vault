package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.CustomAction;

public record CustomActionResponse(
        String key,
        String template,
        String name,
        String snippet,
        String description,
        String rangeCategory,
        Integer rangeFeet,
        String stat,
        Integer diceCount,
        Integer dieType,
        Integer fixedValue,
        String damageType,
        String saveType,
        Integer fixedSaveDc,
        String spellRangeType,
        String aoeType,
        Integer aoeSize,
        String activationType,
        Integer activationTime,
        boolean affectedByMartialArts,
        boolean proficient,
        boolean displayAsAttack,
        String weaponAttackType,
        Integer longRange,
        boolean dualWield,
        boolean silvered) {

    static CustomActionResponse from(CustomAction action) {
        return new CustomActionResponse(
                action.key(), action.template(), action.name(), action.snippet(), action.description(),
                action.rangeCategory(), action.rangeFeet(), action.stat(), action.diceCount(), action.dieType(),
                action.fixedValue(), action.damageType(), action.saveType(), action.fixedSaveDc(),
                action.spellRangeType(), action.aoeType(), action.aoeSize(), action.activationType(),
                action.activationTime(), action.affectedByMartialArts(), action.proficient(),
                action.displayAsAttack(), action.weaponAttackType(), action.longRange(), action.dualWield(),
                action.silvered());
    }
}

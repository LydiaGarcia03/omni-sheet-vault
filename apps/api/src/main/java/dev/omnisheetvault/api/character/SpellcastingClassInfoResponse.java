package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.SpellcastingClassInfo;

public record SpellcastingClassInfoResponse(
        String className,
        String spellcastingAbility,
        CalculatedValueResponse spellcastingModifier,
        CalculatedValueResponse spellAttackBonus,
        CalculatedValueResponse spellSaveDc,
        String castingType,
        int cantripsKnownMax,
        Integer spellsKnownMax,
        Integer spellsPreparedMax,
        boolean classCaster) {

    static SpellcastingClassInfoResponse from(SpellcastingClassInfo info) {
        return new SpellcastingClassInfoResponse(
                info.className(), info.spellcastingAbility(), CalculatedValueResponse.from(info.spellcastingModifier()),
                CalculatedValueResponse.from(info.spellAttackBonus()), CalculatedValueResponse.from(info.spellSaveDc()),
                info.castingType(), info.cantripsKnownMax(), info.spellsKnownMax(), info.spellsPreparedMax(),
                info.classCaster());
    }
}

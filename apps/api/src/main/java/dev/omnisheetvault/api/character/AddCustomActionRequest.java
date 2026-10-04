package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record AddCustomActionRequest(
        @NotBlank String template,
        @NotBlank String name,
        @NotNull String snippet,
        @NotNull String description,
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
}

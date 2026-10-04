package dev.omnisheetvault.api.ruleset;

/**
 * One player-authored custom action — see VitalsZone. Nothing here is calculated
 * (no formula, no contributions), same plain-pass-through treatment as {@link Item}.
 * {@code template}/{@code rangeCategory}/{@code spellRangeType}/{@code aoeType}/
 * {@code activationType}/{@code weaponAttackType} are plain strings, not shared
 * enums: which values exist is a per-system concept (adr-0003), same reasoning as
 * {@link FeatureAction#actionType}. A {@code displayAsAttack} entry that resolves to
 * a real to-hit roll is folded into {@code VitalsZone.attacks} instead and does not
 * also appear here twice — see {@code Dnd5eCustomAction}'s doc comment for exactly
 * which entries that covers and which stay list-only. Used both for display
 * ({@code VitalsZone.customActions}) and as {@link SheetMutator#addCustomAction}'s
 * input, same double duty {@link Spell} already has for {@link SheetMutator#learnSpell}.
 */
public record CustomAction(
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
}

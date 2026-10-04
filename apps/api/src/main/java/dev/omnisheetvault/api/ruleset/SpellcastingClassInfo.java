package dev.omnisheetvault.api.ruleset;

/**
 * One class's calculated spellcasting numbers — see VitalsZone. "A multiclass
 * character has one set per class" (systems/dnd-5e/sheet-ui.md's Spells tab), so
 * VitalsZone carries a list of these rather than a single set. {@code castingType}
 * is a plain string, not a shared enum — same treatment as {@code FeatureAction}'s
 * {@code actionType} (adr-0003: which values exist is a per-system concept).
 * {@code cantripsKnownMax}/{@code spellsKnownMax}/{@code spellsPreparedMax} are
 * phase 9's "Manage spells" limits — see {@code Dnd5eSpellcastingClass}'s doc
 * comment for which apply to which casting type. {@code classCaster} is false for a
 * non-class source that casts its own granted spells ("High Elf"): the Spells tab
 * header lists class casters only, as D&D Beyond's does.
 */
public record SpellcastingClassInfo(
        String className,
        String spellcastingAbility,
        CalculatedValue spellcastingModifier,
        CalculatedValue spellAttackBonus,
        CalculatedValue spellSaveDc,
        String castingType,
        int cantripsKnownMax,
        Integer spellsKnownMax,
        Integer spellsPreparedMax,
        boolean classCaster) {

    public SpellcastingClassInfo(
            String className, String spellcastingAbility, CalculatedValue spellcastingModifier,
            CalculatedValue spellAttackBonus, CalculatedValue spellSaveDc, String castingType, int cantripsKnownMax,
            Integer spellsKnownMax, Integer spellsPreparedMax) {
        this(className, spellcastingAbility, spellcastingModifier, spellAttackBonus, spellSaveDc, castingType,
                cantripsKnownMax, spellsKnownMax, spellsPreparedMax, true);
    }
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A spell the character knows or has prepared — see systems/dnd-5e/sheet-ui.md's
 * Spells tab. {@code level} 0 is a cantrip. {@code attackRoll} and the damage dice
 * are phase 9's "optional damage + attack flag" model, confirmed with the owner: a
 * spell may need an attack roll, deal damage, both, or neither (most utility
 * spells), independently of each other — unlike weapon attacks, spell damage
 * carries no ability modifier (PHB), so {@link Dnd5eMechanicResolver#resolve} adds
 * none. {@code damageDiceCount}/{@code damageDiceSides}/{@code damageType} are all
 * null together for a spell with no damage. Scaling damage/healing by the slot
 * level cast at is modeled — see {@code higherLevelsDamageDiceCount}/
 * {@code higherLevelsDamageDiceSides} below and {@link Dnd5eMechanicResolver
 * #scaledDice} for the actual roll math; a weapon attack still takes the "fixed
 * base numbers only" simplification, unaffected by this. {@code prepared}/
 * {@code alwaysPrepared} are phase 9's
 * "Manage spells" model (see {@link Dnd5eSpellCastingType}): meaningless for a
 * cantrip or for a spell whose owning class is {@code KNOWN} type (both are always
 * usable) — {@link Dnd5eSheetMutator#prepareSpell}/{@code unprepareSpell} no-op in
 * those cases rather than reading these fields. {@code alwaysPrepared} is seed/import
 * data only (a domain-granted spell, say) — no mutation ever sets it. {@code
 * castingTime}/{@code range}/{@code notes} (components and duration, written out —
 * e.g. "Concentration, up to 1 minute, V, S" — rather than D&D Beyond's own
 * abbreviated codes) and {@code effectSummary} (a short category label such as
 * "Buff"/"Healing"/"Control"/"Detection"/"Damage", shown in the Spells tab's Effect
 * column only when the spell has no damage roll to show instead) are phase 10's
 * spell table columns (slice 8) — confirmed live against D&D Beyond's own Spells
 * tab that these are real, distinct columns, not presentation of data already
 * modeled elsewhere. {@code saveAbility} (null for a spell with no saving throw,
 * e.g. an attack-roll spell or one with neither) is the ability the spell's
 * *target* rolls — e.g. Fireball always forces a Dexterity save, regardless of
 * the caster's own spellcasting ability, which only sets the DC's numeric value
 * (a distinction the Spells/Actions tabs' Hit/DC column used to get wrong,
 * showing the caster's spellcasting ability as the label instead). {@code
 * components} (e.g. "V, S, M") and {@code materialComponent} (null when there is
 * no M component) and {@code duration} (e.g. "Instantaneous", "Concentration, up
 * to 1 minute") are the sidebar detail panel's own fields — distinct from
 * {@code notes}, which stays the table's own free-text column. {@code
 * higherLevelsDescription} is 5etools' own "At Higher Levels" scaling text (null
 * for a spell with none — most cantrips, and some leveled spells).
 * {@code higherLevelsDamageDiceCount}/{@code higherLevelsDamageDiceSides} are that
 * same scaling, structured: the dice to add to {@code damageDiceCount}/
 * {@code damageDiceSides} per slot level cast above this spell's own {@code level}.
 *
 * <p>{@code grantedByItemKey}/{@code chargeCost}/{@code fixedSaveDc} are
 * systems/dnd-5e/features/inventory-equipment-mechanics.md's slice 7 (the Wand of Fireballs
 * mechanic) — all three null together for a normal class-known spell.
 * {@code grantedByItemKey} names the {@code Dnd5eItem} whose {@code
 * Dnd5eGrantedSpell} produced this entry ({@code Dnd5eSheetMutator#addCatalogueItem}
 * builds one of these per granted spell, {@code className} set to the item's own
 * name rather than a real spellcasting class — D&D Beyond's own source line does
 * the same). {@code chargeCost} is how many of that item's charges casting this
 * spell spends ({@code Dnd5eSheetMutator#castItemGrantedSpell}); upcasting by
 * spending extra charges (the real PHB rule) is not modeled — a deliberate,
 * named simplification, not the spell's own higher-level scaling fields repurposed
 * for a different mechanic. {@code fixedSaveDc} is the item's own flat save DC
 * (e.g. "save 15"), used in place of a spellcasting class's own computed DC, which
 * a granted spell has none of.
 *
 * <p>{@code usage} is how a feature-granted spell is cast without a slot (at will, or a
 * limited number of times per rest); null for a spell cast with slots.
 */
public record Dnd5eSpell(
        @NotBlank String key,
        @NotBlank String name,
        @NotBlank String className,
        @Min(0) @Max(9) int level,
        @NotBlank String school,
        @NotBlank String castingTime,
        @NotBlank String range,
        boolean concentration,
        boolean ritual,
        boolean attackRoll,
        Integer damageDiceCount,
        Integer damageDiceSides,
        String damageType,
        @NotBlank String notes,
        @NotBlank String effectSummary,
        boolean prepared,
        boolean alwaysPrepared,
        @NotNull String description,
        String saveAbility,
        @NotBlank String components,
        String materialComponent,
        @NotBlank String duration,
        String higherLevelsDescription,
        Integer higherLevelsDamageDiceCount,
        Integer higherLevelsDamageDiceSides,
        String grantedByItemKey,
        Integer chargeCost,
        Integer fixedSaveDc,
        @Valid Dnd5eSpellUsage usage) {

    /** A spell cast with slots or item charges. */
    public Dnd5eSpell(
            String key, String name, String className, int level, String school, String castingTime, String range,
            boolean concentration, boolean ritual, boolean attackRoll, Integer damageDiceCount,
            Integer damageDiceSides, String damageType, String notes, String effectSummary, boolean prepared,
            boolean alwaysPrepared, String description, String saveAbility, String components,
            String materialComponent, String duration, String higherLevelsDescription,
            Integer higherLevelsDamageDiceCount, Integer higherLevelsDamageDiceSides, String grantedByItemKey,
            Integer chargeCost, Integer fixedSaveDc) {
        this(key, name, className, level, school, castingTime, range, concentration, ritual, attackRoll,
                damageDiceCount, damageDiceSides, damageType, notes, effectSummary, prepared, alwaysPrepared,
                description, saveAbility, components, materialComponent, duration, higherLevelsDescription,
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides, grantedByItemKey, chargeCost, fixedSaveDc, null);
    }

    public Dnd5eSpell withPrepared(boolean newPrepared) {
        return new Dnd5eSpell(key, name, className, level, school, castingTime, range, concentration, ritual, attackRoll,
                damageDiceCount, damageDiceSides, damageType, notes, effectSummary, newPrepared, alwaysPrepared,
                description, saveAbility, components, materialComponent, duration, higherLevelsDescription,
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides, grantedByItemKey, chargeCost, fixedSaveDc, usage);
    }

    public Dnd5eSpell withUsage(Dnd5eSpellUsage newUsage) {
        return new Dnd5eSpell(key, name, className, level, school, castingTime, range, concentration, ritual, attackRoll,
                damageDiceCount, damageDiceSides, damageType, notes, effectSummary, prepared, alwaysPrepared,
                description, saveAbility, components, materialComponent, duration, higherLevelsDescription,
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides, grantedByItemKey, chargeCost, fixedSaveDc, newUsage);
    }

    /** A normal, class-known spell — see {@code Dnd5eSheetMutator#learnSpell}, unchanged since phase 9. */
    public Dnd5eSpell(
            String key, String name, String className, int level, String school, String castingTime, String range,
            boolean concentration, boolean ritual, boolean attackRoll, Integer damageDiceCount,
            Integer damageDiceSides, String damageType, String notes, String effectSummary, boolean prepared,
            boolean alwaysPrepared, String description, String saveAbility, String components,
            String materialComponent, String duration, String higherLevelsDescription,
            Integer higherLevelsDamageDiceCount, Integer higherLevelsDamageDiceSides) {
        this(key, name, className, level, school, castingTime, range, concentration, ritual, attackRoll,
                damageDiceCount, damageDiceSides, damageType, notes, effectSummary, prepared, alwaysPrepared,
                description, saveAbility, components, materialComponent, duration, higherLevelsDescription,
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides, null, null, null, null);
    }
}

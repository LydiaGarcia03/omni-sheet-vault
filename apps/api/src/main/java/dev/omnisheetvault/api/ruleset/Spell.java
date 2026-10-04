package dev.omnisheetvault.api.ruleset;

/**
 * One spell the character knows or has prepared — see VitalsZone. Nothing here is
 * calculated (no formula, no contributions), so it is a plain pass-through of
 * stored data, same treatment as an ability score. {@code key} identifies this
 * entry for a spell attack/damage roll (phase 9); {@code damageDiceCount}/
 * {@code damageDiceSides}/{@code damageType} are null together for a spell with
 * no damage. {@code prepared}/{@code alwaysPrepared} are phase 9's "Manage spells"
 * model — see {@code Dnd5eSpell}'s doc comment for what they mean and when they're
 * meaningless (a cantrip, or a spell under a {@code KNOWN}-type class). {@code
 * castingTime}/{@code range}/{@code notes}/{@code effectSummary} are phase 10's
 * Spells tab table columns — see {@code Dnd5eSpell}'s doc comment. {@code
 * saveAbility} is the ability the spell's *target* rolls a saving throw with
 * (e.g. Fireball is always a Dexterity save) — independent of the caster's own
 * spellcasting ability, which only sets the DC's numeric value. {@code
 * components}/{@code materialComponent}/{@code duration} are the sidebar detail
 * panel's own fields, distinct from {@code notes} (the table's free-text column).
 * {@code higherLevelsDescription} is 5etools' own "At Higher Levels" scaling text
 * (null when the spell has none) — see {@code SpellCatalogueData}'s own doc comment.
 * {@code higherLevelsDamageDiceCount}/{@code higherLevelsDamageDiceSides} are that
 * same scaling, structured — the dice added to {@code damageDiceCount}/
 * {@code damageDiceSides} per slot level cast above this spell's own {@code level}
 * — see {@code Dnd5eMechanicResolver#scaledDice}.
 *
 * <p>{@code grantedByItemKey}/{@code chargeCost}/{@code fixedSaveDc} are the Wand
 * of Fireballs mechanic — see {@code Dnd5eSpell}'s own doc comment for the full
 * reasoning. All three null together for a normal class-known spell.
 *
 * <p>{@code adjustments} are the player's Customize values over the class's attack bonus and save DC; {@code name}
 * and {@code notes} already carry a customized name and notes. {@code usage} is how a feature-granted spell is cast
 * without a slot; null for a spell cast with slots.
 */
public record Spell(
        String key,
        String name,
        String className,
        int level,
        String school,
        String castingTime,
        String range,
        boolean concentration,
        boolean ritual,
        boolean attackRoll,
        Integer damageDiceCount,
        Integer damageDiceSides,
        String damageType,
        String notes,
        String effectSummary,
        boolean prepared,
        boolean alwaysPrepared,
        String description,
        String saveAbility,
        String components,
        String materialComponent,
        String duration,
        String higherLevelsDescription,
        Integer higherLevelsDamageDiceCount,
        Integer higherLevelsDamageDiceSides,
        String grantedByItemKey,
        Integer chargeCost,
        Integer fixedSaveDc,
        SpellAdjustments adjustments,
        SpellUsage usage) {

    public Spell {
        adjustments = adjustments == null ? SpellAdjustments.NONE : adjustments;
    }

    /** A spell cast with slots or item charges. */
    public Spell(
            String key, String name, String className, int level, String school, String castingTime, String range,
            boolean concentration, boolean ritual, boolean attackRoll, Integer damageDiceCount,
            Integer damageDiceSides, String damageType, String notes, String effectSummary, boolean prepared,
            boolean alwaysPrepared, String description, String saveAbility, String components,
            String materialComponent, String duration, String higherLevelsDescription,
            Integer higherLevelsDamageDiceCount, Integer higherLevelsDamageDiceSides, String grantedByItemKey,
            Integer chargeCost, Integer fixedSaveDc, SpellAdjustments adjustments) {
        this(key, name, className, level, school, castingTime, range, concentration, ritual, attackRoll,
                damageDiceCount, damageDiceSides, damageType, notes, effectSummary, prepared, alwaysPrepared,
                description, saveAbility, components, materialComponent, duration, higherLevelsDescription,
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides, grantedByItemKey, chargeCost, fixedSaveDc,
                adjustments, null);
    }

    /** A spell without adjustments. */
    public Spell(
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
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides, grantedByItemKey, chargeCost, fixedSaveDc,
                null, null);
    }

    /** A normal, class-known spell — see {@code Dnd5eSpell}'s own matching constructor. */
    public Spell(
            String key, String name, String className, int level, String school, String castingTime, String range,
            boolean concentration, boolean ritual, boolean attackRoll, Integer damageDiceCount,
            Integer damageDiceSides, String damageType, String notes, String effectSummary, boolean prepared,
            boolean alwaysPrepared, String description, String saveAbility, String components,
            String materialComponent, String duration, String higherLevelsDescription,
            Integer higherLevelsDamageDiceCount, Integer higherLevelsDamageDiceSides) {
        this(key, name, className, level, school, castingTime, range, concentration, ritual, attackRoll,
                damageDiceCount, damageDiceSides, damageType, notes, effectSummary, prepared, alwaysPrepared,
                description, saveAbility, components, materialComponent, duration, higherLevelsDescription,
                higherLevelsDamageDiceCount, higherLevelsDamageDiceSides, null, null, null, null, null);
    }
}

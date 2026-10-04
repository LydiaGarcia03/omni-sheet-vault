package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Only what the vitals zone needs — no class, spells, or inventory yet. Validation has
 * no live caller in phase 3; it is ready for the day a client submits a sheet.
 * Armor/weapon/tool proficiencies and training are display strings, not modeled any
 * richer — nothing checks them against actions or items yet. Damage
 * resistances/immunities/vulnerabilities and condition immunities are display strings
 * too, for the same reason. {@code activeConditions} names which of the standard 5e
 * conditions are currently on the character — toggling them is a phase-6 mutation;
 * here they are read-only character data, per features/character-sheet.md's
 * "Condition: Toggled. In the first version it changes no other value."
 * {@code exhaustionLevel} tracks exhaustion separately (0-6, not a boolean),
 * mutable via {@link #withExhaustionLevel}. {@code currentHitPoints}, {@code temporaryHitPoints} and
 * {@code heroicInspiration} are the same story as conditions: real session state,
 * changing it (damage, healing, toggling inspiration) is phase 6 — here they are
 * read-only. {@code maxHitPointsAdjustment} is legacy: a stored value moves into the
 * {@code hitPoints} customizations as the Max HP Modifier, and the field reads 0.
 * {@code attacks} and {@code featureActions} feed the Actions tab (phase
 * 8); a feature action's usage track is mutable via {@link #withFeatureActions} —
 * spending/restoring a use is phase 9's resource model.
 * {@code armorProficiencies}/{@code weaponProficiencies}/{@code toolProficiencies}/
 * {@code languages} are mutable through the Collection editor (phase 8's third
 * mold) via {@link #withProficiencies}. {@code spellcastingClasses} and
 * {@code spells} feed the Spells tab (phase 8) — read-only display data this
 * phase: the known/prepared distinction, slot tracks and casting are phase 9.
 * {@code items} and the five coin fields feed the Inventory tab (phase 8),
 * mutable via {@link #withItems}/{@link #withCoins}. An item's
 * {@code equipped}/{@code attuned} flags are real state, but this phase does
 * not wire either into armor class or the Actions tab — see {@link Dnd5eItem}.
 * {@code featureTraits} feeds the Features and Traits tab (phase 8) — the full
 * catalog of class features, species traits and feats, mutable via
 * {@link #withFeatureTraits}; see {@link Dnd5eFeatureTrait} for how it differs
 * from {@code featureActions}.
 * {@code background} feeds the Background and Notes tab (phase 8) — every field
 * read-only this phase; see {@link Dnd5eBackground} for why editing is deferred.
 * {@code extras} feeds the Extras tab (phase 8) — mutable via {@link #withExtras};
 * see {@link Dnd5eExtra} for its hit-point mutation rules.
 * {@code hitDiceUsed} is phase 9's hit dice pool, spent via
 * {@link #withHitDiceSpend} — the pool's maximum (one die per character level)
 * is a mocked simplification, not stored; see {@link dev.omnisheetvault.api.ruleset.HitDice}.
 * {@code spellSlots} is phase 9's spell slot pool, one entry per level with
 * slots, mutable via {@link #withSpellSlots}; see {@link Dnd5eSpellSlotLevel}.
 * {@code specialSenses} lists fixed vision/perception senses the character has
 * natively (from species or a feature — e.g. a Mountain Dwarf's Darkvision),
 * each a {@link Dnd5eSenseType} plus its range in feet; unlike
 * {@code savingThrows}/{@code skills}, these are authored fixed values, not
 * derived from ability scores, and unlike {@code armorProficiencies} et al.
 * they are structured rather than a display string, so the Senses panel can
 * format a consistent label ("Darkvision 60 ft.") per entry. Read-only this
 * phase — no mutator exists, the same "no feature/species catalog to pick
 * from yet" reasoning as the Extras tab's missing "add an extra" UI.
 * {@code customActions} feeds the Actions tab's "Manage Custom" panel — punch
 * list item 7 (systems/dnd-5e/references/sheet-fidelity-audit.md), mutable via
 * {@link #withCustomActions}; see {@link Dnd5eCustomAction} for its shape and
 * how a {@code displayAsAttack} entry folds into {@code attacks} instead.
 * {@code trackEncumbrance} (weight/encumbrance initiative) is a per-character
 * setting, not locked at creation — whether carried weight is tracked at all;
 * when {@code true}, exceeding carrying capacity (Strength score x 15 lb) makes
 * the character Overloaded, which forces disadvantage on Strength ability
 * checks and Strength saving throws ({@link dev.omnisheetvault.api.dice.RollService}).
 * Mutable via {@link #withTrackEncumbrance}; missing from an already-persisted
 * sheet deserializes to {@code false} (Jackson's disabled
 * {@code FAIL_ON_NULL_FOR_PRIMITIVES}), matching D&D Beyond's own default.
 * {@code build} is what the character is built from (see {@link Dnd5eCharacterBuild});
 * {@code null} for a hand-authored sheet. {@code skillExpertise}, {@code classLevels},
 * {@code hitPointBase} (the hit die results per level, without Constitution) and
 * {@code derivation} are written when a build is materialized. {@code conditionModifiers}
 * (copied from the condition catalogue when a condition turns on) and
 * {@code activeEffects} (lasting spells) join the active modifiers (Stage C3); both are
 * null on sheets written before them. {@code deathSaves}, {@code experiencePoints} and
 * {@code appearance} are play state that survives re-materializing; each is null on
 * sheets written before it and reads as its default through the {@code OrDefault} accessors.
 */
public record Dnd5eSheet(
        @Min(1) @Max(30) int strength,
        @Min(1) @Max(30) int dexterity,
        @Min(1) @Max(30) int constitution,
        @Min(1) @Max(30) int intelligence,
        @Min(1) @Max(30) int wisdom,
        @Min(1) @Max(30) int charisma,
        @Min(1) @Max(20) int level,
        @Min(6) @Max(12) int hitDieSize,
        @Min(0) @Max(120) int speed,
        @NotNull Set<String> savingThrowProficiencies,
        @NotNull Set<String> skillProficiencies,
        @NotNull List<String> armorProficiencies,
        @NotNull List<String> weaponProficiencies,
        @NotNull List<String> toolProficiencies,
        @NotNull List<String> languages,
        @NotNull List<String> damageResistances,
        @NotNull List<String> damageImmunities,
        @NotNull List<String> damageVulnerabilities,
        @NotNull List<String> conditionImmunities,
        @NotNull Set<String> activeConditions,
        @Min(0) @Max(6) int exhaustionLevel,
        @Min(0) int currentHitPoints,
        @Min(0) int temporaryHitPoints,
        int maxHitPointsAdjustment,
        boolean heroicInspiration,
        @NotNull @Valid List<Dnd5eAttack> attacks,
        @NotNull @Valid List<Dnd5eFeatureAction> featureActions,
        @NotNull @Valid List<Dnd5eSpellcastingClass> spellcastingClasses,
        @NotNull @Valid List<Dnd5eSpell> spells,
        @NotNull @Valid List<Dnd5eItem> items,
        @Min(0) int copperPieces,
        @Min(0) int silverPieces,
        @Min(0) int electrumPieces,
        @Min(0) int goldPieces,
        @Min(0) int platinumPieces,
        @NotNull @Valid List<Dnd5eFeatureTrait> featureTraits,
        @NotNull @Valid Dnd5eBackground background,
        @NotNull @Valid List<Dnd5eExtra> extras,
        @Min(0) int hitDiceUsed,
        @NotNull @Valid List<Dnd5eSpellSlotLevel> spellSlots,
        @NotNull @Valid List<Dnd5eSpecialSense> specialSenses,
        @NotNull @Valid List<Dnd5eCustomAction> customActions,
        boolean trackEncumbrance,
        @Valid Dnd5eCharacterBuild build,
        Set<String> skillExpertise,
        @Valid List<Dnd5eClassLevel> classLevels,
        @Min(1) Integer hitPointBase,
        Dnd5eDerivation derivation,
        Map<String, List<@Valid Dnd5eModifier>> conditionModifiers,
        List<@Valid Dnd5eActiveEffect> activeEffects,
        @Valid Dnd5eDeathSaves deathSaves,
        @Min(0) Integer experiencePoints,
        @Valid Dnd5eAppearance appearance,
        @Valid Dnd5eCustomizations customizations,
        int schemaVersion) {

    /**
     * Version 2 added {@code build}, {@code skillExpertise}, {@code classLevels},
     * {@code hitPointBase} and {@code derivation}; version-1 sheets read with all of
     * them {@code null}.
     */
    public static final int SCHEMA_VERSION = 2;

    static final String MAX_HP_MODIFIER = "maxModifier";
    static final String OVERRIDE_MAX_HP = "overrideMax";

    /** The legacy max hit points adjustment becomes the Max HP Modifier customization. */
    public Dnd5eSheet {
        if (maxHitPointsAdjustment != 0) {
            Dnd5eCustomizations current = customizations == null ? Dnd5eCustomizations.NONE : customizations;
            if (current.hitPointsField(MAX_HP_MODIFIER).value() == null) {
                customizations = current.withHitPointsField(MAX_HP_MODIFIER, new Dnd5eNotedValue(maxHitPointsAdjustment, null));
            }
            maxHitPointsAdjustment = 0;
        }
    }

    /** A sheet without condition modifiers or active effects (both empty). */
    public Dnd5eSheet(
            int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma, int level,
            int hitDieSize, int speed, Set<String> savingThrowProficiencies, Set<String> skillProficiencies,
            List<String> armorProficiencies, List<String> weaponProficiencies, List<String> toolProficiencies,
            List<String> languages, List<String> damageResistances, List<String> damageImmunities,
            List<String> damageVulnerabilities, List<String> conditionImmunities, Set<String> activeConditions,
            int exhaustionLevel, int currentHitPoints, int temporaryHitPoints, int maxHitPointsAdjustment,
            boolean heroicInspiration, List<Dnd5eAttack> attacks, List<Dnd5eFeatureAction> featureActions,
            List<Dnd5eSpellcastingClass> spellcastingClasses, List<Dnd5eSpell> spells, List<Dnd5eItem> items,
            int copperPieces, int silverPieces, int electrumPieces, int goldPieces, int platinumPieces,
            List<Dnd5eFeatureTrait> featureTraits, Dnd5eBackground background, List<Dnd5eExtra> extras, int hitDiceUsed,
            List<Dnd5eSpellSlotLevel> spellSlots, List<Dnd5eSpecialSense> specialSenses, List<Dnd5eCustomAction> customActions,
            boolean trackEncumbrance, Dnd5eCharacterBuild build, Set<String> skillExpertise, List<Dnd5eClassLevel> classLevels,
            Integer hitPointBase, Dnd5eDerivation derivation, int schemaVersion) {
        this(strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies, toolProficiencies,
                languages, damageResistances, damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions,
                exhaustionLevel, currentHitPoints, temporaryHitPoints, maxHitPointsAdjustment, heroicInspiration, attacks,
                featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces, electrumPieces, goldPieces,
                platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions,
                trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, null, null, schemaVersion);
    }

    /** A sheet without death saves, experience points or appearance (all at their defaults). */
    public Dnd5eSheet(
            int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma, int level,
            int hitDieSize, int speed, Set<String> savingThrowProficiencies, Set<String> skillProficiencies,
            List<String> armorProficiencies, List<String> weaponProficiencies, List<String> toolProficiencies,
            List<String> languages, List<String> damageResistances, List<String> damageImmunities,
            List<String> damageVulnerabilities, List<String> conditionImmunities, Set<String> activeConditions,
            int exhaustionLevel, int currentHitPoints, int temporaryHitPoints, int maxHitPointsAdjustment,
            boolean heroicInspiration, List<Dnd5eAttack> attacks, List<Dnd5eFeatureAction> featureActions,
            List<Dnd5eSpellcastingClass> spellcastingClasses, List<Dnd5eSpell> spells, List<Dnd5eItem> items,
            int copperPieces, int silverPieces, int electrumPieces, int goldPieces, int platinumPieces,
            List<Dnd5eFeatureTrait> featureTraits, Dnd5eBackground background, List<Dnd5eExtra> extras, int hitDiceUsed,
            List<Dnd5eSpellSlotLevel> spellSlots, List<Dnd5eSpecialSense> specialSenses, List<Dnd5eCustomAction> customActions,
            boolean trackEncumbrance, Dnd5eCharacterBuild build, Set<String> skillExpertise, List<Dnd5eClassLevel> classLevels,
            Integer hitPointBase, Dnd5eDerivation derivation, Map<String, List<Dnd5eModifier>> conditionModifiers,
            List<Dnd5eActiveEffect> activeEffects, int schemaVersion) {
        this(strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies, toolProficiencies,
                languages, damageResistances, damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions,
                exhaustionLevel, currentHitPoints, temporaryHitPoints, maxHitPointsAdjustment, heroicInspiration, attacks,
                featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces, electrumPieces, goldPieces,
                platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions,
                trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers,
                activeEffects, null, null, null, schemaVersion);
    }

    /** A sheet without customizations (every value as calculated). */
    public Dnd5eSheet(
            int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma, int level,
            int hitDieSize, int speed, Set<String> savingThrowProficiencies, Set<String> skillProficiencies,
            List<String> armorProficiencies, List<String> weaponProficiencies, List<String> toolProficiencies,
            List<String> languages, List<String> damageResistances, List<String> damageImmunities,
            List<String> damageVulnerabilities, List<String> conditionImmunities, Set<String> activeConditions,
            int exhaustionLevel, int currentHitPoints, int temporaryHitPoints, int maxHitPointsAdjustment,
            boolean heroicInspiration, List<Dnd5eAttack> attacks, List<Dnd5eFeatureAction> featureActions,
            List<Dnd5eSpellcastingClass> spellcastingClasses, List<Dnd5eSpell> spells, List<Dnd5eItem> items,
            int copperPieces, int silverPieces, int electrumPieces, int goldPieces, int platinumPieces,
            List<Dnd5eFeatureTrait> featureTraits, Dnd5eBackground background, List<Dnd5eExtra> extras, int hitDiceUsed,
            List<Dnd5eSpellSlotLevel> spellSlots, List<Dnd5eSpecialSense> specialSenses, List<Dnd5eCustomAction> customActions,
            boolean trackEncumbrance, Dnd5eCharacterBuild build, Set<String> skillExpertise, List<Dnd5eClassLevel> classLevels,
            Integer hitPointBase, Dnd5eDerivation derivation, Map<String, List<Dnd5eModifier>> conditionModifiers,
            List<Dnd5eActiveEffect> activeEffects, Dnd5eDeathSaves deathSaves, Integer experiencePoints,
            Dnd5eAppearance appearance, int schemaVersion) {
        this(strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies, toolProficiencies,
                languages, damageResistances, damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions,
                exhaustionLevel, currentHitPoints, temporaryHitPoints, maxHitPointsAdjustment, heroicInspiration, attacks,
                featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces, electrumPieces, goldPieces,
                platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions,
                trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers,
                activeEffects, deathSaves, experiencePoints, appearance, null, schemaVersion);
    }

    public Dnd5eDeathSaves deathSavesOrDefault() {
        return deathSaves == null ? Dnd5eDeathSaves.NONE : deathSaves;
    }

    /** Never below the points the character's level starts at (PHB 2014, "Character Advancement"). */
    public int experiencePointsOrDefault() {
        return Math.max(experiencePoints == null ? 0 : experiencePoints, Dnd5eExperience.threshold(level));
    }

    public Dnd5eAppearance appearanceOrDefault() {
        return appearance == null ? Dnd5eAppearance.DEFAULT : appearance;
    }

    /** Each active condition's copied modifiers, keyed by condition ("exhaustion" holds the current level's); empty on older sheets. */
    public Map<String, List<Dnd5eModifier>> conditionModifiersOrEmpty() {
        return conditionModifiers == null ? Map.of() : conditionModifiers;
    }

    public List<Dnd5eActiveEffect> activeEffectsOrEmpty() {
        return activeEffects == null ? List.of() : activeEffects;
    }

    /** Replaces the condition state: active conditions, exhaustion and their copied modifiers. */
    Dnd5eSheet withConditions(Set<String> activeConditions, int exhaustionLevel, Map<String, List<Dnd5eModifier>> conditionModifiers) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses,
                customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers,
                activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    Dnd5eSheet withActiveEffects(List<Dnd5eActiveEffect> activeEffects) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses,
                customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers,
                activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    Dnd5eSheet withClassLevels(List<Dnd5eClassLevel> classLevels) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses,
                customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers,
                activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    Dnd5eSheet withHitDiceUsed(int hitDiceUsed) {
        return withHitDiceSpend(currentHitPoints, hitDiceUsed);
    }

    Dnd5eSheet withAbilityScores(int strength, int dexterity, int constitution, int intelligence, int wisdom, int charisma) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses,
                customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers,
                activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Skills at the EXPERT proficiency level; empty for a sheet written before expertise existed. */
    public Set<String> skillExpertiseOrEmpty() {
        return skillExpertise == null ? Set.of() : skillExpertise;
    }

    public List<Dnd5eClassLevel> classLevelsOrEmpty() {
        return classLevels == null ? List.of() : classLevels;
    }

    /** Replaces only the fields phase 6 makes mutable; everything else carries over. */
    Dnd5eSheet withSessionState(
            int currentHitPoints, int temporaryHitPoints, boolean heroicInspiration, Set<String> activeConditions) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the four proficiency/training lists; everything else carries over. */
    Dnd5eSheet withProficiencies(
            List<String> armorProficiencies, List<String> weaponProficiencies, List<String> toolProficiencies,
            List<String> languages) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the item list; everything else carries over. */
    Dnd5eSheet withItems(List<Dnd5eItem> items) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the five coin totals; everything else carries over. */
    Dnd5eSheet withCoins(int copperPieces, int silverPieces, int electrumPieces, int goldPieces, int platinumPieces) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the extras list; everything else carries over. */
    Dnd5eSheet withExtras(List<Dnd5eExtra> extras) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the feature actions list; everything else carries over. */
    Dnd5eSheet withFeatureActions(List<Dnd5eFeatureAction> featureActions) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the feature traits list; everything else carries over. */
    Dnd5eSheet withFeatureTraits(List<Dnd5eFeatureTrait> featureTraits) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only current hit points and the hit dice pool; everything else carries over. */
    Dnd5eSheet withHitDiceSpend(int currentHitPoints, int hitDiceUsed) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the spell slots list; everything else carries over. */
    Dnd5eSheet withSpellSlots(List<Dnd5eSpellSlotLevel> spellSlots) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the background; everything else carries over — phase 10's background/characteristics editing. */
    Dnd5eSheet withBackground(Dnd5eBackground background) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the spells list; everything else carries over — phase 9's "Manage spells". */
    Dnd5eSheet withSpells(List<Dnd5eSpell> spells) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the exhaustion level; everything else carries over. */
    Dnd5eSheet withExhaustionLevel(int exhaustionLevel) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only the custom actions list; everything else carries over — punch list item 7. */
    Dnd5eSheet withCustomActions(List<Dnd5eCustomAction> customActions) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    /** Replaces only whether carried weight is tracked; everything else carries over — weight/encumbrance initiative. */
    Dnd5eSheet withTrackEncumbrance(boolean trackEncumbrance) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses, customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers, activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }

    Dnd5eSheet withDeathSaves(Dnd5eDeathSaves deathSaves) {
        return withPlayState(deathSaves, experiencePoints, appearance, customizations);
    }

    Dnd5eSheet withExperiencePoints(int experiencePoints) {
        return withPlayState(deathSaves, experiencePoints, appearance, customizations);
    }

    Dnd5eSheet withAppearance(Dnd5eAppearance appearance) {
        return withPlayState(deathSaves, experiencePoints, appearance, customizations);
    }

    Dnd5eSheet withCustomizations(Dnd5eCustomizations customizations) {
        return withPlayState(deathSaves, experiencePoints, appearance, customizations);
    }

    public Dnd5eCustomizations customizationsOrEmpty() {
        return customizations == null ? Dnd5eCustomizations.NONE : customizations;
    }

    /** Carries a previous sheet's play state (death saves, experience, appearance, customizations) onto this one. */
    Dnd5eSheet withPlayStateOf(Dnd5eSheet previous) {
        return withPlayState(previous.deathSaves(), previous.experiencePoints(), previous.appearance(), previous.customizations());
    }

    private Dnd5eSheet withPlayState(
            Dnd5eDeathSaves deathSaves, Integer experiencePoints, Dnd5eAppearance appearance, Dnd5eCustomizations customizations) {
        return new Dnd5eSheet(
                strength, dexterity, constitution, intelligence, wisdom, charisma, level, hitDieSize, speed,
                savingThrowProficiencies, skillProficiencies, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, damageResistances, damageImmunities, damageVulnerabilities,
                conditionImmunities, activeConditions, exhaustionLevel, currentHitPoints, temporaryHitPoints,
                maxHitPointsAdjustment, heroicInspiration,
                attacks, featureActions, spellcastingClasses, spells, items, copperPieces, silverPieces,
                electrumPieces, goldPieces, platinumPieces, featureTraits, background, extras, hitDiceUsed, spellSlots, specialSenses,
                customActions, trackEncumbrance, build, skillExpertise, classLevels, hitPointBase, derivation, conditionModifiers,
                activeEffects, deathSaves, experiencePoints, appearance, customizations, schemaVersion);
    }
}

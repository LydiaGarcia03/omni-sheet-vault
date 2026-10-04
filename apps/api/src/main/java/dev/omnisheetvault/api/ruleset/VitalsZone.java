package dev.omnisheetvault.api.ruleset;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * What the sheet's vitals zone needs — see roadmap.md phase 3 and 4. Ability names
 * are plain strings, not a shared enum: which abilities exist is a per-system concept
 * (adr-0003), not something to fix before a second system proves what is actually
 * common. {@code abilityScores} and {@code speed} are stored data, not derived
 * values — no formula, no contributions to trace — so they are plain, unlike the
 * {@link CalculatedValue} fields. {@code attacks} and {@code featureActions} feed the
 * Actions tab (phase 8) — see {@link AttackRow} and {@link FeatureAction}.
 * {@code spellcasting} and {@code spells} feed the Spells tab (phase 8) — see
 * {@link SpellcastingClassInfo} and {@link Spell}. {@code items} and
 * {@code coins} feed the Inventory tab (phase 8) — see {@link Item} and
 * {@link Coins}. {@code featureTraits} feeds the Features and Traits tab
 * (phase 8) — see {@link FeatureTrait}. {@code background} feeds the
 * Background and Notes tab (phase 8) — see {@link Background}. {@code extras}
 * feeds the Extras tab (phase 8) — see {@link Extra}. {@code hitDice} is phase
 * 9's hit dice pool — see {@link HitDice} for why its maximum is mocked.
 * {@code spellSlots} is phase 9's spell slot pool, one entry per level with
 * slots — see {@link SpellSlotLevel}. {@code specialSenses} lists fixed
 * vision/perception senses the character has natively (species or a feature) —
 * see {@link SpecialSense}. {@code customActions} is every player-authored custom
 * action not already folded into {@code attacks} — see {@link CustomAction}.
 * {@code attacksPerAction} is how many attacks the Attack action makes (Extra Attack).
 * {@code rollModes} is the advantage/disadvantage the sheet forces, keyed by roll:
 * {@code ATTACK}, {@code INITIATIVE}, {@code ABILITY_CHECK:<ability>},
 * {@code SAVING_THROW:<ability>}, {@code SKILL_CHECK:<skill>}; rolls with no forced mode
 * are absent. {@code activeEffects} lists the lasting effects on the character — see
 * {@link ActiveEffectInfo}. {@code provenance} explains the build-derived values — see
 * {@link Provenance}; null when the system has none to offer. {@code rollNotes} lists the
 * situational roll modifiers shown but never applied — see {@link RollNote}.
 * {@code deathSaves} are the death saving throws — see {@link DeathSaves}. {@code experience}
 * is the character's advancement — see {@link Experience}; null for a system without one.
 * {@code sheetTheme} is the id of the theme the sheet is drawn in; null for the system default.
 * {@code customSkills} are the skills the player added — see {@link CustomSkill}. {@code defenses} lists every
 * defense with its source — see {@link DefenseEntry}. {@code calculatedMaxHitPoints} is the hit point maximum before the
 * player's own modifier or override ({@code hitPoints} holds the one in effect).
 */
public record VitalsZone(
        Map<String, Integer> abilityScores,
        Map<String, CalculatedValue> abilityModifiers,
        CalculatedValue proficiencyBonus,
        CalculatedValue armorClass,
        CalculatedValue initiative,
        CalculatedValue hitPoints,
        int speed,
        int level,
        Map<String, CalculatedValue> savingThrows,
        Map<String, String> savingThrowProficiencies,
        Map<String, CalculatedValue> senses,
        List<String> armorProficiencies,
        List<String> weaponProficiencies,
        List<String> toolProficiencies,
        List<String> languages,
        Map<String, CalculatedValue> skills,
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
        Map<String, AttackRow> attacks,
        List<FeatureAction> featureActions,
        List<SpellcastingClassInfo> spellcasting,
        List<Spell> spells,
        List<Item> items,
        Coins coins,
        List<FeatureTrait> featureTraits,
        Background background,
        List<Extra> extras,
        HitDice hitDice,
        List<SpellSlotLevel> spellSlots,
        List<SpecialSense> specialSenses,
        List<CustomAction> customActions,
        Encumbrance encumbrance,
        int attacksPerAction,
        Map<String, RollModeInfo> rollModes,
        List<ActiveEffectInfo> activeEffects,
        Provenance provenance,
        List<RollNote> rollNotes,
        DeathSaves deathSaves,
        Experience experience,
        String sheetTheme,
        List<CustomSkill> customSkills,
        List<DefenseEntry> defenses) {

    public VitalsZone {
        customSkills = customSkills == null ? List.of() : List.copyOf(customSkills);
        defenses = defenses == null ? List.of() : List.copyOf(defenses);
    }

    /** A vitals zone without custom skills or detailed defenses. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance, int attacksPerAction,
            Map<String, RollModeInfo> rollModes, List<ActiveEffectInfo> activeEffects, Provenance provenance,
            List<RollNote> rollNotes, DeathSaves deathSaves, Experience experience, String sheetTheme) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, attacksPerAction, rollModes, activeEffects, provenance, rollNotes,
                deathSaves, experience, sheetTheme, null, null);
    }

    /** A vitals zone drawn in the system's default theme. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance, int attacksPerAction,
            Map<String, RollModeInfo> rollModes, List<ActiveEffectInfo> activeEffects, Provenance provenance,
            List<RollNote> rollNotes, DeathSaves deathSaves, Experience experience) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, attacksPerAction, rollModes, activeEffects, provenance, rollNotes,
                deathSaves, experience, null);
    }

    /** A vitals zone with no advancement to show. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance, int attacksPerAction,
            Map<String, RollModeInfo> rollModes, List<ActiveEffectInfo> activeEffects, Provenance provenance,
            List<RollNote> rollNotes, DeathSaves deathSaves) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, attacksPerAction, rollModes, activeEffects, provenance, rollNotes,
                deathSaves, null);
    }

    /** A vitals zone whose character isn't dying. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance, int attacksPerAction,
            Map<String, RollModeInfo> rollModes, List<ActiveEffectInfo> activeEffects, Provenance provenance,
            List<RollNote> rollNotes) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, attacksPerAction, rollModes, activeEffects, provenance, rollNotes,
                DeathSaves.NOT_DYING, null);
    }

    /** A vitals zone with no roll notes. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance, int attacksPerAction,
            Map<String, RollModeInfo> rollModes, List<ActiveEffectInfo> activeEffects, Provenance provenance) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, attacksPerAction, rollModes, activeEffects, provenance, List.of());
    }

    /** A vitals zone with no provenance. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance, int attacksPerAction,
            Map<String, RollModeInfo> rollModes, List<ActiveEffectInfo> activeEffects) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, attacksPerAction, rollModes, activeEffects, null);
    }

    /** A vitals zone with no active effects. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance, int attacksPerAction,
            Map<String, RollModeInfo> rollModes) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, attacksPerAction, rollModes, List.of());
    }

    /** A vitals zone whose character makes one attack per Attack action. */
    public VitalsZone(
            Map<String, Integer> abilityScores, Map<String, CalculatedValue> abilityModifiers, CalculatedValue proficiencyBonus,
            CalculatedValue armorClass, CalculatedValue initiative, CalculatedValue hitPoints, int speed, int level,
            Map<String, CalculatedValue> savingThrows, Map<String, String> savingThrowProficiencies,
            Map<String, CalculatedValue> senses, List<String> armorProficiencies, List<String> weaponProficiencies,
            List<String> toolProficiencies, List<String> languages, Map<String, CalculatedValue> skills,
            Map<String, String> skillProficiencies, Map<String, String> skillGoverningAbilities,
            List<String> damageResistances, List<String> damageImmunities, List<String> damageVulnerabilities,
            List<String> conditionImmunities, Set<String> activeConditions, int exhaustionLevel, int currentHitPoints,
            int temporaryHitPoints, int calculatedMaxHitPoints, boolean heroicInspiration, Map<String, AttackRow> attacks,
            List<FeatureAction> featureActions, List<SpellcastingClassInfo> spellcasting, List<Spell> spells,
            List<Item> items, Coins coins, List<FeatureTrait> featureTraits, Background background, List<Extra> extras,
            HitDice hitDice, List<SpellSlotLevel> spellSlots, List<SpecialSense> specialSenses,
            List<CustomAction> customActions, Encumbrance encumbrance) {
        this(abilityScores, abilityModifiers, proficiencyBonus, armorClass, initiative, hitPoints, speed, level,
                savingThrows, savingThrowProficiencies, senses, armorProficiencies, weaponProficiencies,
                toolProficiencies, languages, skills, skillProficiencies, skillGoverningAbilities, damageResistances,
                damageImmunities, damageVulnerabilities, conditionImmunities, activeConditions, exhaustionLevel,
                currentHitPoints, temporaryHitPoints, calculatedMaxHitPoints, heroicInspiration, attacks, featureActions,
                spellcasting, spells, items, coins, featureTraits, background, extras, hitDice, spellSlots,
                specialSenses, customActions, encumbrance, 1, Map.of(), List.of());
    }
}

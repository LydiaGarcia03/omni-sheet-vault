package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;

/** What a modifier changes; names follow D&D Beyond's {@code ModifierSubTypeEnum}. Grows by adding values, never by content-specific code. */
public enum Dnd5eModifierTarget {
    /** Armor class, always. */
    ARMOR_CLASS,
    /** Armor class while wearing armor. */
    ARMORED_ARMOR_CLASS,
    /** Base armor class while wearing no armor; Dexterity is added on top. */
    UNARMORED_ARMOR_CLASS,
    /** Hit point maximum, per character or class level. */
    HIT_POINTS_PER_LEVEL,
    /** Extra attacks when taking the Attack action. */
    EXTRA_ATTACKS,
    /** Hit point maximum as a whole (exhaustion halves it; Aid raises it). */
    HIT_POINT_MAXIMUM,
    /** Walking speed. */
    SPEED,
    /** Every attack roll, weapon or spell. */
    ATTACK_ROLLS,
    /** Every ability check, skills and initiative included. */
    ABILITY_CHECKS,
    /** Strength ability checks, Athletics included. */
    STRENGTH_ABILITY_CHECKS,
    /** Every saving throw. */
    SAVING_THROWS,
    /** Strength saving throws. */
    STRENGTH_SAVING_THROWS,
    /** Dexterity saving throws. */
    DEXTERITY_SAVING_THROWS,
    /** Stealth checks only (armor with stealth disadvantage). */
    STEALTH_CHECKS,
    /** Spell attack bonus of every spellcasting class. */
    SPELL_ATTACKS,
    /** Spell save DC of every spellcasting class. */
    SPELL_SAVE_DC,
    /** An ability score itself ({@code SET}: the score becomes the value unless already higher). */
    STRENGTH_SCORE,
    DEXTERITY_SCORE,
    CONSTITUTION_SCORE,
    INTELLIGENCE_SCORE,
    WISDOM_SCORE,
    CHARISMA_SCORE;

    /** The skill-specific check target for a skill key ("stealth"), if the vocabulary has one. */
    public static Optional<Dnd5eModifierTarget> checksOf(String skill) {
        String name = skill.replaceAll("([A-Z])", "_$1").toUpperCase(Locale.ROOT) + "_CHECKS";
        return Arrays.stream(values()).filter(target -> target.name().equals(name)).findFirst();
    }

    /** The score target for an ability key ("strength"). */
    public static Dnd5eModifierTarget scoreOf(String ability) {
        return valueOf(ability.toUpperCase(Locale.ROOT) + "_SCORE");
    }
}

package dev.omnisheetvault.api.ruleset;

/**
 * One ability score row in an extra's stat block — see {@code Dnd5eExtraAbilityScore}.
 * {@code modifier} is computed from {@code score} by the calculator (same formula as
 * the character's own ability scores); {@code save} is a plain pass-through.
 */
public record ExtraAbilityScore(
        String abilityKey,
        int score,
        int modifier,
        int save) {
}

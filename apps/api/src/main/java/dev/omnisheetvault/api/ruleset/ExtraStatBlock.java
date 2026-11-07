package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * The full structured stat block behind an extra's Entity Detail panel — see
 * {@code Dnd5eExtraStatBlock}. Nothing here is calculated except each ability
 * score's {@code modifier} (folded into {@link ExtraAbilityScore} itself); the
 * rest is a plain pass-through of stored data. {@code challengeXp} and {@code challengeProficiencyBonus} are the
 * challenge rating's experience points and proficiency bonus, null when the rating isn't one the system knows.
 */
public record ExtraStatBlock(
        String size,
        String creatureType,
        String alignment,
        int initiativeBonus,
        String hitDiceLabel,
        String additionalSpeeds,
        List<ExtraAbilityScore> abilityScores,
        List<ExtraSkill> skills,
        String senses,
        String languages,
        String challengeRating,
        List<ExtraStatEntry> traits,
        List<ExtraStatEntry> actions,
        List<ExtraStatEntry> reactions,
        List<ExtraStatEntry> legendaryActions,
        Integer challengeXp,
        Integer challengeProficiencyBonus) {

    public ExtraStatBlock(String size, String creatureType, String alignment, int initiativeBonus, String hitDiceLabel,
            String additionalSpeeds, List<ExtraAbilityScore> abilityScores, List<ExtraSkill> skills, String senses, String languages,
            String challengeRating, List<ExtraStatEntry> traits, List<ExtraStatEntry> actions) {
        this(size, creatureType, alignment, initiativeBonus, hitDiceLabel, additionalSpeeds, abilityScores, skills, senses, languages,
                challengeRating, traits, actions, List.of(), List.of(), null, null);
    }
}

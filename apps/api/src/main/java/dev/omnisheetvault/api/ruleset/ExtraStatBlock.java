package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * The full structured stat block behind an extra's Entity Detail panel — see
 * {@code Dnd5eExtraStatBlock}. Nothing here is calculated except each ability
 * score's {@code modifier} (folded into {@link ExtraAbilityScore} itself); the
 * rest is a plain pass-through of stored data.
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
        List<ExtraStatEntry> actions) {
}

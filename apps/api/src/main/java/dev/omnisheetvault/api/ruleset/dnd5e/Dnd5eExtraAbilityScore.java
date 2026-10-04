package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * One ability score row in an extra's stat block — see {@link Dnd5eExtraStatBlock}.
 * {@code save} is authored, not derived: unlike the character's own saving throws,
 * an extra's proficiencies are fixed monster-manual numbers, not computed from a
 * proficiency bonus this app tracks — same "fixed base numbers only" simplification
 * already taken for weapon attacks and spell damage. {@code modifier} is not stored
 * here; {@link Dnd5eSheetCalculator} derives it from {@code score} with the same
 * formula used for the character's own ability scores.
 */
public record Dnd5eExtraAbilityScore(
        @NotBlank String abilityKey,
        @Min(1) @Max(30) int score,
        int save) {
}

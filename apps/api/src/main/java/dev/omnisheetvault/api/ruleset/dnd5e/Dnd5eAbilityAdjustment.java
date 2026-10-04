package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * A player's manual correction to one ability score, D&D Beyond's "Other Modifier" and
 * "Override Score": {@code otherModifier} adds to the calculated total, and
 * {@code overrideScore} replaces the total outright. Either may be null.
 */
public record Dnd5eAbilityAdjustment(@Min(-20) @Max(20) Integer otherModifier, @Min(1) @Max(30) Integer overrideScore) {
}

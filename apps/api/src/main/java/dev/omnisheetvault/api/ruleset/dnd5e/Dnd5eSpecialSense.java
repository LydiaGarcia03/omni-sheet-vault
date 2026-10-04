package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * One fixed sense a character has natively (species or a feature), not derived
 * from ability scores — e.g. a Mountain Dwarf's Darkvision 60 ft. Structured
 * (type plus range) rather than a free-text line, so {@link Dnd5eSheetCalculator}
 * can format a consistent label per entry instead of trusting authored text.
 */
public record Dnd5eSpecialSense(@NotNull Dnd5eSenseType type, @Min(0) @Max(120) int rangeFeet) {
}

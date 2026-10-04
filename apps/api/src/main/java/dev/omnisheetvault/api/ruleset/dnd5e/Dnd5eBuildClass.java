package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** One class a character has levels in; {@code subclassSlug} stays {@code null} until the subclass is chosen. */
public record Dnd5eBuildClass(
        @NotBlank String classSlug,
        String subclassSlug,
        @Min(1) @Max(20) int level) {
}

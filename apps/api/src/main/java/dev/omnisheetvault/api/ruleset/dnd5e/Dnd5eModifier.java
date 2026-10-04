package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One mechanical effect of a feature, feat or other source (adr-0007). {@code ability},
 * when set, adds that ability's modifier to {@code value} (Unarmored Defense's
 * Constitution). A per-level target counts character levels, or the levels of
 * {@code classSlug} when set (Draconic Resilience counts Sorcerer levels).
 * {@code source} labels the contribution it produces. {@code restriction}, when set
 * ("against poison"), makes it situational: it is shown as a note, never applied.
 */
public record Dnd5eModifier(
        @NotNull Dnd5eModifierType type,
        @NotNull Dnd5eModifierTarget target,
        int value,
        String ability,
        String classSlug,
        @NotBlank String source,
        String restriction) {

    public Dnd5eModifier(Dnd5eModifierType type, Dnd5eModifierTarget target, int value, String ability, String classSlug, String source) {
        this(type, target, value, ability, classSlug, source, null);
    }

    public boolean situational() {
        return restriction != null && !restriction.isBlank();
    }
}
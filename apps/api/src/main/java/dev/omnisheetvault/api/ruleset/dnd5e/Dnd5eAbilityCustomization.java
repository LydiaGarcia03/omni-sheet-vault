package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * D&D Beyond's ability pane fields: {@code otherModifier} adds to the calculated score, and {@code overrideScore}
 * replaces it entirely. Null means not set.
 */
public record Dnd5eAbilityCustomization(@Min(-30) @Max(30) Integer otherModifier, @Min(1) @Max(30) Integer overrideScore) {

    public static final Dnd5eAbilityCustomization NONE = new Dnd5eAbilityCustomization(null, null);

    public boolean isEmpty() {
        return otherModifier == null && overrideScore == null;
    }
}

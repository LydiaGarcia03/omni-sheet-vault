package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * D&D Beyond's Customize fields for an item: a to-hit override that replaces the attack bonus, to-hit and damage
 * bonuses that add to it, cost (gp) and weight (lb) overrides, the Silvered / Adamantine markers, Display As Attack
 * (a weapon shows among the attacks even when not equipped), and a name and notes over its own. Null means not set.
 */
public record Dnd5eItemCustomization(
        @Min(-99) @Max(99) Integer toHitOverride,
        @Min(-99) @Max(99) Integer toHitBonus,
        @Min(-99) @Max(99) Integer damageBonus,
        @DecimalMin("0") @DecimalMax("1000000") Double costOverride,
        @DecimalMin("0") @DecimalMax("100000") Double weightOverride,
        Boolean silvered,
        Boolean adamantine,
        Boolean displayAsAttack,
        @Size(max = 128) String name,
        @Size(max = 500) String notes) {

    public static final Dnd5eItemCustomization NONE =
            new Dnd5eItemCustomization(null, null, null, null, null, false, false, false, null, null);

    public Dnd5eItemCustomization {
        silvered = Boolean.TRUE.equals(silvered);
        adamantine = Boolean.TRUE.equals(adamantine);
        displayAsAttack = Boolean.TRUE.equals(displayAsAttack);
        name = blankToNull(name);
        notes = blankToNull(notes);
    }

    boolean isEmpty() {
        return equals(NONE);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }
}

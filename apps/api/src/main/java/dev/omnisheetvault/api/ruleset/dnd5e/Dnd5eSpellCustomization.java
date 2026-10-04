package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * D&D Beyond's Customize fields for a spell: a to-hit override that replaces the spell attack bonus, to-hit and damage
 * bonuses that add to it, a save DC override and bonus, Display As Attack (the spell shows among the attacks), and a
 * name and notes over its own. Null means not set.
 */
public record Dnd5eSpellCustomization(
        @Min(-99) @Max(99) Integer toHitOverride,
        @Min(-99) @Max(99) Integer toHitBonus,
        @Min(-99) @Max(99) Integer damageBonus,
        @Min(0) @Max(99) Integer dcOverride,
        @Min(-99) @Max(99) Integer dcBonus,
        Boolean displayAsAttack,
        @Size(max = 128) String name,
        @Size(max = 500) String notes) {

    public static final Dnd5eSpellCustomization NONE = new Dnd5eSpellCustomization(null, null, null, null, null, false, null, null);

    public Dnd5eSpellCustomization {
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

package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * D&D Beyond's Customize fields for a saving throw or a skill: an override that replaces the total, magic and misc
 * bonuses that add to it, a proficiency level over the calculated one, and (skills only) the ability it uses; each
 * with its source notes. Null means not set.
 */
public record Dnd5eCheckCustomization(
        @Min(-99) @Max(99) Integer override,
        @Size(max = 200) String overrideNotes,
        @Min(-99) @Max(99) Integer magicBonus,
        @Size(max = 200) String magicBonusNotes,
        @Min(-99) @Max(99) Integer miscBonus,
        @Size(max = 200) String miscBonusNotes,
        Dnd5eProficiencyLevel proficiencyLevel,
        @Size(max = 200) String proficiencyLevelNotes,
        String statOverride,
        @Size(max = 200) String statOverrideNotes) {

    public static final Dnd5eCheckCustomization NONE = new Dnd5eCheckCustomization(null, null, null, null, null, null, null, null, null, null);

    public Dnd5eCheckCustomization {
        overrideNotes = blankToNull(overrideNotes);
        magicBonusNotes = blankToNull(magicBonusNotes);
        miscBonusNotes = blankToNull(miscBonusNotes);
        proficiencyLevelNotes = blankToNull(proficiencyLevelNotes);
        statOverrideNotes = blankToNull(statOverrideNotes);
    }

    boolean isEmpty() {
        return equals(NONE);
    }

    /** The magic and misc bonuses together, 0 when neither is set. */
    int addedBonus() {
        return (magicBonus == null ? 0 : magicBonus) + (miscBonus == null ? 0 : miscBonus);
    }

    private static String blankToNull(String text) {
        return text == null || text.isBlank() ? null : text.strip();
    }
}

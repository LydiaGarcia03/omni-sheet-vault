package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * A skill the player adds to the sheet (D&D Beyond's "Custom Skills"): its name, the ability it uses (none when
 * {@code statOverride} is null), its proficiency level, the same override and bonuses as any skill, and its notes and
 * description.
 */
public record Dnd5eCustomSkill(
        @NotBlank @Size(max = 64) String key,
        @NotBlank @Size(max = 128) String name,
        String statOverride,
        Dnd5eProficiencyLevel proficiencyLevel,
        @Min(-99) @Max(99) Integer override,
        @Min(-99) @Max(99) Integer magicBonus,
        @Min(-99) @Max(99) Integer miscBonus,
        @Size(max = 200) String notes,
        @Size(max = 2000) String description) {

    public Dnd5eCustomSkill {
        proficiencyLevel = proficiencyLevel == null ? Dnd5eProficiencyLevel.FULL : proficiencyLevel;
    }

    /** A new custom skill as D&D Beyond adds it: named after its position, proficient, with no ability. */
    static Dnd5eCustomSkill added(String key, int position) {
        return new Dnd5eCustomSkill(key, "Custom Skill " + position, null, Dnd5eProficiencyLevel.FULL, null, null, null, null, null);
    }

    /** The same skill with the editable fields of {@code edited}, keeping its own key. */
    Dnd5eCustomSkill editedAs(Dnd5eCustomSkill edited) {
        return new Dnd5eCustomSkill(key, edited.name(), edited.statOverride(), edited.proficiencyLevel(), edited.override(),
                edited.magicBonus(), edited.miscBonus(), edited.notes(), edited.description());
    }
}

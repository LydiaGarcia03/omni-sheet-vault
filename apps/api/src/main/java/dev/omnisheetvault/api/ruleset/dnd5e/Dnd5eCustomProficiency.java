package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * A proficiency the player adds by hand (D&D Beyond's "Add New Proficiencies"): an existing armor, weapon, tool or
 * language picked from the catalogue, or a custom tool or language the player names; with source notes. A custom one
 * may have no name yet, and then grants nothing.
 */
public record Dnd5eCustomProficiency(
        @NotBlank @Size(max = 64) String key,
        @NotNull Type type,
        @Size(max = 100) String name,
        boolean custom,
        @Size(max = 200) String notes) {

    public enum Type { ARMOR, WEAPON, TOOL, LANGUAGE }

    public Dnd5eCustomProficiency {
        name = name == null ? "" : name.strip();
        notes = notes == null || notes.isBlank() ? null : notes.strip();
    }

    /** D&D Beyond offers custom entries for tools and languages only. */
    boolean isOffered() {
        return !custom || type == Type.TOOL || type == Type.LANGUAGE;
    }

    Dnd5eCustomProficiency withNameAndNotes(String name, String notes) {
        return new Dnd5eCustomProficiency(key, type, custom ? name : this.name, custom, notes);
    }
}

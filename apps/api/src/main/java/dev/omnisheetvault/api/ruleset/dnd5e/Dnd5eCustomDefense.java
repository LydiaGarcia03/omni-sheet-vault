package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * A defense the player adds by hand (D&D Beyond's Defenses "Customize"): a resistance, immunity or vulnerability to one
 * of the thirteen damage types, or an immunity to one of the conditions D&D Beyond lists; with source notes.
 */
public record Dnd5eCustomDefense(
        @NotBlank @Size(max = 64) String key,
        @NotNull Type type,
        @NotBlank String subtype,
        @Size(max = 200) String notes) {

    public enum Type { RESISTANCE, IMMUNITY, VULNERABILITY }

    /** PHB 2014, "Damage Types". */
    static final List<String> DAMAGE_TYPES = List.of(
            "acid", "bludgeoning", "cold", "fire", "force", "lightning", "necrotic", "piercing", "poison", "psychic",
            "radiant", "slashing", "thunder");

    /** The conditions D&D Beyond offers as immunities. */
    static final List<String> CONDITIONS = List.of(
            "blinded", "charmed", "deafened", "exhaustion", "frightened", "grappled", "incapacitated", "invisible",
            "paralyzed", "petrified", "poisoned", "prone", "restrained", "stunned", "unconscious");

    public Dnd5eCustomDefense {
        notes = notes == null || notes.isBlank() ? null : notes.strip();
    }

    /** A damage type for every type; a condition only for an immunity. */
    boolean isOffered() {
        return DAMAGE_TYPES.contains(subtype) || (type == Type.IMMUNITY && CONDITIONS.contains(subtype));
    }

    boolean isCondition() {
        return CONDITIONS.contains(subtype);
    }
}

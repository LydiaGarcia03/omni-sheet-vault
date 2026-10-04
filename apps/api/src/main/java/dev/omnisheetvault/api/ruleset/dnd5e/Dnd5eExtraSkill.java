package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotBlank;

/** One skill bonus in an extra's stat block — see {@link Dnd5eExtraStatBlock}. Authored, not derived. */
public record Dnd5eExtraSkill(
        @NotBlank String name,
        int bonus) {
}

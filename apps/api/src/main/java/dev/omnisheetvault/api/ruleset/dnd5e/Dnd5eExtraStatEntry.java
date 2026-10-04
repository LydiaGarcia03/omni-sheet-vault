package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One named trait or action in an extra's stat block — see
 * {@link Dnd5eExtraStatBlock}. Plain prose, same treatment as a feature's own
 * description: an action's to-hit or damage (e.g. "Melee Attack Roll: +4") is
 * part of the text, not a separate roll target — confirmed live against D&D
 * Beyond's own Extras panel, which renders these as continuous prose with no
 * roll button of its own, the same inert treatment this app already gives the
 * Actions tab's Standard Actions.
 */
public record Dnd5eExtraStatEntry(
        @NotBlank String name,
        @NotNull String description) {
}

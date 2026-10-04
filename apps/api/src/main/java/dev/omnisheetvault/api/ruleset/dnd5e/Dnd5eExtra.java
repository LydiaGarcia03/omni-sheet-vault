package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A familiar, mount, summoned creature or vehicle linked to the character — see
 * systems/dnd-5e/sheet-ui.md's Extras tab. {@code armorClass} and {@code speed}
 * are the creature's own stored stats, not derived from the character's ability
 * scores, so they are plain ints — same treatment as {@code Dnd5eSheet}'s own
 * {@code speed} — the row reads them directly, and {@link Dnd5eExtraStatBlock}'s
 * own detail panel reads the same values rather than duplicating them.
 * {@code statBlock} was free text through phase 9; phase 10 slice 10 replaced
 * it with a structured shape — see {@link Dnd5eExtraStatBlock}'s own doc comment.
 * {@code currentHitPoints}/{@code temporaryHitPoints} are real, mutable session
 * state, per the tab's own spec ("editable hit points for that instance") —
 * mirroring {@code Dnd5eSheet}'s own damage/heal/temporary-hit-points rules
 * exactly, scoped to this one extra instead of the whole character.
 */
public record Dnd5eExtra(
        @NotBlank String key,
        @NotBlank String name,
        @NotNull Dnd5eExtraCategory category,
        @Min(0) int armorClass,
        @Min(0) int maxHitPoints,
        @Min(0) int currentHitPoints,
        @Min(0) int temporaryHitPoints,
        @Min(0) int speed,
        @NotNull @Valid Dnd5eExtraStatBlock statBlock) {
}

package dev.omnisheetvault.api.dice;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * A player-picked, no-source roll from the dice tray — see the manual/custom rolling
 * rule in features/character-sheet.md ("follow the same path, with no source
 * attached"). One entry per die type the player selected a count for.
 */
record ManualRollRequest(@NotEmpty @Valid List<DiceGroupRequest> dice) {

    /** {@code count} is capped at 20 per type — our own DoS-safe ceiling, not a rule from any game system. */
    record DiceGroupRequest(@NotNull DieType type, @Min(1) @Max(20) int count) {
    }
}

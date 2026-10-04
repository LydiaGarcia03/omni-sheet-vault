package dev.omnisheetvault.api.dice;

import java.time.Instant;
import java.util.UUID;

/** {@code dropped} lists the indexes in {@code results} that don't count toward {@code total} (e.g. the lowest die of 4d6kh3). */
record RollResponse(UUID id, String expression, String context, int[] results, int[] dropped, int total, Instant rolledAt) {

    static RollResponse from(Roll roll) {
        return new RollResponse(roll.id(), roll.expression(), roll.context(), roll.results(),
                KeptDice.droppedFor(roll.expression(), roll.results()), roll.total(), roll.rolledAt());
    }
}

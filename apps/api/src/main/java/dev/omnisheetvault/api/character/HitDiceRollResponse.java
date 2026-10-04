package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.dice.Roll;
import java.time.Instant;
import java.util.UUID;

/**
 * Same shape as {@code dice.RollResponse}, duplicated rather than shared: that
 * type is package-private to {@code dice}, and a hit-dice spend's roll rides
 * along inside {@link SpendHitDiceResponse} instead of the normal
 * {@code POST /rolls} endpoint (see {@code RollService#spendHitDice}'s doc
 * comment for why the roll and the sheet mutation happen together).
 */
record HitDiceRollResponse(UUID id, String expression, String context, int[] results, int total, Instant rolledAt) {

    static HitDiceRollResponse from(Roll roll) {
        return new HitDiceRollResponse(roll.id(), roll.expression(), roll.context(), roll.results(), roll.total(), roll.rolledAt());
    }
}

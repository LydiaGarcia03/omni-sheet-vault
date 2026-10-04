package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Min;

/** {@code dieSize} picks which dice to spend; null spends the largest size with dice left. */
record SpendHitDiceRequest(@Min(1) int count, Integer dieSize) {
}
package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Min;

record CoinAdjustmentRequest(@Min(1) int amount) {
}

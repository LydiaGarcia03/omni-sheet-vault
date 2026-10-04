package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Min;

/**
 * Shared by damage, healing and temporary hit points — the endpoint path carries the intent.
 * {@code critical} marks damage from a critical hit; absent means false, and only damage reads it.
 */
record HitPointAdjustmentRequest(@Min(1) int amount, Boolean critical) {

    boolean isCritical() {
        return Boolean.TRUE.equals(critical);
    }
}

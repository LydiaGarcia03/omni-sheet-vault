package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/** {@code onSelf} omitted means the caster is the target; {@code pact} omitted spends a regular slot. */
record CastSpellRequest(@Min(0) @Max(9) int slotLevel, Boolean onSelf, Boolean pact) {

    boolean onSelfOrDefault() {
        return onSelf == null || onSelf;
    }

    boolean pactOrDefault() {
        return Boolean.TRUE.equals(pact);
    }
}

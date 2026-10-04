package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/** One hand-set number with its source notes (a passive score override, a sense's distance, an AC bonus); null value: not set. */
public record Dnd5eNotedValue(@Min(-99) @Max(999) Integer value, @Size(max = 200) String notes) {

    public static final Dnd5eNotedValue NONE = new Dnd5eNotedValue(null, null);

    public Dnd5eNotedValue {
        notes = notes == null || notes.isBlank() ? null : notes.strip();
    }

    boolean isEmpty() {
        return value == null && notes == null;
    }
}

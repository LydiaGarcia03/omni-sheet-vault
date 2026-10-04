package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import java.util.stream.Stream;

/** Exactly one of {@code add}, {@code remove} or {@code set}, in experience points. */
record ExperienceRequest(@Min(0) Integer add, @Min(0) Integer remove, @Min(0) Integer set) {

    @AssertTrue(message = "give exactly one of add, remove or set")
    boolean isExactlyOneChange() {
        return Stream.of(add, remove, set).filter(value -> value != null).count() == 1;
    }

    /** The new total from the current one; never below 0. */
    int applyTo(int current) {
        if (set != null) {
            return set;
        }
        return add != null ? current + add : Math.max(0, current - remove);
    }
}

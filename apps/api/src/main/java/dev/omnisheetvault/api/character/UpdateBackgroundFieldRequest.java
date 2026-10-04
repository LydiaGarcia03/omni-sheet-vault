package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotNull;

/** {@code value} may be blank (clearing a field is valid) but never null. */
record UpdateBackgroundFieldRequest(@NotNull String value) {
}

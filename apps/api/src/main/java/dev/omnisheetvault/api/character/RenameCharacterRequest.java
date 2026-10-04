package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A character's new name, at most 128 characters as on D&D Beyond. */
record RenameCharacterRequest(@NotBlank @Size(max = 128) String name) {
}

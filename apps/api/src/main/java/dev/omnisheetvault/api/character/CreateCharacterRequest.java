package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;

public record CreateCharacterRequest(@NotBlank String name, @NotBlank String systemId) {
}

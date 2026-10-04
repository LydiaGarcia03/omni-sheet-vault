package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;

record AppearanceRequest(@NotBlank String theme) {
}

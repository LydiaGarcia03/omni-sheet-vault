package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;

record MoveItemRequest(@NotBlank String storageLocation) {
}

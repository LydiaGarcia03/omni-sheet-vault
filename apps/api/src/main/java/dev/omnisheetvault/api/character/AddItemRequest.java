package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

record AddItemRequest(
        @NotBlank String name, @Min(1) int quantity, @NotNull String cost, @NotNull String notes,
        boolean requiresAttunement, String storageLocation) {
}

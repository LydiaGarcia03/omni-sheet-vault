package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;

/** {@code catalogueEntryId} identifies which real 5etools item to copy onto the sheet — see CharacterSheetService#addCatalogueItem. */
public record AddCatalogueItemRequest(@NotBlank String catalogueEntryId) {
}

package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** A catalogue creature to add as extras: which entry, under which category, how many copies. */
public record AddCatalogueExtraRequest(@NotBlank String catalogueEntryId, @NotBlank String category, @Min(1) @Max(20) int quantity) {
}

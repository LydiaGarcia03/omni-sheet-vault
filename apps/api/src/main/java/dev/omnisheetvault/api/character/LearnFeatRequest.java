package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;

/** {@code catalogueEntryId} identifies which feat — see CharacterSheetService#learnFeat. */
public record LearnFeatRequest(@NotBlank String catalogueEntryId) {
}

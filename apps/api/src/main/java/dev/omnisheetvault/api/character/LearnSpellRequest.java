package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;

/** {@code catalogueEntryId} identifies which spell; {@code className} which of the character's own spellcasting classes it is learned under — see CharacterSheetService#learnSpell. */
public record LearnSpellRequest(@NotBlank String catalogueEntryId, @NotBlank String className) {
}

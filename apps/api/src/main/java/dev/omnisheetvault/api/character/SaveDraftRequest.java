package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import tools.jackson.databind.JsonNode;

/** The whole draft, sent on every autosave; {@code build} is the game system's own build document. */
public record SaveDraftRequest(@NotBlank @Size(max = 120) String name, @NotNull JsonNode build) {
}

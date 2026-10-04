package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * The answer to one {@link dev.omnisheetvault.api.ruleset.CreationChoice}: its deterministic {@code id} (e.g.
 * {@code class:fighter:1:fighting-style}) and the option keys selected.
 */
public record Dnd5eBuildChoice(
        @NotBlank String id,
        @NotNull List<@NotBlank String> selections) {
}

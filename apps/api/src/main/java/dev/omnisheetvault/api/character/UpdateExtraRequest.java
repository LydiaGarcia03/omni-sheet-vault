package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Size;

/** An extra's own name and notes, from its Customize section; a blank name keeps the current one. */
public record UpdateExtraRequest(@Size(max = 128) String name, @Size(max = 500) String notes) {
}

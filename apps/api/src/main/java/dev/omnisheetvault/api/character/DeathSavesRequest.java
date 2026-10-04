package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

record DeathSavesRequest(@Min(0) @Max(3) int successes, @Min(0) @Max(3) int failures) {
}

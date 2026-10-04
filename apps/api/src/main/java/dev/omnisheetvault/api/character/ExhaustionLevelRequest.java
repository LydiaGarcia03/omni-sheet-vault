package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

record ExhaustionLevelRequest(@Min(0) @Max(6) int level) {
}

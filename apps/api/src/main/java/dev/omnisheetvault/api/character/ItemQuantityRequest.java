package dev.omnisheetvault.api.character;

import jakarta.validation.constraints.Min;

record ItemQuantityRequest(@Min(1) int quantity) {
}

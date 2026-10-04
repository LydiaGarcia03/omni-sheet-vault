package dev.omnisheetvault.api.ruleset.dnd5e;

import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

/**
 * {@code Dnd5eSheet} is stored as a JSONB payload that evolves without Flyway
 * migrations (see ground-rules.md) — a new primitive field (e.g.
 * {@code Dnd5eItem.requiresAttunement}) is simply absent on rows persisted
 * before it existed. The shared, Spring-managed {@code ObjectMapper} fails
 * that read outright ({@code FAIL_ON_NULL_FOR_PRIMITIVES}, on by default),
 * since it's also used for strict HTTP request-body validation elsewhere and
 * that strictness shouldn't loosen globally. {@link Dnd5eSheetMutator} and
 * {@link Dnd5eSheetCalculator} instead read/write through a mapper rebuilt
 * from the shared one with only that one feature disabled, so an absent
 * primitive defaults to its type's zero value instead of throwing.
 */
final class Dnd5eSheetJsonMapper {

    private Dnd5eSheetJsonMapper() {
    }

    static ObjectMapper lenient(ObjectMapper base) {
        return base.rebuild().disable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES).build();
    }
}

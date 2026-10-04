package dev.omnisheetvault.api.ruleset;

/**
 * One of a character's storage buckets (e.g. D&D 5e's Equipment/Backpack/Bag of
 * Holding) — see {@link Encumbrance}. {@code capacityKg} is {@code null} for a
 * bucket with no maximum (Equipment) — a section only ever appears here when the
 * system actually offers it for the current character (e.g. Bag of Holding is
 * absent unless owned and attuned).
 */
public record StorageSection(String location, int itemCount, double weightKg, Double capacityKg) {
}

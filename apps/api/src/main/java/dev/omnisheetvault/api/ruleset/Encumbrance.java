package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * Carried weight, only meaningful when {@code trackWeight} is on — a per-character
 * setting (D&D 5e: {@code Dnd5eSheet.trackEncumbrance}), not locked at creation.
 * {@code carriedWeightKg} excludes a Bag of Holding's own contents (an
 * interdimensional space, not physically carried) and Other Possessions (not
 * physically carried either) — see {@link StorageSection}'s doc comment for which
 * buckets exist. {@code overloaded} is {@code false} whenever {@code trackWeight} is
 * off, even if the raw numbers would otherwise exceed {@code capacityKg} — the whole
 * point of the setting is that an untracked character never suffers the consequence.
 */
public record Encumbrance(boolean trackWeight, double carriedWeightKg, double capacityKg, boolean overloaded, List<StorageSection> sections) {
}

package dev.omnisheetvault.api.ruleset;

/** One labelled line of a character card, e.g. {@code ("Classes", "Rogue 4 / Sorcerer 3")}. */
public record SummaryFact(String label, String value) {
}

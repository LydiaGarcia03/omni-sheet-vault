package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * A derived value is never just a number — see features/character-sheet.md. It is
 * always this value plus the labelled contributions that produced it.
 */
public record CalculatedValue(int value, List<Contribution> contributions) {
}

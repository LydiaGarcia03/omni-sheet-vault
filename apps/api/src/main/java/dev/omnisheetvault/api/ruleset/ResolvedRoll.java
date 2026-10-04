package dev.omnisheetvault.api.ruleset;

/**
 * What to roll and why, not the result — {@link RollKind} plus a modifier resolved
 * server-side, never trusted from the client. {@code context} is the human-readable
 * trigger recorded with the roll, e.g. "Strength: check".
 */
public record ResolvedRoll(int diceCount, int diceSides, int modifier, String context) {
}

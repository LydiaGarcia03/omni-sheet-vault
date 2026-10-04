package dev.omnisheetvault.api.ruleset;

/**
 * A situational roll modifier the sheet shows but never applies, because it depends on the circumstance
 * ({@code restriction}: "against poison"). {@code mode} is e.g. {@code ADVANTAGE}; {@code target} the
 * rolls it concerns, e.g. {@code SAVING_THROWS}; {@code source} names where it comes from.
 */
public record RollNote(String mode, String target, String restriction, String source) {
}
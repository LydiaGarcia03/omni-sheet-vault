package dev.omnisheetvault.api.ruleset;

/** One class the character has levels in; {@code subclass} is null until one is chosen. */
public record ClassLevelInfo(String name, String subclass, int level) {
}

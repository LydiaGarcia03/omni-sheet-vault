package dev.omnisheetvault.api.ruleset;

/**
 * One of the character's defenses with where it comes from: {@code type} is RESISTANCE, IMMUNITY, VULNERABILITY or
 * CONDITION_IMMUNITY; {@code source} names what grants it (null when unknown); a {@code custom} one was added by the
 * player, with its {@code key} and {@code notes}.
 */
public record DefenseEntry(String key, String type, String name, String source, boolean custom, String notes) {
}

package dev.omnisheetvault.api.ruleset;

/** One named trait or action in an extra's stat block — see {@code Dnd5eExtraStatEntry}. Plain pass-through. */
public record ExtraStatEntry(String name, String description) {
}

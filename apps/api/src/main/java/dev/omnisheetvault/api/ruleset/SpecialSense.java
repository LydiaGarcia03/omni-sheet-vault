package dev.omnisheetvault.api.ruleset;

/**
 * One fixed vision/perception sense the character has natively — see VitalsZone.
 * {@code type} is a plain string, not a shared enum, same reasoning as ability
 * names (adr-0003) — which sense types exist is a per-system concept.
 * {@code label} is the formatted display line ("Darkvision 60 ft."), built once
 * server-side so every consumer renders it identically instead of re-deriving
 * the "Type Range ft." format from {@code type}/{@code rangeFeet} separately.
 */
public record SpecialSense(String type, int rangeFeet, String label) {
}

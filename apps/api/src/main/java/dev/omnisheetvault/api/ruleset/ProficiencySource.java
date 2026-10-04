package dev.omnisheetvault.api.ruleset;

/** Where a proficiency shown on the sheet came from: {@code label} as listed ("Light", "Thieves' Tools"), {@code source} ("Rogue 1"). */
public record ProficiencySource(String kind, String label, String source) {
}

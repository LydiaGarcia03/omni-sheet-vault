package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * How much of the proficiency bonus a roll adds — D&D Beyond's own four levels
 * ({@code ProficiencyLevelEnum}): half is rounded down unless the source says to round up.
 */
public enum Dnd5eProficiencyLevel {
    NONE,
    HALF,
    FULL,
    EXPERT;

    int bonus(int proficiencyBonus, boolean roundHalfUp) {
        return switch (this) {
            case NONE -> 0;
            case HALF -> roundHalfUp ? (proficiencyBonus + 1) / 2 : proficiencyBonus / 2;
            case FULL -> proficiencyBonus;
            case EXPERT -> proficiencyBonus * 2;
        };
    }

    String contributionLabel() {
        return switch (this) {
            case NONE -> "";
            case HALF -> "Half proficiency bonus";
            case FULL -> "Proficiency bonus";
            case EXPERT -> "Proficiency bonus ×2 (Expertise)";
        };
    }
}

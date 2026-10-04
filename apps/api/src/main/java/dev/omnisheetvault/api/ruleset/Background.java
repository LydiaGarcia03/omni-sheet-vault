package dev.omnisheetvault.api.ruleset;

/**
 * Background and characteristics, plus free-text notes — see VitalsZone. Every
 * field is a plain pass-through of stored data; nothing here is calculated.
 * {@code gender} through {@code lifestyle} are phase 10's addition, matching
 * D&D Beyond's own combined "Characteristics and Details" panel.
 */
public record Background(
        String name,
        String featureName,
        String featureDescription,
        String alignment,
        String personalityTraits,
        String ideals,
        String bonds,
        String flaws,
        String appearance,
        String organizations,
        String allies,
        String enemies,
        String backstory,
        String other,
        String gender,
        String eyes,
        String size,
        String height,
        String faith,
        String hair,
        String skin,
        String age,
        String weight,
        String lifestyle) {
}

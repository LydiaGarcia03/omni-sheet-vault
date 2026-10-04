package dev.omnisheetvault.api.ruleset.dnd5e;

/**
 * The {@code Dnd5eBackground} fields editable via
 * {@link Dnd5eSheetMutator#updateBackgroundField} — every field except
 * {@code name}/{@code featureName}/{@code featureDescription}, which identify
 * the chosen background itself (a phase 11 concern). See
 * {@code Dnd5eBackground}'s own doc comment for which of these share the
 * combined "Characteristics and Details" panel on the frontend versus opening
 * individually — that split is a presentation concern, not modeled here.
 */
public enum Dnd5eBackgroundField {
    ALIGNMENT,
    PERSONALITY_TRAITS,
    IDEALS,
    BONDS,
    FLAWS,
    APPEARANCE,
    ORGANIZATIONS,
    ALLIES,
    ENEMIES,
    BACKSTORY,
    OTHER,
    GENDER,
    EYES,
    SIZE,
    HEIGHT,
    FAITH,
    HAIR,
    SKIN,
    AGE,
    WEIGHT,
    LIFESTYLE
}

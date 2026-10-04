package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotNull;

/**
 * Background and characteristics, plus free-text notes — see
 * systems/dnd-5e/sheet-ui.md's Background and notes tab (a merged tab; D&D Beyond has
 * these as two). Every field is free text. {@code name}/{@code featureName}/
 * {@code featureDescription} identify the chosen background itself — changing
 * background entirely is a phase 11 (character creation) concern, so these
 * three stay read-only. Every other field is editable via
 * {@link Dnd5eSheetMutator#updateBackgroundField} (phase 10) — confirmed live
 * against D&D Beyond that {@code alignment} and the nine fields from
 * {@code gender} through {@code weight}, plus {@code lifestyle} (found live,
 * not in the original audit's nine), share one combined "Characteristics and
 * Details" sidebar panel, while {@code personalityTraits}/{@code ideals}/
 * {@code bonds}/{@code flaws}/{@code appearance}/{@code organizations}/
 * {@code allies}/{@code enemies}/{@code backstory}/{@code other} each open
 * their own single-field panel. None of these ten new fields carry mechanics
 * (same "display strings" treatment {@code alignment} already had) — a
 * dropdown-backed enum for e.g. alignment or lifestyle was considered and
 * rejected for consistency with the existing free-text fields.
 */
public record Dnd5eBackground(
        @NotNull String name,
        @NotNull String featureName,
        @NotNull String featureDescription,
        @NotNull String alignment,
        @NotNull String personalityTraits,
        @NotNull String ideals,
        @NotNull String bonds,
        @NotNull String flaws,
        @NotNull String appearance,
        @NotNull String organizations,
        @NotNull String allies,
        @NotNull String enemies,
        @NotNull String backstory,
        @NotNull String other,
        @NotNull String gender,
        @NotNull String eyes,
        @NotNull String size,
        @NotNull String height,
        @NotNull String faith,
        @NotNull String hair,
        @NotNull String skin,
        @NotNull String age,
        @NotNull String weight,
        @NotNull String lifestyle) {

    /**
     * Returns a copy with one field changed — the single seam
     * {@link Dnd5eSheetMutator#updateBackgroundField} uses, so a 21-branch
     * switch lives here once instead of being duplicated at the call site.
     */
    Dnd5eBackground withField(Dnd5eBackgroundField field, String value) {
        String alignment = this.alignment;
        String personalityTraits = this.personalityTraits;
        String ideals = this.ideals;
        String bonds = this.bonds;
        String flaws = this.flaws;
        String appearance = this.appearance;
        String organizations = this.organizations;
        String allies = this.allies;
        String enemies = this.enemies;
        String backstory = this.backstory;
        String other = this.other;
        String gender = this.gender;
        String eyes = this.eyes;
        String size = this.size;
        String height = this.height;
        String faith = this.faith;
        String hair = this.hair;
        String skin = this.skin;
        String age = this.age;
        String weight = this.weight;
        String lifestyle = this.lifestyle;

        switch (field) {
            case ALIGNMENT -> alignment = value;
            case PERSONALITY_TRAITS -> personalityTraits = value;
            case IDEALS -> ideals = value;
            case BONDS -> bonds = value;
            case FLAWS -> flaws = value;
            case APPEARANCE -> appearance = value;
            case ORGANIZATIONS -> organizations = value;
            case ALLIES -> allies = value;
            case ENEMIES -> enemies = value;
            case BACKSTORY -> backstory = value;
            case OTHER -> other = value;
            case GENDER -> gender = value;
            case EYES -> eyes = value;
            case SIZE -> size = value;
            case HEIGHT -> height = value;
            case FAITH -> faith = value;
            case HAIR -> hair = value;
            case SKIN -> skin = value;
            case AGE -> age = value;
            case WEIGHT -> weight = value;
            case LIFESTYLE -> lifestyle = value;
        }

        return new Dnd5eBackground(
                name, featureName, featureDescription, alignment, personalityTraits, ideals, bonds, flaws,
                appearance, organizations, allies, enemies, backstory, other, gender, eyes, size, height, faith,
                hair, skin, age, weight, lifestyle);
    }
}

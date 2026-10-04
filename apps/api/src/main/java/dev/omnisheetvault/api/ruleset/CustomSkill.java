package dev.omnisheetvault.api.ruleset;

/**
 * A skill the player added to the sheet, with its calculated bonus: the ability it uses ({@code abilityKey} null when
 * none), its proficiency level, and the player's notes and description.
 */
public record CustomSkill(
        String key,
        String name,
        String abilityKey,
        String proficiencyLevel,
        CalculatedValue value,
        String notes,
        String description) {
}

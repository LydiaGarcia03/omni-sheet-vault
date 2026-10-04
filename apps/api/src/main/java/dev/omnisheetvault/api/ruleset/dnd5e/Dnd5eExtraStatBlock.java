package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * The full structured stat block behind an extra's Entity Detail panel — see
 * {@link Dnd5eExtra} and systems/dnd-5e/sheet-ui.md's Extras section. Phase 10
 * slice 10: replaces the earlier free-text {@code statBlock} field, confirmed
 * live against D&D Beyond's own Extras panel (a Cat familiar) to carry size,
 * type, alignment, initiative, hit dice, ability scores with saves, skills,
 * senses, languages, challenge rating, and named trait/action entries — the
 * same structure the reference renders, not an approximation of it.
 * {@code armorClass}/{@code maxHitPoints}/{@code speed} stay on {@link Dnd5eExtra}
 * itself, unchanged — the row already reads them from there, and the detail
 * panel reads the same values rather than duplicating them here.
 * {@code additionalSpeeds} is free text for anything beyond the base walking
 * speed (e.g. "Climb 40 ft."), null when there is none. {@code senses},
 * {@code languages} and {@code challengeRating} stay single free-text fields
 * rather than decomposed further — nothing else in this app references them,
 * same reasoning {@code Dnd5eFeatureTrait} gives for not tracking a source/page
 * field. Initiative is deliberately not authored here: unlike a stored monster
 * stat, it is always the creature's own Dexterity modifier (2014 rules, which
 * this app follows per "No 2024 content" in systems/dnd-5e/sheet-ui.md's deviations
 * list) — {@link Dnd5eSheetCalculator} derives it from {@code abilityScores}'
 * own "dexterity" entry, the same way it derives the character's initiative.
 */
public record Dnd5eExtraStatBlock(
        @NotBlank String size,
        @NotBlank String creatureType,
        @NotBlank String alignment,
        @NotBlank String hitDiceLabel,
        String additionalSpeeds,
        @NotNull @Valid List<Dnd5eExtraAbilityScore> abilityScores,
        @NotNull @Valid List<Dnd5eExtraSkill> skills,
        @NotBlank String senses,
        @NotBlank String languages,
        @NotBlank String challengeRating,
        @NotNull @Valid List<Dnd5eExtraStatEntry> traits,
        @NotNull @Valid List<Dnd5eExtraStatEntry> actions) {
}

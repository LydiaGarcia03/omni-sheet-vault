package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A weapon (or weapon-like) attack the character can make — see
 * systems/dnd-5e/sheet-ui.md's Actions tab. {@code abilityModifierKey} names which ability
 * governs both the hit bonus and the damage bonus (finesse and ranged weapons use
 * dexterity, most melee weapons use strength) — a character-authoring choice, not a
 * formula, so it is stored rather than derived. Proficiency is assumed for every
 * listed attack; there is no weapon-proficiency check yet, the same simplification
 * already taken for saving throws and skills before their own proficiency flags
 * existed. {@code category} is free text shown as the row's subtitle on D&D Beyond
 * (e.g. "Melee Weapon", "Ranged Weapon") and also selects the row's icon
 * (`AttackRow.tsx`'s keyword match) — not an enum, same free-text treatment as
 * {@code damageType}/{@code notes}, since this app has no weapon catalog to derive
 * it from. {@code damageDiceCount} of {@code 0} (direct owner request,
 * 2026-09-04, Unarmed Strike) means no dice at all — pure {@code
 * abilityModifierKey} damage, still a clickable roll (recorded in the log
 * and shown in the roll popup, per {@code RollService}'s own zero-dice
 * handling), just with nothing random about it. {@code damageDiceSides} is
 * meaningless at that point (never rolled) but stays {@code @Min(1)} —
 * pick any positive placeholder.
 */
public record Dnd5eAttack(
        @NotBlank String key,
        @NotBlank String name,
        @NotBlank String range,
        @NotBlank String abilityModifierKey,
        @Min(0) int damageDiceCount,
        @Min(1) int damageDiceSides,
        @NotBlank String damageType,
        @NotBlank String category,
        @NotNull String notes) {
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * One spellcasting class a character has — see systems/dnd-5e/sheet-ui.md's Spells
 * tab: "a multiclass character has one set per class." {@code abilityKey} names
 * which ability governs this class's spellcasting (e.g. wisdom for a cleric,
 * intelligence for an eldritch knight) — a character-authoring choice, not a
 * formula, so it is stored rather than derived. {@code castingType} is phase 9's
 * "Manage spells" model — see {@link Dnd5eSpellCastingType}. {@code cantripsKnownMax}
 * applies to both casting types (zero for a class that grants none, e.g. Ranger);
 * {@code spellsKnownMax} bounds {@link Dnd5eSheetMutator#learnSpell} for a
 * {@code KNOWN} class and is unused (null) for a {@code PREPARED} one — matching
 * D&D Beyond, a spellbook has no hard numeric cap in this app any more than in the
 * rules; {@code spellsPreparedMax} bounds {@link Dnd5eSheetMutator#prepareSpell} for
 * a {@code PREPARED} class and is unused (null) for a {@code KNOWN} one, which has no
 * separate preparation step at all. Same "stored, not derived" treatment as
 * {@code abilityKey} — none of these four numbers are computed from a class table.
 */
public record Dnd5eSpellcastingClass(
        @NotBlank String className,
        @NotBlank String abilityKey,
        @NotNull Dnd5eSpellCastingType castingType,
        @Min(0) int cantripsKnownMax,
        @Min(0) Integer spellsKnownMax,
        @Min(0) Integer spellsPreparedMax) {
}

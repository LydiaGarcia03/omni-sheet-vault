package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * One spell slot level's pool — see systems/dnd-5e/sheet-ui.md's Spells tab slot
 * track. {@code level} is 1-9; cantrips (spell level 0) have no slots and are not
 * represented here. Spent and restored through
 * {@link Dnd5eSheetMutator#consumeSpellSlot}/{@link Dnd5eSheetMutator#restoreSpellSlot},
 * matching {@code Dnd5eFeatureAction}'s box-track treatment rather than hit dice's
 * roll-and-validate one: consuming a slot is a plain resource spend, no roll
 * attached, so an already-exhausted level clamps at zero instead of throwing.
 * {@code pact} marks the Warlock's Pact Magic pool, kept apart from the spellcasting
 * slots (it may share a level with them) and regained on a short rest too;
 * {@code className} names the class it belongs to (null for the regular slots).
 */
public record Dnd5eSpellSlotLevel(
        @Min(1) @Max(9) int level,
        @Min(0) int maxSlots,
        @Min(0) int usedSlots,
        boolean pact,
        String className) {

    /** A spellcasting slot pool (not Pact Magic). */
    public Dnd5eSpellSlotLevel(int level, int maxSlots, int usedSlots) {
        this(level, maxSlots, usedSlots, false, null);
    }

    boolean matches(int level, boolean pact) {
        return this.level == level && this.pact == pact;
    }

    Dnd5eSpellSlotLevel withUsedSlots(int usedSlots) {
        return new Dnd5eSpellSlotLevel(level, maxSlots, usedSlots, pact, className);
    }
}

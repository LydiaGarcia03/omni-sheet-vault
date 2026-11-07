package dev.omnisheetvault.api.ruleset;

import dev.omnisheetvault.api.shared.LocalizedProblem;

/**
 * Thrown when preparing a spell would exceed the class's own prepared-spell cap —
 * see features/character-sheet.md's mutation table ("Prepared spells: Preparing and
 * unpreparing, within the limits of each class") and
 * {@code Dnd5eSpellcastingClass#spellsPreparedMax}.
 */
public class SpellPreparationLimitExceededException extends RuntimeException implements LocalizedProblem {

    private final int limit;

    public SpellPreparationLimitExceededException(int limit) {
        super("Cannot prepare more than " + limit + " spells");
        this.limit = limit;
    }

    @Override
    public String messageCode() {
        return "error.spellPreparationLimit";
    }

    @Override
    public Object[] messageArguments() {
        return new Object[] {limit};
    }
}

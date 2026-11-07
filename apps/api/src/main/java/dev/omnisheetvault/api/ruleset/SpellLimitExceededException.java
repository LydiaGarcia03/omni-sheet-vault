package dev.omnisheetvault.api.ruleset;

import dev.omnisheetvault.api.shared.LocalizedProblem;

/**
 * Thrown when learning a spell would exceed the class's own cantrip or known-spell
 * cap — see features/character-sheet.md's mutation table and
 * {@code Dnd5eSpellcastingClass}'s {@code cantripsKnownMax}/{@code spellsKnownMax}.
 */
public class SpellLimitExceededException extends RuntimeException implements LocalizedProblem {

    private final int limit;

    public SpellLimitExceededException(int limit) {
        super("Cannot know more than " + limit + " of this kind of spell");
        this.limit = limit;
    }

    @Override
    public String messageCode() {
        return "error.spellLimit";
    }

    @Override
    public Object[] messageArguments() {
        return new Object[] {limit};
    }
}

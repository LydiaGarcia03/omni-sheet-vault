package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.shared.LocalizedProblem;

/** The level up endpoints were used on a character with no level up in progress. */
public class LevelUpNotStartedException extends RuntimeException implements LocalizedProblem {

    private final String characterName;

    LevelUpNotStartedException(String characterName) {
        super(characterName + " has no level up in progress.");
        this.characterName = characterName;
    }

    @Override
    public String messageCode() {
        return "error.levelUpNotStarted";
    }

    @Override
    public Object[] messageArguments() {
        return new Object[] {characterName};
    }
}

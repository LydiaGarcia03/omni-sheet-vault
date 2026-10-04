package dev.omnisheetvault.api.character;

/** The level up endpoints were used on a character with no level up in progress. */
public class LevelUpNotStartedException extends RuntimeException {

    LevelUpNotStartedException(String characterName) {
        super(characterName + " has no level up in progress.");
    }
}

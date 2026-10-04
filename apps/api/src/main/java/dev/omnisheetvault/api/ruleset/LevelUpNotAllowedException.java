package dev.omnisheetvault.api.ruleset;

/** A level up that can't be started: the top level, a character with no build, an unknown class or unmet prerequisites. */
public class LevelUpNotAllowedException extends RuntimeException {

    public LevelUpNotAllowedException(String message) {
        super(message);
    }
}

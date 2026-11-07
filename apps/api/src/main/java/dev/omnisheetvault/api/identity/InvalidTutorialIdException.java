package dev.omnisheetvault.api.identity;

public class InvalidTutorialIdException extends RuntimeException {

    InvalidTutorialIdException(String tutorialId) {
        super("Not a tutorial id: " + tutorialId);
    }
}

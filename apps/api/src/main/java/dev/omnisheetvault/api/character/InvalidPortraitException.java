package dev.omnisheetvault.api.character;

public class InvalidPortraitException extends RuntimeException {

    InvalidPortraitException(String message) {
        super(message);
    }
}

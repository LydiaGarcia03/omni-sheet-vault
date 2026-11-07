package dev.omnisheetvault.api.character;

public class FeatNotAllowedException extends RuntimeException {

    FeatNotAllowedException(String message) {
        super(message);
    }
}

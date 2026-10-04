package dev.omnisheetvault.api.character;

import java.util.UUID;

/** A sheet operation on a character that is still a creation draft. */
public class CharacterIsDraftException extends RuntimeException {

    public CharacterIsDraftException(UUID characterId) {
        super("Character " + characterId + " is still a draft; finish creating it first");
    }
}

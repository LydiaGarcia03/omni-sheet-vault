package dev.omnisheetvault.api.character;

import java.util.UUID;

/** A creation-flow operation on a character that has already been finished. */
public class CharacterNotDraftException extends RuntimeException {

    public CharacterNotDraftException(UUID characterId) {
        super("Character " + characterId + " is not a draft");
    }
}

package dev.omnisheetvault.api.character;

import java.util.UUID;

public class CharacterNotFoundException extends RuntimeException {

    public CharacterNotFoundException(UUID characterId) {
        super("No character found with id " + characterId);
    }
}

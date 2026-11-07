package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.shared.LocalizedProblem;
import java.util.UUID;

public class CharacterNotFoundException extends RuntimeException implements LocalizedProblem {

    public CharacterNotFoundException(UUID characterId) {
        super("No character found with id " + characterId);
    }

    @Override
    public String messageCode() {
        return "error.characterNotFound";
    }
}

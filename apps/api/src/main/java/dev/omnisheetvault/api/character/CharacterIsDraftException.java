package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.shared.LocalizedProblem;
import java.util.UUID;

/** A sheet operation on a character that is still a creation draft. */
public class CharacterIsDraftException extends RuntimeException implements LocalizedProblem {

    public CharacterIsDraftException(UUID characterId) {
        super("Character " + characterId + " is still a draft; finish creating it first");
    }

    @Override
    public String messageCode() {
        return "error.characterIsDraft";
    }
}

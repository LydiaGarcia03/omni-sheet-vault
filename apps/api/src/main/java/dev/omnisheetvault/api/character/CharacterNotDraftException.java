package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.shared.LocalizedProblem;
import java.util.UUID;

/** A creation-flow operation on a character that has already been finished. */
public class CharacterNotDraftException extends RuntimeException implements LocalizedProblem {

    public CharacterNotDraftException(UUID characterId) {
        super("Character " + characterId + " is not a draft");
    }

    @Override
    public String messageCode() {
        return "error.characterNotDraft";
    }
}

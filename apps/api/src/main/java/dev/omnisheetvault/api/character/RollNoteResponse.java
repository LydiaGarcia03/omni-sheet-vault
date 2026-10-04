package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.RollNote;

public record RollNoteResponse(String mode, String target, String restriction, String source) {

    static RollNoteResponse from(RollNote note) {
        return new RollNoteResponse(note.mode(), note.target(), note.restriction(), note.source());
    }
}
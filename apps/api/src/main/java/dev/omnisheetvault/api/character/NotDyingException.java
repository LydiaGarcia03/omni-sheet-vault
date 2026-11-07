package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.shared.LocalizedProblem;

/** A death saving throw was asked for a character who isn't dying (above 0 hit points, stable or dead). */
public class NotDyingException extends RuntimeException implements LocalizedProblem {

    NotDyingException() {
        super("The character isn't making death saving throws.");
    }

    @Override
    public String messageCode() {
        return "error.notDying";
    }
}

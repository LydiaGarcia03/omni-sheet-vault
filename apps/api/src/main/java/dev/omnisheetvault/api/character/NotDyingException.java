package dev.omnisheetvault.api.character;

/** A death saving throw was asked for a character who isn't dying (above 0 hit points, stable or dead). */
public class NotDyingException extends RuntimeException {

    NotDyingException() {
        super("The character isn't making death saving throws.");
    }
}

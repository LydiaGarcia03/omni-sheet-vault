package dev.omnisheetvault.api.ruleset;

/** A sheet theme id the system doesn't offer. */
public class InvalidSheetThemeException extends RuntimeException {

    public InvalidSheetThemeException(String theme) {
        super("Unknown sheet theme: \"" + theme + "\"");
    }
}

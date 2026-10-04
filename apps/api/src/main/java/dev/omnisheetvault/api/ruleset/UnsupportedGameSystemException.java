package dev.omnisheetvault.api.ruleset;

public class UnsupportedGameSystemException extends RuntimeException {

    public UnsupportedGameSystemException(String systemId) {
        super("Unsupported game system: " + systemId);
    }
}

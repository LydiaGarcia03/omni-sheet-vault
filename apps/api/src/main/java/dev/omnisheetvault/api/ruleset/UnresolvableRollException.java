package dev.omnisheetvault.api.ruleset;

public class UnresolvableRollException extends RuntimeException {

    public UnresolvableRollException(RollKind kind, String key) {
        super("Cannot resolve roll: " + kind + (key != null ? " \"" + key + "\"" : ""));
    }
}

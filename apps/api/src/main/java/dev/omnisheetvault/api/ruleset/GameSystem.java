package dev.omnisheetvault.api.ruleset;

/**
 * Identity and metadata of a supported game system. See adr-0003 and adr-0004.
 */
public interface GameSystem {

    String systemId();

    String displayName();
}

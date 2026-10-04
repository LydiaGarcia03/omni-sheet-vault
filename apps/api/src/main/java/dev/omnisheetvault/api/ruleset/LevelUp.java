package dev.omnisheetvault.api.ruleset;

/** A build with one more class level: the new build, and the level the leveled class reaches. */
public record LevelUp(String buildJson, int classLevel) {
}

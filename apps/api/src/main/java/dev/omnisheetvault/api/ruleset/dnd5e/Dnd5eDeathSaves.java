package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * Death saving throw counts while the character is at 0 hit points (PHB 2014, "Dropping to 0
 * Hit Points"): three successes stabilize, three failures kill. Both reset when the character
 * regains any hit points.
 */
public record Dnd5eDeathSaves(@Min(0) @Max(3) int successes, @Min(0) @Max(3) int failures) {

    public static final int LIMIT = 3;
    public static final Dnd5eDeathSaves NONE = new Dnd5eDeathSaves(0, 0);

    public Dnd5eDeathSaves {
        successes = Math.clamp(successes, 0, LIMIT);
        failures = Math.clamp(failures, 0, LIMIT);
    }

    public boolean stable() {
        return successes >= LIMIT && failures < LIMIT;
    }

    public boolean dead() {
        return failures >= LIMIT;
    }

    Dnd5eDeathSaves withSuccesses(int count) {
        return new Dnd5eDeathSaves(successes + count, failures);
    }

    Dnd5eDeathSaves withFailures(int count) {
        return new Dnd5eDeathSaves(successes, failures + count);
    }
}

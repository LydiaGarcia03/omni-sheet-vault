package dev.omnisheetvault.api.ruleset;

/**
 * The character's death saving throws. {@code dying} is true while the sheet replaces its hit
 * points with the death saves (at 0 hit points, whatever the counts say); {@code stable} and
 * {@code dead} say how it ended.
 */
public record DeathSaves(int successes, int failures, boolean dying, boolean stable, boolean dead) {

    public static final DeathSaves NOT_DYING = new DeathSaves(0, 0, false, false, false);
}

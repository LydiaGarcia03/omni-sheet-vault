package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.List;

/** PHB 2014, "Character Advancement": the experience points each character level starts at. */
final class Dnd5eExperience {

    static final int MAX_LEVEL = 20;

    /** Index 0 is level 1. */
    static final List<Integer> THRESHOLDS = List.of(
            0, 300, 900, 2_700, 6_500, 14_000, 23_000, 34_000, 48_000, 64_000,
            85_000, 100_000, 120_000, 140_000, 165_000, 195_000, 225_000, 265_000, 305_000, 355_000);

    private Dnd5eExperience() {
    }

    /** The experience points {@code level} starts at. */
    static int threshold(int level) {
        return THRESHOLDS.get(Math.clamp(level, 1, MAX_LEVEL) - 1);
    }

    /** The highest level {@code points} reach. */
    static int levelFor(int points) {
        int level = 1;
        while (level < MAX_LEVEL && points >= threshold(level + 1)) {
            level++;
        }
        return level;
    }

    /** An experience-points character may level up once its points reach the next level, below 20. */
    static boolean levelUpAvailable(int points, int level) {
        return level < MAX_LEVEL && levelFor(points) > level;
    }
}

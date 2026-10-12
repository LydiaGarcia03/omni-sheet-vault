package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * The character's advancement. {@code advancement} is how it levels ({@code XP} or
 * {@code MILESTONE}). {@code currentLevelAt}/{@code nextLevelAt} bound the progress bar for the
 * current {@code level}; {@code nextLevelAt} is null at the top level. {@code levelUpAvailable}
 * says the points reached a level the character doesn't have yet; a milestone build sets the
 * next level's points when its milestone is reached, so the same flag drives both;
 * {@code canLevelUp} says a level up may be started at all. {@code thresholds} are the points
 * each level starts at, level 1 first. {@code classes} are the class levels that make up
 * {@code level}.
 */
public record Experience(
        String advancement,
        int points,
        int level,
        int levelFromPoints,
        int currentLevelAt,
        Integer nextLevelAt,
        boolean levelUpAvailable,
        boolean canLevelUp,
        List<Integer> thresholds,
        List<ClassLevelInfo> classes) {
}

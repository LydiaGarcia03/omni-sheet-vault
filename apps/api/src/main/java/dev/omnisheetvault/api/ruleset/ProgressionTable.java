package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * A class's level-by-level table, as its book prints it: {@code columns} name each cell, and
 * {@code rows} hold one row per level from 1, each with one cell per column. {@code key} is the
 * class's key, the same as its choices' {@link ChoicePlacement#group()}; {@code currentLevel} is
 * the level the build has reached, so a UI can mark it and preview the rest.
 */
public record ProgressionTable(String key, String name, int currentLevel, List<String> columns, List<List<String>> rows) {
}

package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.NotBlank;
import java.util.Set;

/** How the character's sheet looks: its theme id (DDB Red by default), one of {@link #THEMES}. */
public record Dnd5eAppearance(@NotBlank String theme) {

    public static final String DEFAULT_THEME = "ddb-red";
    public static final Dnd5eAppearance DEFAULT = new Dnd5eAppearance(DEFAULT_THEME);

    /** D&D Beyond's sheet themes, campaign themes left out. */
    public static final Set<String> THEMES = Set.of(
            DEFAULT_THEME, "barbarian-fire", "bard-rouge", "cleric-silver", "druid-moss", "fighter-rust", "monk-sky",
            "paladin-gold", "ranger-emerald", "rogue-ash", "sorcerer-blood", "warlock-iris", "wizard-cobalt",
            "artificer-copper");
}

package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * One class feature, species trait or feat — see VitalsZone. {@code category} and
 * {@code rechargeTrigger} are plain strings, not shared enums, same reasoning as
 * ability names (adr-0003). Nothing here is calculated; it is a plain
 * pass-through of stored data. {@code key} identifies this entry for spending/
 * restoring a use. {@code summary} is the short inline line, {@code description}
 * the full sidebar text — see {@code Dnd5eFeatureTrait}'s doc comment.
 * {@code choices} are what the player picked for it, listed beneath it.
 */
public record FeatureTrait(
        String key,
        String name,
        String category,
        String source,
        String summary,
        String description,
        Integer maxUses,
        int usedCount,
        String rechargeTrigger,
        List<String> choices) {

    public FeatureTrait(String key, String name, String category, String source, String summary, String description,
            Integer maxUses, int usedCount, String rechargeTrigger) {
        this(key, name, category, source, summary, description, maxUses, usedCount, rechargeTrigger, List.of());
    }
}

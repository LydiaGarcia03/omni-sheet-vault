package dev.omnisheetvault.api.ruleset;

/**
 * A familiar, mount, summoned creature or vehicle linked to the character — see
 * VitalsZone. {@code armorClass} and {@code speed} are the creature's own stored
 * stats, not derived from the character, so they are plain ints, unlike a
 * {@link CalculatedValue} field. {@code statBlock} carries the full structured
 * detail (phase 10 slice 10) — see {@link ExtraStatBlock}. {@code currentHitPoints}/
 * {@code temporaryHitPoints} are real, mutable session state, scoped to this one extra.
 */
public record Extra(
        String key,
        String name,
        String category,
        int armorClass,
        int maxHitPoints,
        int currentHitPoints,
        int temporaryHitPoints,
        int speed,
        ExtraStatBlock statBlock) {
}

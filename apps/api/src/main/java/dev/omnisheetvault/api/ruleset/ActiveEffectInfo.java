package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * A lasting effect currently on the character (a cast Mage Armor). {@code endsOnRests}
 * names the rests that end it; empty means only the player ends it, with
 * {@code durationText} ("8 hours") as the reminder. {@code onSelf} false means it was
 * cast on an ally: tracked, but none of the character's values change.
 */
public record ActiveEffectInfo(
        String key,
        String name,
        Integer castAtLevel,
        boolean concentration,
        List<String> endsOnRests,
        String durationText,
        boolean onSelf) {
}

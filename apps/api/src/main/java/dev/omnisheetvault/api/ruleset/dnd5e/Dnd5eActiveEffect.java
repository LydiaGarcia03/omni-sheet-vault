package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

/**
 * A lasting effect currently on the character, such as a cast Mage Armor (Stage C3's
 * "Active effects"). Its {@code modifiers} join the sheet's active list while it
 * exists, unless it was cast on an ally ({@code onSelf} false), which is tracked only
 * for its duration and concentration. {@code onSelf} null (sheets saved before the
 * target choice) means the character. {@code endsOnRests} holds only the rests the
 * source's own text names; empty means the player ends it, with {@code durationText}
 * ("8 hours") as the reminder. {@code concentration} effects end when another
 * concentration effect starts.
 */
public record Dnd5eActiveEffect(
        @NotBlank String key,
        @NotBlank String name,
        @NotBlank String sourceKey,
        Integer castAtLevel,
        boolean concentration,
        @NotNull Set<Dnd5eRechargeTrigger> endsOnRests,
        String durationText,
        @NotNull List<@Valid Dnd5eModifier> modifiers,
        Boolean onSelf) {

    public boolean appliesToCharacter() {
        return onSelf == null || onSelf;
    }
}

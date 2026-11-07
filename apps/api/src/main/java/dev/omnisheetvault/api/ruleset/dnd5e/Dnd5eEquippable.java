package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.Locale;
import java.util.Set;

/**
 * Whether an item can be equipped: weapons, armor and shields always; a magic item (one with a rarity, or
 * one that needs attunement) unless it's used up
 * (potions, scrolls, ammunition). Magic weapons and armor often come through as gear, so for a magic item
 * the type label decides, not the kind. Mundane gear is never equipped.
 */
final class Dnd5eEquippable {

    private static final Set<String> SINGLE_USE_TYPES = Set.of("potion", "scroll", "ammunition");
    private static final String NOT_MAGIC = "none";

    private Dnd5eEquippable() {
    }

    static boolean of(Dnd5eItem item) {
        return switch (item.itemKind()) {
            case WEAPON, ARMOR, SHIELD -> true;
            default -> (item.requiresAttunement() || isMagic(item.rarity())) && !isSingleUse(item.typeLabel());
        };
    }

    private static boolean isMagic(String rarity) {
        return rarity != null && !rarity.isBlank() && !NOT_MAGIC.equalsIgnoreCase(rarity);
    }

    private static boolean isSingleUse(String typeLabel) {
        return typeLabel != null && SINGLE_USE_TYPES.contains(typeLabel.toLowerCase(Locale.ROOT));
    }
}

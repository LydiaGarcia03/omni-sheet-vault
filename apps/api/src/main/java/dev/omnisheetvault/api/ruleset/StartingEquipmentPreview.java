package dev.omnisheetvault.api.ruleset;

import java.util.List;

/**
 * What a build starts with, for a builder's inventory list: {@code items} as they go onto the sheet,
 * {@code inventory} as the builder shows them, and the starting money already split into coins.
 */
public record StartingEquipmentPreview(List<StartingItem> items, List<Entry> inventory, int gold, int silver, int copper) {

    /**
     * {@code key} names this line for equipping (null for an item with no catalogue entry); {@code equipAction} is
     * {@code WEAR} or {@code WIELD} for an item that can start equipped, else null.
     */
    public record Entry(String key, String name, int quantity, String equipAction, boolean equipped) {
    }

    public static StartingEquipmentPreview none() {
        return new StartingEquipmentPreview(List.of(), List.of(), 0, 0, 0);
    }
}

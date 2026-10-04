package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.Item;
import java.util.List;

public record ItemResponse(
        String key,
        String name,
        int quantity,
        String cost,
        String notes,
        boolean equipped,
        boolean attuned,
        boolean requiresAttunement,
        Integer charges,
        int chargesUsed,
        String itemKind,
        String rarity,
        String storageLocation,
        Double weightKg,
        String source,
        List<String> properties) {

    private static final double POUNDS_TO_KILOGRAMS = 0.453592;

    static ItemResponse from(Item item) {
        Double weightKg = item.weightLb() != null ? Math.round(item.weightLb() * POUNDS_TO_KILOGRAMS * 10) / 10.0 : null;
        return new ItemResponse(
                item.key(), item.name(), item.quantity(), item.cost(), item.notes(), item.equipped(), item.attuned(),
                item.requiresAttunement(), item.charges(), item.chargesUsed(), item.itemKind(), item.rarity(),
                item.storageLocation(), weightKg, item.source(), item.properties() == null ? List.of() : item.properties());
    }
}

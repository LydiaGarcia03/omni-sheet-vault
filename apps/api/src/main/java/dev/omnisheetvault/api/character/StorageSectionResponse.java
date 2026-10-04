package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.StorageSection;

public record StorageSectionResponse(String location, int itemCount, double weightKg, Double capacityKg) {

    static StorageSectionResponse from(StorageSection section) {
        return new StorageSectionResponse(section.location(), section.itemCount(), section.weightKg(), section.capacityKg());
    }
}

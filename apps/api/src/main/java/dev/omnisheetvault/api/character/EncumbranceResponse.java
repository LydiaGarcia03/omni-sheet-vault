package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.Encumbrance;
import java.util.List;

public record EncumbranceResponse(
        boolean trackWeight, double carriedWeightKg, double capacityKg, boolean overloaded, List<StorageSectionResponse> sections) {

    static EncumbranceResponse from(Encumbrance encumbrance) {
        return new EncumbranceResponse(
                encumbrance.trackWeight(), encumbrance.carriedWeightKg(), encumbrance.capacityKg(), encumbrance.overloaded(),
                encumbrance.sections().stream().map(StorageSectionResponse::from).toList());
    }
}

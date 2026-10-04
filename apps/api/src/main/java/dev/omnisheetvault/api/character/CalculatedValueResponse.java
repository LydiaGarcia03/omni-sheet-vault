package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.CalculatedValue;
import java.util.List;

public record CalculatedValueResponse(int value, List<ContributionResponse> contributions) {

    static CalculatedValueResponse from(CalculatedValue calculatedValue) {
        return new CalculatedValueResponse(
                calculatedValue.value(),
                calculatedValue.contributions().stream().map(ContributionResponse::from).toList());
    }
}

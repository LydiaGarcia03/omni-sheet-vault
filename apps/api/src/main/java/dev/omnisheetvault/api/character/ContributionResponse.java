package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.Contribution;

public record ContributionResponse(String source, int amount) {

    static ContributionResponse from(Contribution contribution) {
        return new ContributionResponse(contribution.source(), contribution.amount());
    }
}

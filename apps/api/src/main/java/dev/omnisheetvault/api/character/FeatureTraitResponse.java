package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.FeatureTrait;
import java.util.List;

public record FeatureTraitResponse(
        String key,
        String name,
        String category,
        String source,
        String summary,
        String description,
        Integer maxUses,
        int usedCount,
        String rechargeTrigger,
        List<String> choices) {

    static FeatureTraitResponse from(FeatureTrait feature) {
        return new FeatureTraitResponse(
                feature.key(), feature.name(), feature.category(), feature.source(), feature.summary(),
                feature.description(), feature.maxUses(), feature.usedCount(), feature.rechargeTrigger(),
                feature.choices() == null ? List.of() : feature.choices());
    }
}

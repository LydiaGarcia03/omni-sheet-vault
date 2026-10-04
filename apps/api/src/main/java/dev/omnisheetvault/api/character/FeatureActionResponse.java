package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.FeatureAction;

public record FeatureActionResponse(
        String key,
        String name,
        String actionType,
        String description,
        Integer maxUses,
        int usedCount,
        String rechargeTrigger,
        String parentName,
        String parentKey) {

    static FeatureActionResponse from(FeatureAction feature) {
        return new FeatureActionResponse(
                feature.key(), feature.name(), feature.actionType(), feature.description(),
                feature.maxUses(), feature.usedCount(), feature.rechargeTrigger(), feature.parentName(), feature.parentKey());
    }
}

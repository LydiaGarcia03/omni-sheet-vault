package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.FeatureAction;
import java.util.function.UnaryOperator;

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

    /** Name, description and parent name in another language. */
    FeatureActionResponse localized(UnaryOperator<String> text, UnaryOperator<String> name) {
        return new FeatureActionResponse(key, name.apply(this.name), actionType, text.apply(description), maxUses, usedCount,
                rechargeTrigger, name.apply(parentName), parentKey);
    }

    static FeatureActionResponse from(FeatureAction feature) {
        return new FeatureActionResponse(
                feature.key(), feature.name(), feature.actionType(), feature.description(),
                feature.maxUses(), feature.usedCount(), feature.rechargeTrigger(), feature.parentName(), feature.parentKey());
    }
}

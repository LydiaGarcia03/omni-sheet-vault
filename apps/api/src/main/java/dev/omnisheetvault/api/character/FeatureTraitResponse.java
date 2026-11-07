package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.ruleset.FeatureTrait;
import java.util.List;
import java.util.function.UnaryOperator;

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

    /** Name, source, summary and description in another language; {@code summary} rewrites the summary from the translated description. */
    FeatureTraitResponse localized(UnaryOperator<String> text, UnaryOperator<String> name, SheetTextLocalizer.Summary summary) {
        String translated = text.apply(description);
        return new FeatureTraitResponse(key, name.apply(this.name), category, name.apply(source),
                summary.of(this.summary, description, translated), translated, maxUses, usedCount, rechargeTrigger,
                choices.stream().map(name).toList());
    }

    static FeatureTraitResponse from(FeatureTrait feature) {
        return new FeatureTraitResponse(
                feature.key(), feature.name(), feature.category(), feature.source(), feature.summary(),
                feature.description(), feature.maxUses(), feature.usedCount(), feature.rechargeTrigger(),
                feature.choices() == null ? List.of() : feature.choices());
    }
}

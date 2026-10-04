package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * A class feature, species trait or feat — see systems/dnd-5e/sheet-ui.md's
 * Features and Traits tab. {@code source} names the class or species it comes
 * from, without a level; a subclass feature names its class (feats use a fixed
 * "Feat" source, since a feat isn't granted by one);
 * entries are grouped by it, per the tab's own spec ("grouped by class and by
 * species"). Distinct from {@link Dnd5eFeatureAction}: that list is only the
 * subset of features that grant an action, feeding the Actions tab; this list
 * is the full catalog, including passive features with no action attached
 * (Fighting Style, Extra Attack) and species traits/feats, which never grant
 * an action. The two lists overlap in content (Second Wind appears in both)
 * but are independent — each tab reads only the shape it needs, and spending a
 * use through {@link Dnd5eSheetMutator#useFeatureTraitUse} here does not affect
 * the corresponding {@link Dnd5eFeatureAction} entry, or vice versa.
 * {@code summary} is the short line shown inline in the tab's list;
 * {@code description} is the full rules text shown in the Entity Detail
 * sidebar on click — confirmed live against D&D Beyond's own Features & Traits
 * tab (phase 10, slice 7) that these are genuinely different lengths, not the
 * same text twice. {@code rechargeTrigger} is null for a resource with no
 * automatic rest recharge; when present, a rest mechanic (phase 9) restores it
 * on a matching rest. {@code modifiers} are the feature's mechanical effects
 * (adr-0007), copied from the catalogue when a build is materialized; null on
 * sheets written before them. {@code choices} are what the player picked for
 * the feature, listed beneath it (the subclass under "Martial Archetype"); null
 * on sheets written before them.
 */
public record Dnd5eFeatureTrait(
        @NotBlank String key,
        @NotBlank String name,
        @NotNull Dnd5eFeatureTraitCategory category,
        @NotBlank String source,
        @NotBlank String summary,
        @NotNull String description,
        Integer maxUses,
        @Min(0) int usedCount,
        Dnd5eRechargeTrigger rechargeTrigger,
        List<@Valid Dnd5eModifier> modifiers,
        List<String> choices) {

    public Dnd5eFeatureTrait(
            String key, String name, Dnd5eFeatureTraitCategory category, String source, String summary,
            String description, Integer maxUses, int usedCount, Dnd5eRechargeTrigger rechargeTrigger,
            List<Dnd5eModifier> modifiers) {
        this(key, name, category, source, summary, description, maxUses, usedCount, rechargeTrigger, modifiers, List.of());
    }

    public Dnd5eFeatureTrait(
            String key, String name, Dnd5eFeatureTraitCategory category, String source, String summary,
            String description, Integer maxUses, int usedCount, Dnd5eRechargeTrigger rechargeTrigger) {
        this(key, name, category, source, summary, description, maxUses, usedCount, rechargeTrigger, List.of());
    }

    public List<Dnd5eModifier> modifiersOrEmpty() {
        return modifiers == null ? List.of() : modifiers;
    }
}

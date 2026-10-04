package dev.omnisheetvault.api.ruleset.dnd5e;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * A class feature or trait that grants an action — see systems/dnd-5e/sheet-ui.md's
 * Actions tab. {@code maxUses} is null for an at-will feature; when present it pairs
 * with {@code usedCount} to draw a box track, spent and restored through
 * {@link Dnd5eSheetMutator#useFeatureAction}/{@link Dnd5eSheetMutator#restoreFeatureAction}.
 * {@code rechargeTrigger} is null for a resource with no automatic rest recharge;
 * when present, a rest mechanic (phase 9) restores it on a matching rest.
 * {@code traitKey}, when set, links the action to a {@link Dnd5eFeatureTrait}: the
 * trait holds the one counter both tabs show and spend, and this entry's own
 * {@code maxUses}/{@code usedCount}/{@code rechargeTrigger} are unused.
 */
public record Dnd5eFeatureAction(
        @NotBlank String key,
        @NotBlank String name,
        @NotNull Dnd5eActionType actionType,
        @NotNull String description,
        Integer maxUses,
        @Min(0) int usedCount,
        Dnd5eRechargeTrigger rechargeTrigger,
        String traitKey) {

    public Dnd5eFeatureAction(
            String key, String name, Dnd5eActionType actionType, String description, Integer maxUses, int usedCount,
            Dnd5eRechargeTrigger rechargeTrigger) {
        this(key, name, actionType, description, maxUses, usedCount, rechargeTrigger, null);
    }
}

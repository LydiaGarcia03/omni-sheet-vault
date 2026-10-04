package dev.omnisheetvault.api.ruleset;

/**
 * One class feature or trait that grants an action — see VitalsZone. {@code
 * actionType} is a plain string, not a shared enum: which action-type categories
 * exist is a per-system concept, same reasoning as ability names (adr-0003).
 * {@code rechargeTrigger} is a plain string for the same reason — which rest
 * types exist is per-system, not a shared concept yet (adr-0003). {@code key}
 * identifies this entry for spending/restoring a use. {@code parentName} and {@code parentKey} name the feature that
 * grants the action (null when it isn't linked to one).
 */
public record FeatureAction(
        String key,
        String name,
        String actionType,
        String description,
        Integer maxUses,
        int usedCount,
        String rechargeTrigger,
        String parentName,
        String parentKey) {

    /** An action not linked to a granting feature. */
    public FeatureAction(String key, String name, String actionType, String description, Integer maxUses, int usedCount,
            String rechargeTrigger) {
        this(key, name, actionType, description, maxUses, usedCount, rechargeTrigger, null, null);
    }
}

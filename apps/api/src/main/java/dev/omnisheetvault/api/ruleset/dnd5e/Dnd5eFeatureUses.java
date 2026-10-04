package dev.omnisheetvault.api.ruleset.dnd5e;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;

/**
 * Combines the build's feature {@code uses} (adr-0007) into one counter per resource,
 * the way D&D Beyond's limited uses work: the first feature naming a resource owns
 * its counter; later features raise its {@code count} (highest wins, "Action Surge (two
 * uses)"), {@code add} to it ("Additional Superiority Die") or raise its {@code die}.
 * A {@code fromTableColumn} count reads the owning class's table at the level reached.
 */
final class Dnd5eFeatureUses {

    /** One resource: the trait key that shows its counter, its maximum, what restores it, and its die (0 when it has none). */
    record Resource(String ownerKey, int maxUses, Dnd5eRechargeTrigger recharge, int die) {
    }

    private final Map<String, Resource> byResource = new LinkedHashMap<>();
    private final Map<String, String> resourceByFeature = new LinkedHashMap<>();

    Dnd5eFeatureUses(List<Dnd5eBuildOutcome.FeatureOutcome> features, List<Dnd5eBuildOutcome.ClassOutcome> classes) {
        Map<String, int[]> counts = new LinkedHashMap<>();
        Map<String, String> owners = new LinkedHashMap<>();
        Map<String, Dnd5eRechargeTrigger> recharges = new LinkedHashMap<>();
        for (Dnd5eBuildOutcome.FeatureOutcome feature : features) {
            JsonNode uses = feature.mechanics() == null ? null : feature.mechanics().get("uses");
            if (uses == null || uses.isNull()) {
                continue;
            }
            String resource = uses.path("resource").asString(feature.key());
            resourceByFeature.put(feature.key(), resource);
            owners.putIfAbsent(resource, feature.key());
            int[] total = counts.computeIfAbsent(resource, key -> new int[3]);
            total[0] = Math.max(total[0], count(uses, feature.classSlug(), classes));
            total[1] += uses.path("add").asInt(0);
            total[2] = Math.max(total[2], uses.path("die").asInt(0));
            if (uses.hasNonNull("recharge")) {
                recharges.putIfAbsent(resource, Dnd5eRechargeTrigger.valueOf(uses.get("recharge").asString()));
            }
        }
        counts.forEach((resource, total) -> {
            int maxUses = total[0] + total[1];
            if (maxUses > 0) {
                byResource.put(resource, new Resource(owners.get(resource), maxUses, recharges.get(resource), total[2]));
            }
        });
    }

    /** The resource whose counter this trait shows, or null. */
    Resource ownedBy(String traitKey) {
        return byResource.values().stream().filter(resource -> resource.ownerKey().equals(traitKey)).findFirst().orElse(null);
    }

    /** The key of the trait holding this feature's counter, or null for a feature without uses. */
    String counterKeyFor(String featureKey) {
        String resource = resourceByFeature.get(featureKey);
        Resource found = resource == null ? null : byResource.get(resource);
        return found == null ? null : found.ownerKey();
    }

    private static int count(JsonNode uses, String classSlug, List<Dnd5eBuildOutcome.ClassOutcome> classes) {
        if (uses.hasNonNull("count")) {
            return uses.get("count").asInt();
        }
        if (!uses.hasNonNull("fromTableColumn") || classSlug == null) {
            return 0;
        }
        for (Dnd5eBuildOutcome.ClassOutcome taken : classes) {
            if (!taken.playerClass().slug().equals(classSlug)) {
                continue;
            }
            for (JsonNode column : taken.playerClass().data().path("tableColumns")) {
                if (column.path("label").asString().equals(uses.get("fromTableColumn").asString())) {
                    JsonNode values = column.path("valuesByLevel");
                    int level = taken.buildClass().level();
                    return values.size() >= level ? parseCount(values.get(level - 1).asString()) : 0;
                }
            }
        }
        return 0;
    }

    private static int parseCount(String value) {
        String digits = value.replaceAll("[^0-9]", "");
        return digits.isEmpty() ? 0 : Integer.parseInt(digits);
    }
}

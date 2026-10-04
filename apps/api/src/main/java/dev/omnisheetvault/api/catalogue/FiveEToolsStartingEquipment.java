package dev.omnisheetvault.api.catalogue;

import java.util.Map;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Maps 5etools starting-equipment groups ({@code defaultData}): each group is either
 * fixed (key {@code _}) or a choice between lettered options ({@code a}, {@code b},
 * {@code c}). Each option lists item slugs, or equipment categories such as
 * {@code weaponMartial}, with a quantity.
 */
final class FiveEToolsStartingEquipment {

    private static final String FIXED_OPTION_KEY = "_";
    private static final Set<String> GRANT_KEYS = Set.of(
            "item", "equipmentType", "special", "value", "quantity", "displayName", "containsValue", "worthValue");

    private final ObjectMapper objectMapper;

    FiveEToolsStartingEquipment(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ArrayNode groups(JsonNode defaultData) {
        ArrayNode groups = objectMapper.createArrayNode();
        if (defaultData == null) {
            return groups;
        }
        for (JsonNode group : defaultData) {
            ArrayNode options = groups.addObject().putArray("options");
            for (Map.Entry<String, JsonNode> option : group.properties()) {
                ObjectNode mapped = options.addObject();
                mapped.put("label", FIXED_OPTION_KEY.equals(option.getKey()) ? null : option.getKey());
                ArrayNode grants = mapped.putArray("grants");
                for (JsonNode grant : option.getValue()) {
                    grants.add(grant(grant));
                }
            }
        }
        return groups;
    }

    /**
     * One grant: exactly one of {@code itemSlug} (a catalogue item), {@code equipmentType}
     * (a category to pick from), {@code special} (a free-text trinket) or {@code valueCp}
     * (coins), plus {@code quantity}, an optional {@code displayName}, and
     * {@code containsValueCp}/{@code worthValueCp} for a pouch's coins or a gem's worth.
     * Values are in copper pieces, as 5etools stores them.
     */
    private ObjectNode grant(JsonNode grant) {
        ObjectNode mapped = objectMapper.createObjectNode();
        mapped.putNull("itemSlug");
        mapped.putNull("equipmentType");
        mapped.putNull("special");
        mapped.putNull("valueCp");
        if (grant.isString()) {
            mapped.put("itemSlug", itemSlug(grant.asString()));
        } else {
            for (String key : grant.propertyNames()) {
                if (!GRANT_KEYS.contains(key)) {
                    throw new FiveEToolsIngestException("Unsupported starting-equipment grant: " + grant);
                }
            }
            if (grant.get("item") != null) {
                mapped.put("itemSlug", itemSlug(grant.get("item").asString()));
            } else if (grant.get("equipmentType") != null) {
                mapped.put("equipmentType", grant.get("equipmentType").asString());
            } else if (grant.get("special") != null) {
                mapped.put("special", TagMarkupStripper.strip(grant.get("special").asString()));
            } else if (grant.get("value") != null) {
                mapped.put("valueCp", grant.get("value").asInt());
            } else {
                throw new FiveEToolsIngestException("Unsupported starting-equipment grant: " + grant);
            }
        }
        mapped.put("quantity", grant.path("quantity").asInt(1));
        mapped.put("displayName", textOrNull(grant.get("displayName")));
        mapped.put("containsValueCp", intOrNull(grant.get("containsValue")));
        mapped.put("worthValueCp", intOrNull(grant.get("worthValue")));
        return mapped;
    }

    private static String itemSlug(String itemReference) {
        return FiveEToolsNaming.slug(FiveEToolsNaming.stripSourceSuffix(itemReference));
    }

    private static String textOrNull(JsonNode node) {
        return node == null ? null : TagMarkupStripper.strip(node.asString());
    }

    private static Integer intOrNull(JsonNode node) {
        return node == null ? null : node.asInt();
    }
}

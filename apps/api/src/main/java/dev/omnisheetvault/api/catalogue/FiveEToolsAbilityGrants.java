package dev.omnisheetvault.api.catalogue;

import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Maps a 5etools {@code ability} block (species, feats) into alternatives of
 * {@code {fixed: {strength: 2}, choices: [{from, count, amount}], maximum}} — the
 * character gets exactly one alternative. {@code maximum} is the score cap the increase
 * may reach ({@code null} = the normal 20).
 */
final class FiveEToolsAbilityGrants {

    private final ObjectMapper objectMapper;
    private final FiveEToolsProficiencies proficiencies;

    FiveEToolsAbilityGrants(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.proficiencies = new FiveEToolsProficiencies(objectMapper);
    }

    ArrayNode alternatives(JsonNode ability) {
        ArrayNode alternatives = objectMapper.createArrayNode();
        for (JsonNode alternative : proficiencies.asArray(ability)) {
            ObjectNode mapped = alternatives.addObject();
            ObjectNode fixed = mapped.putObject("fixed");
            ArrayNode choices = mapped.putArray("choices");
            mapped.putNull("maximum");
            for (Map.Entry<String, JsonNode> increase : alternative.properties()) {
                switch (increase.getKey()) {
                    case "choose" -> choices.add(choice(increase.getValue()));
                    case "max" -> mapped.put("maximum", increase.getValue().asInt());
                    default -> fixed.put(FiveEToolsNaming.abilityKey(increase.getKey()), increase.getValue().asInt());
                }
            }
        }
        return alternatives;
    }

    /**
     * {@code {from, count, amount}}: {@code count} distinct abilities each get {@code amount}.
     * A 5etools {@code weighted} choice becomes {@code weights} instead — distinct abilities
     * in selection order, the first getting {@code weights[0]}, and so on.
     */
    private ObjectNode choice(JsonNode choose) {
        JsonNode weighted = choose.get("weighted");
        JsonNode source = weighted != null ? weighted : choose;
        if (source.get("from") == null) {
            throw new FiveEToolsIngestException("Unsupported 5etools ability choice: " + choose);
        }
        ObjectNode choice = objectMapper.createObjectNode();
        ArrayNode from = choice.putArray("from");
        source.get("from").forEach(ability -> from.add(FiveEToolsNaming.abilityKey(ability.asString())));
        if (weighted != null) {
            ArrayNode weights = choice.putArray("weights");
            weighted.get("weights").forEach(weight -> weights.add(weight.asInt()));
            choice.put("count", weights.size());
            choice.putNull("amount");
        } else {
            choice.putNull("weights");
            choice.put("count", choose.path("count").asInt(1));
            choice.put("amount", choose.path("amount").asInt(1));
        }
        return choice;
    }
}

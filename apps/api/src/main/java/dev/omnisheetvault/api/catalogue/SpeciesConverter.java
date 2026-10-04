package dev.omnisheetvault.api.catalogue;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools race, already resolved by {@link FiveEToolsSpeciesData}, into a
 * {@code SPECIES} catalogue entry: its own mechanics, its subspecies nested under
 * {@code subspecies}, and its expanded versions under {@code variants}.
 *
 * <p>Subspecies merge rule for the build resolver: a subspecies' non-null scalar
 * (size, speed, senses) replaces the species' value; each list field is added to the
 * species' list unless the subspecies names it in {@code overwrite}, in which case it
 * replaces it.
 */
final class SpeciesConverter implements FiveEToolsConverter {

    private static final Map<String, String> SIZES = Map.of(
            "T", "tiny", "S", "small", "M", "medium", "L", "large", "H", "huge", "G", "gargantuan", "V", "varies");

    /** 5etools field names a subrace may overwrite, as this project names them; {@code null} = a field not mapped here. */
    private static final Map<String, String> OVERWRITABLE_FIELDS = mapOverwritable();

    private final ObjectMapper objectMapper;
    private final FiveEToolsProficiencies proficiencies;
    private final FiveEToolsAbilityGrants abilityGrants;
    private final FiveEToolsMechanicsOverlay overlay;

    SpeciesConverter(ObjectMapper objectMapper) {
        this(objectMapper, FiveEToolsMechanicsOverlay.empty());
    }

    SpeciesConverter(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        this.objectMapper = objectMapper;
        this.proficiencies = new FiveEToolsProficiencies(objectMapper);
        this.abilityGrants = new FiveEToolsAbilityGrants(objectMapper);
        this.overlay = overlay;
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.SPECIES;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        List<JsonNode> races = FiveEToolsSpeciesData.load(dataSource, objectMapper);
        overlay.matchEntries(FiveEToolsMechanicsOverlay.SPECIES, races);
        return races;
    }

    @Override
    public CatalogueEntryImport convert(JsonNode race) {
        String name = race.get("name").asString();
        ObjectNode data = mechanics(race);
        applyTraitOverlay(data, overlay.findEntry(FiveEToolsMechanicsOverlay.SPECIES, name, race.get("source").asString()));
        data.set("variants", variants(race));
        data.set("subspecies", subspecies(race.get(FiveEToolsSpeciesData.SUBSPECIES_FIELD)));
        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.SPECIES,
                FiveEToolsNaming.slug(name),
                name,
                race.get("source").asString(),
                pageOrNull(race),
                List.of(),
                description(race),
                data);
    }

    private ArrayNode subspecies(JsonNode subraces) {
        ArrayNode result = objectMapper.createArrayNode();
        for (JsonNode subrace : subraces) {
            ObjectNode mapped = result.addObject();
            mapped.put("name", subrace.get("name") == null ? null : subrace.get("name").asString());
            mapped.put("sourceBook", subrace.get(FiveEToolsSpeciesData.SOURCE_NAME_FIELD).asString());
            mapped.put("sourcePage", pageOrNull(subrace));
            mapped.set("overwrite", overwrite(subrace.get("overwrite")));
            mapped.put("description", description(subrace));
            mapped.setAll(mechanics(subrace));
            mapped.set("variants", variants(subrace));
        }
        return result;
    }

    private ArrayNode variants(JsonNode entry) {
        ArrayNode result = objectMapper.createArrayNode();
        for (JsonNode variant : entry.get(FiveEToolsSpeciesData.VARIANTS_FIELD)) {
            ObjectNode mapped = result.addObject();
            mapped.put("name", variant.get("name").asString());
            mapped.put("description", description(variant));
            mapped.setAll(mechanics(variant));
        }
        return result;
    }

    private ObjectNode mechanics(JsonNode entry) {
        ObjectNode data = objectMapper.createObjectNode();
        data.set("size", size(entry.get("size")));
        data.set("speed", speed(entry.get("speed")));
        data.set("senses", senses(entry));
        data.set("abilityAlternatives", abilityGrants.alternatives(entry.get("ability")));
        data.set("skillAlternatives", proficiencies.skillAlternatives(entry.get("skillProficiencies")));
        data.set("toolAlternatives", proficiencies.toolAlternatives(entry.get("toolProficiencies")));
        data.set("languageAlternatives", proficiencies.grantAlternatives(entry.get("languageProficiencies"), FiveEToolsNaming::slug));
        data.set("weaponAlternatives", proficiencies.grantAlternatives(entry.get("weaponProficiencies"), FiveEToolsNaming::slug));
        data.set("armorAlternatives", proficiencies.grantAlternatives(entry.get("armorProficiencies"), FiveEToolsNaming::slug));
        data.set("featAlternatives", proficiencies.grantAlternatives(entry.get("feats"), FiveEToolsNaming::slug));
        data.set("skillToolLanguageChoices", copyOrNull(entry.get("skillToolLanguageProficiencies")));
        data.set("damageResistances", pickList(entry.get("resist")));
        data.set("damageImmunities", pickList(entry.get("immune")));
        data.set("damageVulnerabilities", pickList(entry.get("vulnerable")));
        data.set("conditionImmunities", pickList(entry.get("conditionImmune")));
        data.set("creatureTypes", copyOrNull(entry.get("creatureTypes")));
        data.set("additionalSpells", copyOrNull(entry.get("additionalSpells")));
        data.set("age", copyOrNull(entry.get("age")));
        data.set("heightAndWeight", copyOrNull(entry.get("heightAndWeight")));
        data.set("lineage", copyOrNull(entry.get("lineage")));
        data.set("traits", traits(entry.get("entries")));
        return data;
    }

    private JsonNode size(JsonNode size) {
        if (size == null) {
            return objectMapper.nullNode();
        }
        ArrayNode sizes = objectMapper.createArrayNode();
        for (JsonNode code : proficiencies.asArray(size)) {
            String mapped = SIZES.get(code.asString());
            if (mapped == null) {
                throw new FiveEToolsIngestException("Unknown 5etools size code: " + code.asString());
            }
            sizes.add(mapped);
        }
        return sizes;
    }

    /** A number is a walking speed; {@code true} for another movement type means "equal to your walking speed". */
    private JsonNode speed(JsonNode speed) {
        if (speed == null) {
            return objectMapper.nullNode();
        }
        ObjectNode result = objectMapper.createObjectNode();
        if (speed.isNumber()) {
            result.put("walk", speed.asInt());
            return result;
        }
        int walk = speed.path("walk").asInt(0);
        for (Map.Entry<String, JsonNode> movement : speed.properties()) {
            JsonNode value = movement.getValue();
            if (value.isNumber()) {
                result.put(movement.getKey(), value.asInt());
            } else if (value.isBoolean() && value.asBoolean()) {
                result.put(movement.getKey(), walk);
            } else {
                throw new FiveEToolsIngestException("Unsupported 5etools speed value: " + speed);
            }
        }
        return result;
    }

    private ArrayNode senses(JsonNode entry) {
        ArrayNode senses = objectMapper.createArrayNode();
        for (String sense : List.of("darkvision", "blindsight", "truesight", "tremorsense")) {
            JsonNode range = entry.get(sense);
            if (range != null) {
                ObjectNode mapped = senses.addObject();
                mapped.put("type", sense);
                mapped.put("range", range.asInt());
            }
        }
        return senses;
    }

    /** Resistances and immunities: plain names are fixed; {@code {choose: {from}}} is a pick. */
    private ObjectNode pickList(JsonNode list) {
        ObjectNode result = objectMapper.createObjectNode();
        ArrayNode fixed = result.putArray("fixed");
        ArrayNode choices = result.putArray("choices");
        for (JsonNode item : proficiencies.asArray(list)) {
            if (item.isString()) {
                fixed.add(item.asString());
            } else if (item.get("choose") != null) {
                ObjectNode choice = choices.addObject();
                ArrayNode from = choice.putArray("from");
                item.get("choose").get("from").forEach(from::add);
                choice.put("count", item.get("choose").path("count").asInt(1));
            } else {
                throw new FiveEToolsIngestException("Unsupported 5etools damage/condition entry: " + item);
            }
        }
        return result;
    }

    /** The species overlay's {@code traits} map, keyed by trait name, adds each trait's modifiers, uses and action; a name matching no trait fails. */
    private void applyTraitOverlay(ObjectNode data, JsonNode speciesOverlay) {
        if (speciesOverlay == null) {
            return;
        }
        for (Map.Entry<String, JsonNode> traitOverlay : speciesOverlay.path("traits").properties()) {
            ObjectNode trait = null;
            for (JsonNode candidate : data.path("traits")) {
                if (candidate.path("name").asString().equalsIgnoreCase(traitOverlay.getKey())) {
                    trait = (ObjectNode) candidate;
                }
            }
            if (trait == null) {
                throw new FiveEToolsIngestException("Species overlay names a trait the species doesn't have: " + traitOverlay.getKey());
            }
            FiveEToolsMechanicsOverlay.copyMechanics(trait, traitOverlay.getValue(), objectMapper);
        }
    }

    private ArrayNode traits(JsonNode entries) {
        ArrayNode traits = objectMapper.createArrayNode();
        for (JsonNode entry : proficiencies.asArray(entries)) {
            if (entry.isObject() && entry.get("name") != null) {
                ObjectNode trait = traits.addObject();
                trait.put("name", entry.get("name").asString());
                trait.put("description", TagMarkupStripper.strip(FiveEToolsEntries.flatten(entry.get("entries"))));
            }
        }
        return traits;
    }

    private ArrayNode overwrite(JsonNode overwrite) {
        ArrayNode fields = objectMapper.createArrayNode();
        if (overwrite == null) {
            return fields;
        }
        for (Map.Entry<String, JsonNode> field : overwrite.properties()) {
            if (!OVERWRITABLE_FIELDS.containsKey(field.getKey())) {
                throw new FiveEToolsIngestException("Unsupported 5etools subrace overwrite field: " + field.getKey());
            }
            String mapped = OVERWRITABLE_FIELDS.get(field.getKey());
            if (mapped != null && field.getValue().asBoolean(false)) {
                fields.add(mapped);
            }
        }
        return fields;
    }

    private static Map<String, String> mapOverwritable() {
        Map<String, String> fields = new HashMap<>();
        fields.put("ability", "abilityAlternatives");
        fields.put("skillProficiencies", "skillAlternatives");
        fields.put("toolProficiencies", "toolAlternatives");
        fields.put("languageProficiencies", "languageAlternatives");
        fields.put("weaponProficiencies", "weaponAlternatives");
        fields.put("armorProficiencies", "armorAlternatives");
        fields.put("resist", "damageResistances");
        fields.put("additionalSpells", "additionalSpells");
        fields.put("traitTags", null);
        return Collections.unmodifiableMap(fields);
    }

    private static String description(JsonNode entry) {
        return TagMarkupStripper.strip(FiveEToolsEntries.flatten(entry.get("entries")));
    }

    private static Integer pageOrNull(JsonNode entry) {
        JsonNode page = entry.get("page");
        return page == null || !page.isNumber() ? null : page.asInt();
    }

    private JsonNode copyOrNull(JsonNode node) {
        return node == null ? objectMapper.nullNode() : node.deepCopy();
    }
}

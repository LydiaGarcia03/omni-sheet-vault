package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Loads {@code races.json} and returns the 2014-ruleset species with their
 * {@code _copy} resolved, their subraces attached under {@code _subspecies}, and their
 * {@code _versions} expanded under {@code _variants} (subraces get the same two
 * treatments). Races tagged {@code edition: "one"} are the 2024 ruleset and are dropped;
 * a subrace is kept only when its parent race is.
 */
final class FiveEToolsSpeciesData {

    static final String SUBSPECIES_FIELD = "_subspecies";
    static final String VARIANTS_FIELD = "_variants";
    static final String SOURCE_NAME_FIELD = "_sourceName";

    private static final String REVISED_2024_EDITION = "one";

    private FiveEToolsSpeciesData() {
    }

    static List<JsonNode> load(FiveEToolsDataSource dataSource, ObjectMapper objectMapper) {
        JsonNode file = dataSource.readDataFile("races.json");
        FiveEToolsSourceNames sourceNames = FiveEToolsSourceNames.load(dataSource);

        Map<String, ObjectNode> resolvedRaces =
                FiveEToolsCopies.resolveAll(file.get("race"), FiveEToolsSpeciesData::raceKey, FiveEToolsSpeciesData::raceKey);
        Map<String, ObjectNode> resolvedSubraces =
                FiveEToolsCopies.resolveAll(file.get("subrace"), FiveEToolsSpeciesData::subraceKey, FiveEToolsSpeciesData::subraceKey);

        List<JsonNode> species = new ArrayList<>();
        Map<String, ObjectNode> keptByKey = new HashMap<>();
        for (JsonNode raw : file.get("race")) {
            ObjectNode race = resolvedRaces.get(raceKey(raw));
            if (isRevised2024(race)) {
                continue;
            }
            applyLineageDefaults(race, objectMapper);
            race.set(VARIANTS_FIELD, variants(race, objectMapper));
            race.set(SUBSPECIES_FIELD, objectMapper.createArrayNode());
            species.add(race);
            keptByKey.put(raceKey(race), race);
        }
        for (JsonNode raw : file.get("subrace")) {
            ObjectNode subrace = resolvedSubraces.get(subraceKey(raw));
            ObjectNode parent = keptByKey.get(key(subrace.path("raceName").asString(""), subrace.path("raceSource").asString("")));
            if (parent == null || isRevised2024(subrace)) {
                continue;
            }
            subrace.put(SOURCE_NAME_FIELD, sourceNames.nameOf(subrace.get("source").asString()));
            subrace.set(VARIANTS_FIELD, variants(withParentEntries(subrace, parent, objectMapper), objectMapper));
            ((ArrayNode) parent.get(SUBSPECIES_FIELD)).add(subrace);
        }
        return species;
    }

    /**
     * 5etools' own renderer ({@code render.js}, race list merge) fills in a legacy
     * "lineage" race's ability increases and languages when the data leaves them out:
     * {@code VRGR} lineages get +2/+1 or +1/+1/+1 to abilities of choice, {@code UA1}
     * +2/+1, and both get Common plus one standard language, with the same Languages
     * trait text 5etools adds.
     */
    private static void applyLineageDefaults(ObjectNode race, ObjectMapper objectMapper) {
        String lineage = race.path("lineage").isString() ? race.get("lineage").asString() : null;
        if (lineage == null || !(lineage.equals("VRGR") || lineage.equals("UA1"))) {
            return;
        }
        if (race.get("ability") == null) {
            ArrayNode ability = race.putArray("ability");
            ability.add(weightedChoice(objectMapper, 2, 1));
            if (lineage.equals("VRGR")) {
                ability.add(weightedChoice(objectMapper, 1, 1, 1));
            }
        }
        if (race.get("languageProficiencies") == null) {
            ArrayNode entries = race.get("entries") instanceof ArrayNode existing ? existing : race.putArray("entries");
            ObjectNode languages = entries.addObject();
            languages.put("type", "entries");
            languages.put("name", "Languages");
            languages.putArray("entries").add("You can speak, read, and write Common and one other language that you "
                    + "and your DM agree is appropriate for your character.");
            ObjectNode grant = race.putArray("languageProficiencies").addObject();
            grant.put("common", true);
            grant.put("anyStandard", 1);
        }
    }

    private static ObjectNode weightedChoice(ObjectMapper objectMapper, int... weights) {
        ObjectNode alternative = objectMapper.createObjectNode();
        ObjectNode weighted = alternative.putObject("choose").putObject("weighted");
        ArrayNode from = weighted.putArray("from");
        for (String ability : new String[] {"str", "dex", "con", "int", "wis", "cha"}) {
            from.add(ability);
        }
        ArrayNode weightArray = weighted.putArray("weights");
        for (int weight : weights) {
            weightArray.add(weight);
        }
        return alternative;
    }

    /** A subrace's versions modify the combined race + subrace text, as 5etools renders them. */
    private static ObjectNode withParentEntries(JsonNode subrace, JsonNode parent, ObjectMapper objectMapper) {
        ObjectNode combined = (ObjectNode) subrace.deepCopy();
        ArrayNode entries = objectMapper.createArrayNode();
        parent.path("entries").forEach(entry -> entries.add(entry.deepCopy()));
        subrace.path("entries").forEach(entry -> entries.add(entry.deepCopy()));
        combined.set("entries", entries);
        return combined;
    }

    private static ArrayNode variants(JsonNode entry, ObjectMapper objectMapper) {
        ArrayNode variants = objectMapper.createArrayNode();
        FiveEToolsCopies.expandVersions(entry).forEach(variants::add);
        return variants;
    }

    private static String raceKey(JsonNode race) {
        return key(race.get("name").asString(), race.get("source").asString());
    }

    /** A subrace without a name is its race's unnamed default (e.g. the standard Human). */
    private static String subraceKey(JsonNode subrace) {
        return key(subrace.path("name").asString(""), subrace.path("source").asString(""),
                subrace.path("raceName").asString(""), subrace.path("raceSource").asString(""));
    }

    private static boolean isRevised2024(JsonNode entry) {
        return REVISED_2024_EDITION.equals(entry.path("edition").asString(""));
    }

    private static String key(String... parts) {
        return String.join("|", parts).toLowerCase(Locale.ROOT);
    }
}

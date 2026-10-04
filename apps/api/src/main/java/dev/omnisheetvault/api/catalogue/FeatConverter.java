package dev.omnisheetvault.api.catalogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Converts one 5etools feat entry into this project's own catalogue shape: its
 * structured grants in {@code data} (what a build resolver offers as the feat's own
 * choices), and a {@code description} built from 5etools' {@code entries} with the
 * feat's {@code prerequisite} (when present) prepended as a plain line, the same way
 * D&D Beyond shows it.
 */
final class FeatConverter implements FiveEToolsConverter {

    private static final Map<String, String> ABILITY_NAMES = Map.of(
            "str", "Strength", "dex", "Dexterity", "con", "Constitution",
            "int", "Intelligence", "wis", "Wisdom", "cha", "Charisma");

    private final ObjectMapper objectMapper;
    private final FiveEToolsProficiencies proficiencies;
    private final FiveEToolsAbilityGrants abilityGrants;
    private final FiveEToolsClassProgression progression;
    private final FiveEToolsMechanicsOverlay overlay;

    FeatConverter(ObjectMapper objectMapper) {
        this(objectMapper, FiveEToolsMechanicsOverlay.empty());
    }

    FeatConverter(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        this.objectMapper = objectMapper;
        this.proficiencies = new FiveEToolsProficiencies(objectMapper);
        this.abilityGrants = new FiveEToolsAbilityGrants(objectMapper);
        this.progression = new FiveEToolsClassProgression(objectMapper);
        this.overlay = overlay;
    }

    @Override
    public CatalogueEntryKind kind() {
        return CatalogueEntryKind.FEAT;
    }

    @Override
    public List<JsonNode> loadRawEntries(FiveEToolsDataSource dataSource) {
        List<JsonNode> feats = new ArrayList<>();
        JsonNode featArray = dataSource.readDataFile("feats.json").get("feat");
        if (featArray != null) {
            for (JsonNode feat : featArray) {
                feats.add(feat);
            }
        }
        overlay.matchEntries(FiveEToolsMechanicsOverlay.FEAT, feats);
        return feats;
    }

    @Override
    public CatalogueEntryImport convert(JsonNode feat) {
        String name = feat.get("name").asString();
        String rawSource = feat.get("source").asString();

        return new CatalogueEntryImport(
                "dnd-5e",
                CatalogueEntryKind.FEAT,
                FiveEToolsNaming.slug(name),
                name,
                rawSource,
                intOrNull(feat.get("page")),
                List.of(),
                description(feat),
                data(feat));
    }

    /** The feat's structured grants in the shared grant shape; prerequisites and spells kept verbatim for the build resolver. */
    private ObjectNode data(JsonNode feat) {
        ObjectNode data = objectMapper.createObjectNode();
        data.set("abilityAlternatives", abilityGrants.alternatives(feat.get("ability")));
        data.set("skillAlternatives", proficiencies.skillAlternatives(feat.get("skillProficiencies")));
        data.set("toolAlternatives", proficiencies.toolAlternatives(feat.get("toolProficiencies")));
        data.set("languageAlternatives", proficiencies.grantAlternatives(feat.get("languageProficiencies"), FiveEToolsNaming::slug));
        data.set("weaponAlternatives", proficiencies.grantAlternatives(feat.get("weaponProficiencies"), FiveEToolsNaming::slug));
        data.set("armorAlternatives", proficiencies.grantAlternatives(feat.get("armorProficiencies"), FiveEToolsNaming::slug));
        data.set("savingThrowAlternatives", proficiencies.grantAlternatives(feat.get("savingThrowProficiencies"), FiveEToolsNaming::abilityKey));
        data.set("expertiseAlternatives", proficiencies.skillAlternatives(feat.get("expertise")));
        data.set("skillToolLanguageChoices", copyOrNull(feat.get("skillToolLanguageProficiencies")));
        data.set("optionalFeatureProgressions", progression.optionalFeatureProgressions(feat.get("optionalfeatureProgression")));
        data.set("additionalSpells", copyOrNull(feat.get("additionalSpells")));
        data.set("prerequisites", copyOrNull(feat.get("prerequisite")));
        data.put("repeatable", feat.path("repeatable").asBoolean(false));
        JsonNode mechanics = overlay.findEntry(FiveEToolsMechanicsOverlay.FEAT, feat.get("name").asString(), feat.get("source").asString());
        FiveEToolsMechanicsOverlay.copyMechanics(data, mechanics, objectMapper);
        return data;
    }

    private JsonNode copyOrNull(JsonNode node) {
        return node == null ? objectMapper.nullNode() : node.deepCopy();
    }

    private static String description(JsonNode feat) {
        String prerequisite = prerequisiteText(feat.get("prerequisite"));
        String body = TagMarkupStripper.strip(FiveEToolsEntries.flatten(feat.get("entries")));
        return prerequisite == null ? body : "Prerequisite: " + prerequisite + "\n\n" + body;
    }

    /**
     * {@code prerequisite} is an array of alternatives (any one satisfies the feat);
     * each alternative is an object of conditions that all apply together. Covers the
     * shapes real 2014-era feats actually use — ability score minimums (real shape
     * confirmed against Ritual Caster/Grappler/Defensive Duelist/Skulker: always
     * {@code "ability": [{"int":13}, ...]}, multiple entries meaning "any one of
     * these", never a top-level ability key directly on the alternative), armor/weapon
     * proficiency, spellcasting, race, level — plus 5etools' own free-text
     * {@code otherSummary} fallback for anything odder. Returns null — never a guess —
     * for a shape not covered here; the feat still imports, just without a
     * prerequisite line.
     */
    private static String prerequisiteText(JsonNode prerequisiteArray) {
        if (!FiveEToolsNaming.isNonEmptyArray(prerequisiteArray)) {
            return null;
        }
        List<String> alternatives = new ArrayList<>();
        for (JsonNode alternative : prerequisiteArray) {
            String text = alternativeText(alternative);
            if (text != null) {
                alternatives.add(text);
            }
        }
        return alternatives.isEmpty() ? null : String.join("; ", alternatives);
    }

    private static String alternativeText(JsonNode alternative) {
        List<String> parts = new ArrayList<>();
        addAbilityText(parts, alternative.get("ability"));
        addRaceText(parts, alternative.get("race"));
        addProficiencyText(parts, alternative.get("proficiency"));
        if (isTrue(alternative.get("spellcasting"))) {
            parts.add("the ability to cast at least one spell");
        }
        addLevelText(parts, alternative.get("level"));
        if (!parts.isEmpty()) {
            return String.join(", ", parts);
        }
        JsonNode otherSummary = alternative.get("otherSummary");
        if (otherSummary != null && otherSummary.get("entrySummary") != null) {
            return TagMarkupStripper.strip(otherSummary.get("entrySummary").asString());
        }
        return null;
    }

    private static String abilityScoreText(JsonNode singleAbilityObject) {
        for (Map.Entry<String, String> ability : ABILITY_NAMES.entrySet()) {
            JsonNode scoreNode = singleAbilityObject.get(ability.getKey());
            if (scoreNode != null && scoreNode.isNumber()) {
                return ability.getValue() + " " + scoreNode.asInt() + " or higher";
            }
        }
        return null;
    }

    /**
     * {@code ability} is a list of single-ability objects (e.g.
     * {@code [{"int":13},{"wis":13}]}) — multiple entries mean "any one of these",
     * joined with "or"; a single entry is the plain requirement.
     */
    private static void addAbilityText(List<String> parts, JsonNode abilityArray) {
        if (!FiveEToolsNaming.isNonEmptyArray(abilityArray)) {
            return;
        }
        List<String> options = new ArrayList<>();
        for (JsonNode option : abilityArray) {
            String text = abilityScoreText(option);
            if (text != null) {
                options.add(text);
            }
        }
        if (!options.isEmpty()) {
            parts.add(String.join(" or ", options));
        }
    }

    private static void addRaceText(List<String> parts, JsonNode raceArray) {
        if (!FiveEToolsNaming.isNonEmptyArray(raceArray)) {
            return;
        }
        List<String> races = new ArrayList<>();
        for (JsonNode race : raceArray) {
            JsonNode nameNode = race.get("name");
            if (nameNode != null) {
                races.add(FiveEToolsNaming.capitalize(nameNode.asString()));
            }
        }
        if (!races.isEmpty()) {
            parts.add(String.join(" or ", races));
        }
    }

    private static void addProficiencyText(List<String> parts, JsonNode proficiencyArray) {
        if (!FiveEToolsNaming.isNonEmptyArray(proficiencyArray)) {
            return;
        }
        for (JsonNode requirement : proficiencyArray) {
            for (Map.Entry<String, JsonNode> field : requirement.properties()) {
                String value = field.getValue().isString() ? field.getValue().asString() : "";
                String label = value.isEmpty()
                        ? FiveEToolsNaming.capitalize(field.getKey())
                        : FiveEToolsNaming.capitalize(value) + " " + field.getKey();
                parts.add("proficiency with " + label.toLowerCase(Locale.ROOT));
            }
        }
    }

    private static void addLevelText(List<String> parts, JsonNode levelNode) {
        if (levelNode == null) {
            return;
        }
        if (levelNode.isNumber()) {
            parts.add("level " + levelNode.asInt() + "+");
        } else if (levelNode.get("level") != null) {
            parts.add("level " + levelNode.get("level").asInt() + "+");
        }
    }

    private static boolean isTrue(JsonNode node) {
        return node != null && node.asBoolean();
    }

    private static Integer intOrNull(JsonNode node) {
        return node == null || !node.isNumber() ? null : Integer.valueOf(node.asInt());
    }
}

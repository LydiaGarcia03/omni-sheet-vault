package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.SelectionDetail;
import dev.omnisheetvault.api.ruleset.SelectionDetail.Entry;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;

/** The Species and Background steps' detail cards, built from the chosen entries' catalogue data. */
final class Dnd5eSelectionDetails {

    /** Species traits the card's facts already cover, or that only describe lore. */
    private static final Set<String> COVERED_TRAITS = Set.of("Age", "Alignment", "Size", "Languages");

    private Dnd5eSelectionDetails() {
    }

    /** {@code label} is the species as chosen ("Mountain Dwarf"); {@code mechanics} merges its subspecies and variant. */
    static SelectionDetail species(CatalogueRecord species, String label, JsonNode mechanics, Dnd5eOptionSummaries summaries) {
        List<Entry> facts = new ArrayList<>();
        fact(facts, "Ability bonuses", Dnd5eOptionSummaries.abilityIncreases(mechanics.get("abilityAlternatives")));
        String speed = Dnd5eOptionSummaries.speed(mechanics.get("speed"));
        fact(facts, "Speed", speed == null ? null : speed.replaceFirst("^Speed ", ""));
        fact(facts, "Size", size(mechanics.get("size")));
        fact(facts, "Languages", Dnd5eOptionSummaries.languages(mechanics.get("languageAlternatives")));
        fact(facts, "Senses", Dnd5eOptionSummaries.senses(mechanics.get("senses")));
        List<Entry> traits = new ArrayList<>();
        for (JsonNode trait : mechanics.path("traits")) {
            String name = trait.path("name").asString("");
            if (!name.isEmpty() && !COVERED_TRAITS.contains(name)) {
                traits.add(new Entry(name, summaries.fullProse(trait.path("description").asString(null))));
            }
        }
        return new SelectionDetail(label, species.sourceBook(), facts, traits);
    }

    static SelectionDetail background(CatalogueRecord background, Dnd5eBuildPlanner planner, Dnd5eOptionSummaries summaries) {
        JsonNode data = background.data();
        List<Entry> grants = new ArrayList<>();
        grant(grants, "Skill proficiencies", Dnd5eOptionSummaries.skills(data.get("skillAlternatives")));
        grant(grants, "Tool proficiencies", summaries.tools(data.get("toolAlternatives")));
        grant(grants, "Languages", Dnd5eOptionSummaries.languages(data.get("languageAlternatives")));
        for (JsonNode feature : data.path("features")) {
            grants.add(new Entry("Feature: " + feature.path("name").asString(""), summaries.fullProse(feature.path("description").asString(null))));
        }
        String equipment = planner.equipmentSummary(data.get("startingEquipment"));
        grant(grants, "Equipment", equipment.isEmpty() ? null : equipment);
        return new SelectionDetail(background.name(), background.sourceBook(), List.of(), grants);
    }

    private static String size(JsonNode size) {
        if (size == null || size.isNull()) {
            return null;
        }
        List<String> sizes = size.isArray()
                ? StreamSupport.stream(size.spliterator(), false).map(JsonNode::asString).toList()
                : List.of(size.asString());
        return sizes.stream().map(Dnd5eChoiceOptions::capitalize).collect(Collectors.joining(" or "));
    }

    private static void fact(List<Entry> facts, String label, String value) {
        if (value != null && !value.isBlank()) {
            facts.add(new Entry(label, value));
        }
    }

    private static void grant(List<Entry> grants, String label, String text) {
        if (text != null && !text.isBlank()) {
            grants.add(new Entry(label, text));
        }
    }
}

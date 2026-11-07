package dev.omnisheetvault.api.ruleset.dnd5e;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.SelectionDetail;
import dev.omnisheetvault.api.ruleset.SelectionDetail.Entry;
import dev.omnisheetvault.api.shared.InterfaceLocale;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import tools.jackson.databind.JsonNode;

/** The Species and Background steps' detail cards, built from the chosen entries' catalogue data. */
final class Dnd5eSelectionDetails {

    private static final String LANGUAGES = "Languages";

    /** Species traits the card's facts already cover, or that only describe lore. */
    private static final Set<String> COVERED_TRAITS = Set.of("Age", "Alignment", "Size", LANGUAGES, "Creature Type");

    private Dnd5eSelectionDetails() {
    }

    /** {@code label} is the species as chosen ("Mountain Dwarf"); {@code mechanics} merges its subspecies and variant. */
    static SelectionDetail species(CatalogueRecord species, String label, JsonNode mechanics, Dnd5eOptionSummaries summaries) {
        List<Entry> facts = new ArrayList<>();
        Dnd5eText words = summaries.text();
        fact(facts, "Creature type", creatureType(mechanics.get("creatureTypes"), words));
        fact(facts, "Size", size(mechanics.get("size"), words));
        fact(facts, "Speed", summaries.speedValue(mechanics.get("speed")));
        fact(facts, "Senses", summaries.senses(mechanics.get("senses")));
        fact(facts, LANGUAGES, summaries.languages(mechanics.get("languageAlternatives")));
        fact(facts, "Ability bonuses", summaries.abilityIncreases(mechanics.get("abilityAlternatives")));
        UnaryOperator<String> text = species.texts(InterfaceLocale.current());
        List<Entry> traits = new ArrayList<>();
        for (JsonNode trait : mechanics.path("traits")) {
            String name = trait.path("name").asString("");
            if (!name.isEmpty() && !COVERED_TRAITS.contains(name)) {
                traits.add(new Entry(text.apply(name), summaries.fullProse(text.apply(trait.path("description").asString(null)))));
            }
        }
        return new SelectionDetail(label, species.sourceBook(), facts, traits);
    }

    static SelectionDetail background(CatalogueRecord background, Dnd5eBuildPlanner planner, Dnd5eOptionSummaries summaries) {
        JsonNode data = background.data();
        UnaryOperator<String> text = background.texts(InterfaceLocale.current());
        List<Entry> grants = new ArrayList<>();
        grant(grants, "Skill proficiencies", summaries.skills(data.get("skillAlternatives")));
        grant(grants, "Tool proficiencies", summaries.tools(data.get("toolAlternatives")));
        grant(grants, LANGUAGES, summaries.languages(data.get("languageAlternatives")));
        for (JsonNode feature : data.path("features")) {
            grants.add(new Entry("Feature: " + text.apply(feature.path("name").asString("")),
                    summaries.fullProse(text.apply(feature.path("description").asString(null)))));
        }
        String equipment = planner.equipmentSummary(data.get("startingEquipment"));
        grant(grants, "Equipment", equipment.isEmpty() ? null : equipment);
        return new SelectionDetail(background.localizedName(InterfaceLocale.current()), background.sourceBook(), List.of(), grants);
    }

    private static String size(JsonNode size, Dnd5eText words) {
        if (size == null || size.isNull()) {
            return null;
        }
        List<String> sizes = size.isArray()
                ? StreamSupport.stream(size.spliterator(), false).map(JsonNode::asString).toList()
                : List.of(size.asString());
        return sizes.stream().map(key -> words.term("sizes", key, Dnd5eChoiceOptions.capitalize(key)))
                .collect(Collectors.joining(" " + words.text("plan.or") + " "));
    }

    /** "Fey", or "Humanoid" for a species that names none (5e tools default); several types join with "or". */
    private static String creatureType(JsonNode creatureTypes, Dnd5eText words) {
        List<String> types = creatureTypes == null || !creatureTypes.isArray() || creatureTypes.isEmpty()
                ? List.of("humanoid")
                : StreamSupport.stream(creatureTypes.spliterator(), false).map(JsonNode::asString).toList();
        return types.stream().map(type -> words.term("creatureTypes", type, Dnd5eChoiceOptions.capitalize(type)))
                .collect(Collectors.joining(" " + words.text("plan.or") + " "));
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

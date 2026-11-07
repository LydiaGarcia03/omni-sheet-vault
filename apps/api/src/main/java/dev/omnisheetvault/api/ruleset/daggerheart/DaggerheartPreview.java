package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.ruleset.SelectionDetail;
import dev.omnisheetvault.api.ruleset.SelectionDetail.Entry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * The builder's live summary of a draft, as the platform's generic selection cards: a "summary" card
 * with the derived stats and every feature so far, and one card per chosen class, subclass, heritage
 * part and transformation.
 */
final class DaggerheartPreview {

    private static final String SOURCE = "Daggerheart SRD 2.0";

    private final DaggerheartDerivedStats derivedStats;

    DaggerheartPreview(DaggerheartDerivedStats derivedStats) {
        this.derivedStats = derivedStats;
    }

    Map<String, SelectionDetail> selections(DaggerheartBuild build, ObjectNode sheet, DaggerheartCatalogue catalogue) {
        boolean redacted = catalogue.redactsProse();
        Map<String, SelectionDetail> selections = new LinkedHashMap<>();
        selections.put("summary", summary(sheet, redacted));
        JsonNode identity = sheet.path("identity");
        card(selections, "class", identity.path("class"), sheet, "class", "hope", redacted);
        card(selections, "subclass", identity.path("subclass"), sheet, "subclass", null, redacted);
        card(selections, "ancestry", identity.path("ancestry"), sheet, "ancestry", null, redacted);
        card(selections, "community", identity.path("community"), sheet, "community", null, redacted);
        card(selections, "transformation", identity.path("transformation"), sheet, "transformation", null, redacted);
        return selections;
    }

    private SelectionDetail summary(ObjectNode sheet, boolean redacted) {
        ObjectNode derived = derivedStats.derive(sheet);
        List<Entry> facts = new ArrayList<>();
        facts.add(new Entry("Level", String.valueOf(sheet.path("level").asInt())));
        facts.add(new Entry("Evasion", value(derived, "evasion")));
        facts.add(new Entry("Armor Score", value(derived, "armorScore")));
        facts.add(new Entry("Damage thresholds", value(derived, "majorThreshold") + " / " + value(derived, "severeThreshold")));
        facts.add(new Entry("Hit Points", value(derived, "hitPointSlots")));
        facts.add(new Entry("Stress", value(derived, "stressSlots")));
        facts.add(new Entry("Hope", sheet.path("trackers").path("hope").asInt() + " / " + derived.path("maxHope").asInt()));
        facts.add(new Entry("Proficiency", String.valueOf(derived.path("proficiency").asInt())));
        derived.path("traits").properties().forEach(trait -> {
            if (sheet.path("traits").has(trait.getKey())) {
                facts.add(new Entry(DaggerheartCharacterCreationFlow.capitalize(trait.getKey()),
                        DaggerheartCharacterCreationFlow.signed(trait.getValue().path("value").asInt())));
            }
        });
        sheet.path("experiences").forEach(experience ->
                facts.add(new Entry("Experience", experience.path("name").asString() + " +" + experience.path("bonus").asInt())));
        derived.path("attacks").forEach(attack -> facts.add(new Entry(attack.path("name").asString(),
                attack.path("trait").asString() + " " + DaggerheartCharacterCreationFlow.signed(attack.path("modifier").asInt())
                        + " · " + attack.path("range").asString() + " · " + attack.path("damage").asString() + " " + attack.path("damageType").asString())));
        List<Entry> grants = new ArrayList<>();
        sheet.path("features").forEach(feature -> grants.add(new Entry(feature.path("name").asString(), redacted ? null : feature.path("text").asString())));
        sheet.path("domainCards").forEach(card -> grants.add(new Entry(card.path("name").asString(), redacted ? null : card.path("text").asString())));
        return new SelectionDetail("Summary", null, facts, grants);
    }

    /** The chosen option's card: its name and the features it brings. */
    private static void card(Map<String, SelectionDetail> selections, String key, JsonNode chosen, JsonNode sheet,
            String source, String secondSource, boolean redacted) {
        if (!chosen.isObject()) {
            return;
        }
        List<Entry> grants = new ArrayList<>();
        sheet.path("features").forEach(feature -> {
            String featureSource = feature.path("source").asString();
            if (featureSource.equals(source) || featureSource.equals(secondSource)) {
                grants.add(new Entry(feature.path("name").asString(), redacted ? null : feature.path("text").asString()));
            }
        });
        List<Entry> facts = new ArrayList<>();
        if (chosen.path("spellcastTrait").isString()) {
            facts.add(new Entry("Spellcast trait", chosen.path("spellcastTrait").asString()));
        }
        selections.put(key, new SelectionDetail(chosen.path("name").asString(), SOURCE, facts, grants));
    }

    private static String value(JsonNode derived, String field) {
        return String.valueOf(derived.path(field).path("value").asInt());
    }
}

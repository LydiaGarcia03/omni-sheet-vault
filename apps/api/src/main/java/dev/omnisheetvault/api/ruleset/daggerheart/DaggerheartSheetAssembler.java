package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.ANCESTRY;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.CLASS;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.COMMUNITY;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.DOMAIN;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.DOMAIN_CARD;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.SUBCLASS;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.TRANSFORMATION;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Turns a build into a level-1 Daggerheart sheet (SRD pp. 4–6). Parts the build hasn't answered are
 * left out, so the same assembly serves the builder's live preview and the finished character.
 */
final class DaggerheartSheetAssembler {

    static final int SCHEMA_VERSION = 1;
    static final int STARTING_STRESS_SLOTS = 6;
    static final int STARTING_HOPE = 2;
    static final int STARTING_PROFICIENCY = 1;
    static final int EXPERIENCE_BONUS = 2;
    /** The ancestry feature that adds +1 to one Experience of the player's choice. */
    static final String PURPOSEFUL_DESIGN = "Purposeful Design";
    /** Step 5's fixed inventory, before the potion and the class item. */
    static final List<String> STARTING_INVENTORY = List.of("Torch", "50 feet of rope", "Basic supplies");

    private final ObjectMapper objectMapper;

    DaggerheartSheetAssembler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    ObjectNode assemble(DaggerheartBuild build, DaggerheartCatalogue catalogue) {
        Optional<CatalogueRecord> playerClass = catalogue.find(CLASS, build.classSlug());
        Optional<CatalogueRecord> subclass = catalogue.find(SUBCLASS, build.subclassSlug())
                .filter(entry -> entry.data().path("classSlug").asString().equals(build.classSlug()));

        DaggerheartLevels.Walk levels = DaggerheartLevels.walk(build, catalogue, null);
        ObjectNode sheet = objectMapper.createObjectNode();
        sheet.put("schemaVersion", SCHEMA_VERSION);
        sheet.put("level", build.level());
        sheet.put("proficiency", STARTING_PROFICIENCY + levels.proficiency);
        identity(sheet.putObject("identity"), build, catalogue, playerClass, subclass);
        multiclass((ObjectNode) sheet.get("identity"), levels.multiclass);
        base(sheet.putObject("base"), playerClass);
        sheet.set("traits", objectMapper.valueToTree(build.traits()));
        ArrayNode features = sheet.putArray("features");
        playerClass.ifPresent(entry -> {
            addFeature(features, "hope", entry.name(), entry.data().path("hopeFeature"));
            entry.data().path("features").forEach(feature -> addFeature(features, "class", entry.name(), feature));
        });
        subclass.ifPresent(entry -> {
            entry.data().path("foundation").forEach(feature -> addFeature(features, "subclass", entry.name(), feature));
            if (levels.subclassUpgrades >= 1) {
                entry.data().path("specialization").forEach(feature -> addFeature(features, "specialization", entry.name(), feature));
            }
            if (levels.subclassUpgrades >= 2) {
                entry.data().path("mastery").forEach(feature -> addFeature(features, "mastery", entry.name(), feature));
            }
        });
        if (levels.multiclass != null) {
            CatalogueRecord other = levels.multiclass.playerClass();
            other.data().path("features").forEach(feature -> addFeature(features, "multiclass", other.name(), feature));
            if (levels.multiclass.subclass() != null) {
                CatalogueRecord otherSubclass = levels.multiclass.subclass();
                otherSubclass.data().path("foundation").forEach(feature -> addFeature(features, "multiclass", otherSubclass.name(), feature));
            }
        }
        ancestryFeatures(build, catalogue).forEach(pair -> addFeature(features, "ancestry", pair.origin(), pair.feature()));
        catalogue.find(COMMUNITY, build.communitySlug())
                .ifPresent(entry -> addFeature(features, "community", entry.name(), entry.data().path("feature")));
        catalogue.find(TRANSFORMATION, build.transformationSlug())
                .ifPresent(entry -> entry.data().path("features").forEach(feature -> addFeature(features, "transformation", entry.name(), feature)));
        experiences(sheet.putArray("experiences"), build, features, levels);
        domainCards(sheet.putArray("domainCards"), levels.cards, catalogue);
        ArrayNode advancements = sheet.putArray("advancements");
        levels.gains.forEach(gain -> advancements.addObject().put("level", gain.level()).put("target", gain.target()).put("value", gain.value()));
        ObjectNode weapons = sheet.putObject("weapons");
        weapons.set("primary", item(catalogue, build.primaryWeapon(), "weapon"));
        weapons.set("secondary", item(catalogue, build.secondaryWeapon(), "weapon"));
        sheet.set("armor", item(catalogue, build.armor(), "armor"));
        inventory(sheet.putArray("inventory"), build, catalogue);
        sheet.putObject("gold").put("handfuls", 1).put("bags", 0).put("chests", 0);
        sheet.putObject("trackers").put("hitPointsMarked", 0).put("stressMarked", 0).put("armorSlotsMarked", 0).put("hope", STARTING_HOPE);
        sheet.putArray("conditions");
        sheet.put("notes", "");
        sheet.set("build", build.node().deepCopy());
        return sheet;
    }

    private void identity(ObjectNode identity, DaggerheartBuild build, DaggerheartCatalogue catalogue,
            Optional<CatalogueRecord> playerClass, Optional<CatalogueRecord> subclass) {
        identity.set("class", playerClass.map(this::reference).orElse(null));
        ObjectNode subclassNode = subclass.map(this::reference).orElse(null);
        subclass.ifPresent(entry -> subclassNode.set("spellcastTrait", entry.data().path("spellcastTrait")));
        identity.set("subclass", subclassNode);
        ArrayNode domains = identity.putArray("domains");
        playerClass.ifPresent(entry -> entry.data().path("domains").forEach(domain ->
                catalogue.find(DOMAIN, domain.asString()).ifPresent(found -> domains.add(reference(found)))));
        identity.set("ancestry", ancestry(build, catalogue));
        identity.set("community", catalogue.find(COMMUNITY, build.communitySlug()).map(this::reference).orElse(null));
        identity.set("transformation", catalogue.find(TRANSFORMATION, build.transformationSlug()).map(this::reference).orElse(null));
    }

    private ObjectNode ancestry(DaggerheartBuild build, DaggerheartCatalogue catalogue) {
        if (!build.isMixedAncestry()) {
            return catalogue.find(ANCESTRY, build.ancestrySlug()).map(this::reference).orElse(null);
        }
        Optional<CatalogueRecord> top = catalogue.find(ANCESTRY, build.mixedTopSlug());
        Optional<CatalogueRecord> bottom = catalogue.find(ANCESTRY, build.mixedBottomSlug());
        if (top.isEmpty() || bottom.isEmpty()) {
            return null;
        }
        String name = build.mixedAncestryName().isEmpty() ? top.get().name() + "-" + bottom.get().name() : build.mixedAncestryName();
        ObjectNode node = objectMapper.createObjectNode().put("name", name).put("mixed", true);
        node.putArray("from").add(reference(top.get())).add(reference(bottom.get()));
        return node;
    }

    private void base(ObjectNode base, Optional<CatalogueRecord> playerClass) {
        base.put("evasion", playerClass.map(entry -> entry.data().path("startingEvasion").asInt()).orElse(0));
        base.put("hitPointSlots", playerClass.map(entry -> entry.data().path("startingHitPoints").asInt()).orElse(0));
        base.put("stressSlots", STARTING_STRESS_SLOTS);
        base.put("origin", playerClass.map(CatalogueRecord::name).orElse(""));
    }

    record OriginFeature(String origin, JsonNode feature) {
    }

    /** One ancestry's two features, or a mixed heritage's top feature of one and bottom feature of the other. */
    static List<OriginFeature> ancestryFeatures(DaggerheartBuild build, DaggerheartCatalogue catalogue) {
        if (!build.isMixedAncestry()) {
            return catalogue.find(ANCESTRY, build.ancestrySlug())
                    .map(entry -> entry.data().path("features").valueStream().map(feature -> new OriginFeature(entry.name(), feature)).toList())
                    .orElse(List.of());
        }
        List<OriginFeature> features = new ArrayList<>();
        catalogue.find(ANCESTRY, build.mixedTopSlug())
                .ifPresent(entry -> features.add(new OriginFeature(entry.name(), entry.data().path("features").path(0))));
        catalogue.find(ANCESTRY, build.mixedBottomSlug())
                .ifPresent(entry -> features.add(new OriginFeature(entry.name(), entry.data().path("features").path(1))));
        return features;
    }

    private void addFeature(ArrayNode features, String source, String origin, JsonNode feature) {
        if (!feature.isObject()) {
            return;
        }
        ObjectNode copy = features.addObject();
        copy.put("source", source);
        copy.put("origin", origin);
        copy.put("name", feature.path("name").asString());
        copy.put("text", feature.path("text").asString());
        if (feature.has("modifiers")) {
            copy.set("modifiers", feature.path("modifiers").deepCopy());
        }
    }

    /** The two starting Experiences, then one per tier achievement; level-up bonuses go by position in that order. */
    private void experiences(ArrayNode experiences, DaggerheartBuild build, ArrayNode features, DaggerheartLevels.Walk levels) {
        boolean purposefulDesign = features.valueStream().anyMatch(feature -> PURPOSEFUL_DESIGN.equals(feature.path("name").asString()));
        List<String> names = build.experiences();
        int position = 0;
        for (int index = 0; index < names.size(); index++) {
            if (names.get(index).isEmpty()) {
                continue;
            }
            boolean boosted = purposefulDesign && build.boostedExperience() == index;
            int bonus = EXPERIENCE_BONUS + (boosted ? 1 : 0) + levels.experienceBonuses.getOrDefault(position++, 0);
            experiences.addObject().put("name", names.get(index)).put("bonus", bonus);
        }
        for (String name : levels.newExperiences) {
            experiences.addObject().put("name", name).put("bonus", DaggerheartLevels.NEW_EXPERIENCE_BONUS + levels.experienceBonuses.getOrDefault(position++, 0));
        }
    }

    private void multiclass(ObjectNode identity, DaggerheartLevels.Multiclass multiclass) {
        if (multiclass == null) {
            return;
        }
        ObjectNode node = identity.putObject("multiclass");
        node.set("class", reference(multiclass.playerClass()));
        node.put("domain", multiclass.domain());
        node.set("subclass", multiclass.subclass() == null ? null : reference(multiclass.subclass()));
    }

    private void domainCards(ArrayNode cards, List<String> slugs, DaggerheartCatalogue catalogue) {
        for (String slug : slugs) {
            catalogue.find(DOMAIN_CARD, slug).ifPresent(card -> {
                ObjectNode copy = cards.addObject();
                copy.put("slug", card.slug());
                copy.put("name", card.name());
                copy.setAll((ObjectNode) card.data().deepCopy());
                copy.put("text", card.description() == null ? "" : card.description());
                copy.put("inLoadout", true);
            });
        }
    }

    private ObjectNode item(DaggerheartCatalogue catalogue, String slug, String category) {
        return catalogue.item(slug, category).map(entry -> {
            ObjectNode copy = reference(entry);
            copy.setAll((ObjectNode) entry.data().deepCopy());
            copy.remove("category");
            return copy;
        }).orElse(null);
    }

    private void inventory(ArrayNode inventory, DaggerheartBuild build, DaggerheartCatalogue catalogue) {
        STARTING_INVENTORY.forEach(name -> inventory.addObject().put("name", name).put("quantity", 1));
        catalogue.item(build.potion(), "consumable").ifPresent(potion ->
                inventory.addObject().put("slug", potion.slug()).put("name", potion.name()).put("quantity", 1));
        if (!build.classItem().isEmpty()) {
            inventory.addObject().put("name", build.classItem()).put("quantity", 1);
        }
    }

    /** Play state and edits that outlive a re-assembly (a level up): trackers, conditions, gold, inventory, notes, counters, card places. */
    static void carryPlayState(JsonNode current, ObjectNode fresh) {
        for (String field : PLAY_STATE) {
            if (current.has(field)) {
                fresh.set(field, current.get(field).deepCopy());
            }
        }
        Map<String, Boolean> places = new HashMap<>();
        current.path("domainCards").forEach(card -> places.put(card.path("slug").asString(), card.path("inLoadout").asBoolean(true)));
        long inLoadout = places.values().stream().filter(Boolean::booleanValue).count();
        for (JsonNode card : fresh.path("domainCards")) {
            Boolean place = places.get(card.path("slug").asString());
            boolean toLoadout = place != null ? place : inLoadout < DaggerheartSheetActions.LOADOUT_SIZE;
            if (place == null && toLoadout) {
                inLoadout++;
            }
            ((ObjectNode) card).put("inLoadout", toLoadout);
        }
    }

    /** The sheet fields that hold play state rather than build results. */
    static final List<String> PLAY_STATE = List.of("trackers", "conditions", "gold", "inventory", "notes", "counters", "lastDamage", "lastRest",
            "shortRestsInARow", "vulnerableFromStress");

    private ObjectNode reference(CatalogueRecord entry) {
        return objectMapper.createObjectNode().put("slug", entry.slug()).put("name", entry.name());
    }
}

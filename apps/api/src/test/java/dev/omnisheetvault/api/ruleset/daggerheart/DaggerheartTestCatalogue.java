package dev.omnisheetvault.api.ruleset.daggerheart;

import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/** A small in-memory Daggerheart catalogue shaped like content/daggerheart, with placeholder texts. */
final class DaggerheartTestCatalogue implements CatalogueLookup {

    static final ObjectMapper MAPPER = new ObjectMapper();

    /** A complete, valid build: a Wayfinder Ranger, Slyborne Simiah, with a shortbow and leather armor. */
    static final String VALID = """
            {"class":"ranger","subclass":"wayfinder","ancestry":"simiah","mixedAncestry":null,"community":"slyborne",
             "transformationsAllowed":false,"transformation":"",
             "traits":{"agility":2,"strength":1,"finesse":1,"instinct":0,"presence":0,"knowledge":-1},
             "experiences":["Tracker","Lone Wolf"],"domainCards":["bone-card","sage-card"],
             "primaryWeapon":"shortbow","secondaryWeapon":"","armor":"leather-armor","potion":"minor-health-potion",
             "classItem":"A trophy from your first kill"}""";

    private final Map<String, Map<String, CatalogueRecord>> byKind = new LinkedHashMap<>();
    private boolean redacted;

    DaggerheartTestCatalogue() {
        add("CLASS", "ranger", "Ranger", """
                {"domains":["bone","sage"],"startingEvasion":12,"startingHitPoints":6,
                 "classItems":"A trophy from your first kill or a seemingly broken compass",
                 "hopeFeature":{"name":"Hold Them Off","text":"Hope text."},
                 "features":[{"name":"Ranger’s Focus","text":"Class text."}],"subclasses":["wayfinder","beastbound"]}""");
        add("CLASS", "wizard", "Wizard", """
                {"domains":["codex","splendor"],"startingEvasion":11,"startingHitPoints":5,"classItems":"A book or a pet",
                 "hopeFeature":{"name":"Not This Time","text":"Hope text."},"features":[{"name":"Prestidigitation","text":"Wizard text."}],
                 "subclasses":["school-of-knowledge"]}""");
        add("CLASS", "brawler", "Brawler", """
                {"domains":["valor","bone"],"startingEvasion":10,"startingHitPoints":6,"classItems":"Hand wraps or a book",
                 "hopeFeature":{"name":"Square Up","text":"Hope text."},"features":[{"name":"I Am the Weapon","text":"Unarmed text."}],
                 "subclasses":["juggernaut"]}""");
        add("SUBCLASS", "wayfinder", "Wayfinder", """
                {"classSlug":"ranger","spellcastTrait":"Agility","foundation":[{"name":"Ruthless Predator","text":"Foundation text."}],
                 "specialization":[{"name":"Elusive Predator","text":"Specialization text."}],"mastery":[{"name":"Apex Predator","text":"Mastery text."}]}""");
        add("SUBCLASS", "beastbound", "Beastbound", """
                {"classSlug":"ranger","spellcastTrait":"Agility","foundation":[],"specialization":[],"mastery":[]}""");
        add("SUBCLASS", "school-of-knowledge", "School of Knowledge", """
                {"classSlug":"wizard","spellcastTrait":"Knowledge","foundation":[{"name":"Prepared","text":"Foundation text."}],
                 "specialization":[],"mastery":[]}""");
        add("SUBCLASS", "juggernaut", "Juggernaut", """
                {"classSlug":"brawler","spellcastTrait":null,"foundation":[{"name":"Rugged","text":"Severe text.",
                 "modifiers":[{"target":"severeThreshold","value":3}]}],"specialization":[],"mastery":[]}""");
        add("ANCESTRY", "simiah", "Simiah", """
                {"features":[{"name":"Natural Climber","text":"Top text."},
                 {"name":"Nimble","text":"Evasion text.","modifiers":[{"target":"evasion","value":1}]}],"family":null}""");
        add("ANCESTRY", "clank", "Clank", """
                {"features":[{"name":"Purposeful Design","text":"Experience text."},{"name":"Efficient","text":"Rest text."}],"family":null}""");
        add("COMMUNITY", "slyborne", "Slyborne", """
                {"adjectives":["clever"],"feature":{"name":"Scoundrel","text":"Community text."}}""");
        add("TRANSFORMATION", "vampire", "Vampire", """
                {"features":[{"name":"Fangs","text":"Bite text."},{"name":"Feed","text":"Feed text."}],"questions":[]}""");
        for (String domain : List.of("bone", "sage", "codex", "splendor", "valor")) {
            add("DOMAIN", domain, domain.substring(0, 1).toUpperCase() + domain.substring(1), "{\"classes\":[]}");
        }
        add("DOMAIN_CARD", "bone-card", "Bone Card", "{\"domain\":\"bone\",\"level\":1,\"type\":\"ability\",\"recallCost\":0}");
        add("DOMAIN_CARD", "sage-card", "Sage Card", "{\"domain\":\"sage\",\"level\":1,\"type\":\"spell\",\"recallCost\":1}");
        add("DOMAIN_CARD", "valor-card", "Valor Card", "{\"domain\":\"valor\",\"level\":1,\"type\":\"ability\",\"recallCost\":1}");
        add("DOMAIN_CARD", "codex-card", "Codex Card", "{\"domain\":\"codex\",\"level\":1,\"type\":\"grimoire\",\"recallCost\":2}");
        add("DOMAIN_CARD", "sage-two", "Sage Two", "{\"domain\":\"sage\",\"level\":2,\"type\":\"spell\",\"recallCost\":1}");
        add("DOMAIN_CARD", "bone-three", "Bone Three", "{\"domain\":\"bone\",\"level\":3,\"type\":\"ability\",\"recallCost\":1}");
        add("DOMAIN_CARD", "sage-four", "Sage Four", "{\"domain\":\"sage\",\"level\":4,\"type\":\"spell\",\"recallCost\":2}");
        add("DOMAIN_CARD", "bone-five", "Bone Five", "{\"domain\":\"bone\",\"level\":5,\"type\":\"ability\",\"recallCost\":2}");
        add("DOMAIN_CARD", "codex-three", "Codex Three", "{\"domain\":\"codex\",\"level\":3,\"type\":\"grimoire\",\"recallCost\":2}");
        add("DOMAIN_CARD", "codex-five", "Codex Five", "{\"domain\":\"codex\",\"level\":5,\"type\":\"grimoire\",\"recallCost\":2}");
        add("ITEM", "shortbow", "Shortbow", weapon("primary", 1, "Agility", "Far", "d6", 3, "physical", 2, null));
        add("ITEM", "broadsword", "Broadsword", weapon("primary", 1, "Agility", "Melee", "d8", 0, "physical", 1, null));
        add("ITEM", "improved-broadsword", "Improved Broadsword", weapon("primary", 2, "Agility", "Melee", "d8", 3, "physical", 1, null));
        add("ITEM", "wand", "Wand", weapon("primary", 1, "Knowledge", "Far", "d6", 1, "magic", 1, null));
        add("ITEM", "round-shield", "Round Shield", weapon("secondary", 1, "Strength", "Melee", "d4", 0, "physical", 1,
                "{\"name\":\"Protective\",\"text\":\"+1 to Armor Score\",\"modifiers\":[{\"target\":\"armorScore\",\"value\":1}]}"));
        add("ITEM", "leather-armor", "Leather Armor", armor(1, 6, 13, 3, null));
        add("ITEM", "chainmail-armor", "Chainmail Armor", armor(1, 7, 15, 4,
                "{\"name\":\"Heavy\",\"text\":\"−1 to Evasion\",\"modifiers\":[{\"target\":\"evasion\",\"value\":-1}]}"));
        add("ITEM", "improved-leather-armor", "Improved Leather Armor", armor(2, 9, 20, 4, null));
        add("ITEM", "minor-health-potion", "Minor Health Potion", "{\"category\":\"consumable\",\"roll\":7}");
        add("ITEM", "minor-stamina-potion", "Minor Stamina Potion", "{\"category\":\"consumable\",\"roll\":8}");
    }

    DaggerheartTestCatalogue redacted() {
        this.redacted = true;
        return this;
    }

    @Override
    public Optional<CatalogueRecord> find(String kind, String slug) {
        return Optional.ofNullable(byKind.getOrDefault(kind, Map.of()).get(slug));
    }

    @Override
    public List<CatalogueRecord> list(String kind) {
        return new ArrayList<>(byKind.getOrDefault(kind, Map.of()).values());
    }

    @Override
    public boolean redactsProse() {
        return redacted;
    }

    static ObjectNode valid() {
        return (ObjectNode) MAPPER.readTree(VALID);
    }

    static String json(JsonNode node) {
        return MAPPER.writeValueAsString(node);
    }

    private void add(String kind, String slug, String name, String data) {
        byKind.computeIfAbsent(kind, key -> new LinkedHashMap<>())
                .put(slug, new CatalogueRecord(kind, slug, name, "Daggerheart SRD 2.0", 1, name + " text.", MAPPER.readTree(data)));
    }

    private static String weapon(String slot, int tier, String trait, String range, String die, int bonus, String type, int burden, String feature) {
        return """
                {"category":"weapon","slot":"%s","tier":%d,"trait":"%s","range":"%s","damage":{"die":"%s","bonus":%d,"type":"%s"},
                 "burden":%d,"wheelchair":false,"feature":%s}""".formatted(slot, tier, trait, range, die, bonus, type, burden, feature);
    }

    private static String armor(int tier, int major, int severe, int score, String feature) {
        return """
                {"category":"armor","tier":%d,"baseThresholds":{"major":%d,"severe":%d},"baseScore":%d,"feature":%s}"""
                .formatted(tier, major, severe, score, feature);
    }
}

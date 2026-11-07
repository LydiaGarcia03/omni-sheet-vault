package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.ANCESTRY;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.CLASS;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.COMMUNITY;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.DOMAIN_CARD;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.SUBCLASS;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartCatalogue.TRANSFORMATION;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import dev.omnisheetvault.api.ruleset.CreationPreview;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Daggerheart character creation (SRD pp. 4–6): class and subclass, heritage, traits, starting
 * equipment, Experiences and two level-1 domain cards, checked against the catalogue.
 */
@Component
class DaggerheartCharacterCreationFlow implements CharacterCreationFlow {

    /** The trait modifiers every character assigns, in any order. */
    static final List<Integer> TRAIT_ARRAY = List.of(2, 1, 1, 0, 0, -1);
    static final int DOMAIN_CARDS = 2;
    static final int EXPERIENCES = 2;
    static final int STARTING_TIER = 1;
    static final List<String> POTIONS = List.of("minor-health-potion", "minor-stamina-potion");
    /** Classes whose class feature gives them an unarmed primary weapon (Brawler's "I Am the Weapon"). */
    static final Set<String> UNARMED_CLASSES = Set.of("brawler");

    private final ObjectMapper objectMapper;
    private final DaggerheartSheetAssembler assembler;
    private final DaggerheartPreview previews;

    DaggerheartCharacterCreationFlow(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.assembler = new DaggerheartSheetAssembler(objectMapper);
        this.previews = new DaggerheartPreview(new DaggerheartDerivedStats(objectMapper));
    }

    @Override
    public String systemId() {
        return DaggerheartGameSystem.SYSTEM_ID;
    }

    @Override
    public String emptyDraft() {
        return DaggerheartBuild.EMPTY;
    }

    @Override
    public BuildPlan plan(String buildJson, CatalogueLookup lookup) {
        DaggerheartBuild build = DaggerheartBuild.read(buildJson, objectMapper);
        DaggerheartCatalogue catalogue = new DaggerheartCatalogue(lookup);
        List<CreationChoice> choices = new ArrayList<>();
        List<String> problems = new ArrayList<>();
        Optional<CatalogueRecord> playerClass = playerClass(build, catalogue, choices, problems);
        subclass(build, catalogue, playerClass, choices, problems);
        heritage(build, catalogue, choices, problems);
        traits(build, choices, problems);
        equipment(build, catalogue, playerClass, choices, problems);
        experiences(build, catalogue, choices, problems);
        domainCards(build, catalogue, playerClass, choices, problems);
        problems.addAll(DaggerheartLevels.walk(build, catalogue, null).problems);
        return new BuildPlan(choices, problems);
    }

    @Override
    public CreationPreview preview(String buildJson, CatalogueLookup lookup) {
        DaggerheartBuild build = DaggerheartBuild.read(buildJson, objectMapper);
        DaggerheartCatalogue catalogue = new DaggerheartCatalogue(lookup);
        return new CreationPreview(Map.of(), null, List.of(), previews.selections(build, assembler.assemble(build, catalogue), catalogue), null);
    }

    @Override
    public MaterializedSheet materialize(String currentSheetJson, String buildJson, CatalogueLookup lookup) {
        BuildPlan plan = plan(buildJson, lookup);
        if (!plan.pending().isEmpty() || !plan.problems().isEmpty()) {
            return new MaterializedSheet(null, List.of(), 0, plan);
        }
        DaggerheartBuild build = DaggerheartBuild.read(buildJson, objectMapper);
        ObjectNode sheet = assembler.assemble(build, new DaggerheartCatalogue(lookup));
        if (currentSheetJson != null) {
            DaggerheartSheetAssembler.carryPlayState(objectMapper.readTree(currentSheetJson), sheet);
        }
        return new MaterializedSheet(objectMapper.writeValueAsString(sheet), List.of(), 0, plan);
    }

    @Override
    public Optional<String> buildOf(String sheetJson) {
        JsonNode build = objectMapper.readTree(sheetJson).path("build");
        return build.isObject() ? Optional.of(objectMapper.writeValueAsString(build)) : Optional.empty();
    }

    // Step 1: class and subclass

    private Optional<CatalogueRecord> playerClass(DaggerheartBuild build, DaggerheartCatalogue catalogue,
            List<CreationChoice> choices, List<String> problems) {
        Optional<CatalogueRecord> playerClass = known(catalogue, CLASS, build.classSlug(), "class", problems);
        choices.add(choice("class", "class", "Class", 1, false, options(catalogue.list(CLASS), DaggerheartCharacterCreationFlow::classSummary),
                playerClass.map(CatalogueRecord::slug)));
        return playerClass;
    }

    private void subclass(DaggerheartBuild build, DaggerheartCatalogue catalogue, Optional<CatalogueRecord> playerClass,
            List<CreationChoice> choices, List<String> problems) {
        Optional<CatalogueRecord> subclass = known(catalogue, SUBCLASS, build.subclassSlug(), "subclass", problems);
        boolean ofClass = subclass.isPresent() && subclass.get().data().path("classSlug").asString().equals(build.classSlug());
        if (subclass.isPresent() && playerClass.isPresent() && !ofClass) {
            problems.add(subclass.get().name() + " is not a " + playerClass.get().name() + " subclass.");
        }
        List<CatalogueRecord> options = catalogue.list(SUBCLASS, data -> data.path("classSlug").asString().equals(build.classSlug()));
        choices.add(choice("subclass", "class", "Subclass", 1, false, options(options, DaggerheartCharacterCreationFlow::subclassSummary),
                subclass.filter(found -> ofClass).map(CatalogueRecord::slug)));
    }

    // Step 2: heritage

    private void heritage(DaggerheartBuild build, DaggerheartCatalogue catalogue, List<CreationChoice> choices, List<String> problems) {
        Optional<String> ancestry;
        if (build.isMixedAncestry()) {
            Optional<CatalogueRecord> top = known(catalogue, ANCESTRY, build.mixedTopSlug(), "ancestry", problems);
            Optional<CatalogueRecord> bottom = known(catalogue, ANCESTRY, build.mixedBottomSlug(), "ancestry", problems);
            if (top.isPresent() && bottom.isPresent() && top.get().slug().equals(bottom.get().slug())) {
                problems.add("A mixed ancestry takes its two features from two different ancestries.");
            }
            boolean complete = top.isPresent() && bottom.isPresent() && !top.get().slug().equals(bottom.get().slug());
            ancestry = complete ? Optional.of("mixed:" + top.get().slug() + "+" + bottom.get().slug()) : Optional.empty();
        } else {
            ancestry = known(catalogue, ANCESTRY, build.ancestrySlug(), "ancestry", problems).map(CatalogueRecord::slug);
        }
        choices.add(choice("ancestry", "heritage", "Ancestry", 1, false, options(catalogue.list(ANCESTRY), DaggerheartCharacterCreationFlow::featureNames), ancestry));
        Optional<CatalogueRecord> community = known(catalogue, COMMUNITY, build.communitySlug(), "community", problems);
        choices.add(choice("community", "heritage", "Community", 1, false, options(catalogue.list(COMMUNITY), DaggerheartCharacterCreationFlow::featureNames),
                community.map(CatalogueRecord::slug)));
        if (build.transformationsAllowed()) {
            Optional<CatalogueRecord> transformation = known(catalogue, TRANSFORMATION, build.transformationSlug(), "transformation", problems);
            choices.add(choice("transformation", "heritage", "Transformation", 1, true,
                    options(catalogue.list(TRANSFORMATION), DaggerheartCharacterCreationFlow::featureNames), transformation.map(CatalogueRecord::slug)));
        }
    }

    // Step 3: traits

    private void traits(DaggerheartBuild build, List<CreationChoice> choices, List<String> problems) {
        Map<String, Integer> traits = build.traits();
        traits.keySet().stream().filter(trait -> !DaggerheartBuild.TRAITS.contains(trait)).forEach(trait -> problems.add("Unknown trait: " + trait));
        List<Integer> remaining = new ArrayList<>(TRAIT_ARRAY);
        List<String> assigned = new ArrayList<>();
        for (String trait : DaggerheartBuild.TRAITS) {
            Integer value = traits.get(trait);
            if (value == null) {
                continue;
            }
            if (!remaining.remove(value)) {
                problems.add("Traits use the modifiers +2, +1, +1, +0, +0 and −1 once each; " + capitalize(trait) + " can't be " + signed(value) + ".");
            }
            assigned.add(trait);
        }
        choices.add(new CreationChoice("traits", "traits", null, "Trait modifiers", null, DaggerheartBuild.TRAITS.size(), false, List.of(), assigned));
    }

    // Step 5: starting equipment

    private void equipment(DaggerheartBuild build, DaggerheartCatalogue catalogue, Optional<CatalogueRecord> playerClass,
            List<CreationChoice> choices, List<String> problems) {
        boolean spellcaster = catalogue.find(SUBCLASS, build.subclassSlug()).map(entry -> !entry.data().path("spellcastTrait").isNull()
                && !entry.data().path("spellcastTrait").asString("").isEmpty()).orElse(false);
        boolean unarmed = UNARMED_CLASSES.contains(build.classSlug());

        Optional<CatalogueRecord> primary = startingWeapon(build.primaryWeapon(), "primary", catalogue, spellcaster, problems);
        choices.add(choice("primary-weapon", "equipment", "Primary weapon", 1, unarmed,
                options(startingWeapons(catalogue, "primary", spellcaster), DaggerheartCharacterCreationFlow::weaponSummary), primary.map(CatalogueRecord::slug)));
        Optional<CatalogueRecord> secondary = startingWeapon(build.secondaryWeapon(), "secondary", catalogue, spellcaster, problems);
        boolean twoHanded = primary.map(weapon -> weapon.data().path("burden").asInt() == 2).orElse(false);
        if (twoHanded && secondary.isPresent()) {
            problems.add(primary.get().name() + " is two-handed, so it leaves no hand for a secondary weapon.");
        }
        if (primary.isEmpty() && secondary.isPresent()) {
            problems.add("A secondary weapon goes with a one-handed primary weapon.");
        }
        if (primary.isPresent() && !twoHanded) {
            choices.add(choice("secondary-weapon", "equipment", "Secondary weapon", 1, false,
                    options(startingWeapons(catalogue, "secondary", spellcaster), DaggerheartCharacterCreationFlow::weaponSummary),
                    secondary.map(CatalogueRecord::slug)));
        }

        Optional<CatalogueRecord> armor = Optional.empty();
        if (!build.armor().isEmpty()) {
            armor = catalogue.item(build.armor(), "armor");
            if (armor.isEmpty()) {
                problems.add("Unknown armor: " + build.armor());
            } else if (armor.get().data().path("tier").asInt() != STARTING_TIER) {
                problems.add(armor.get().name() + " is not Tier 1 armor.");
                armor = Optional.empty();
            }
        }
        choices.add(choice("armor", "equipment", "Armor", 1, false,
                options(catalogue.list(DaggerheartCatalogue.ITEM, data -> "armor".equals(data.path("category").asString())
                        && data.path("tier").asInt() == STARTING_TIER), DaggerheartCharacterCreationFlow::armorSummary),
                armor.map(CatalogueRecord::slug)));

        if (!build.potion().isEmpty() && !POTIONS.contains(build.potion())) {
            problems.add("The starting potion is a Minor Health Potion or a Minor Stamina Potion.");
        }
        List<CatalogueRecord> potions = POTIONS.stream().flatMap(slug -> catalogue.item(slug, "consumable").stream()).toList();
        choices.add(choice("potion", "equipment", "Potion", 1, false, options(potions, entry -> catalogue.redactsProse() ? null : entry.description()),
                POTIONS.contains(build.potion()) ? Optional.of(build.potion()) : Optional.empty()));

        List<String> classItems = playerClass.map(entry -> classItems(entry.data())).orElse(List.of());
        boolean classItemKnown = classItems.stream().anyMatch(item -> item.equalsIgnoreCase(build.classItem()));
        if (!build.classItem().isEmpty() && playerClass.isPresent() && !classItemKnown) {
            problems.add("\"" + build.classItem() + "\" is not one of the " + playerClass.get().name() + "'s class items.");
        }
        choices.add(choice("class-item", "equipment", "Class item", 1, false,
                classItems.stream().map(item -> new CreationChoiceOption(item, item, null, null)).toList(),
                classItemKnown ? Optional.of(build.classItem()) : Optional.empty()));
    }

    private Optional<CatalogueRecord> startingWeapon(String slug, String slot, DaggerheartCatalogue catalogue, boolean spellcaster, List<String> problems) {
        if (slug.isEmpty()) {
            return Optional.empty();
        }
        Optional<CatalogueRecord> weapon = catalogue.item(slug, "weapon");
        if (weapon.isEmpty()) {
            problems.add("Unknown weapon: " + slug);
            return Optional.empty();
        }
        JsonNode data = weapon.get().data();
        if (data.path("tier").asInt() != STARTING_TIER) {
            problems.add(weapon.get().name() + " is not a Tier 1 weapon.");
        } else if (!slot.equals(data.path("slot").asString())) {
            problems.add(weapon.get().name() + " is not a " + slot + " weapon.");
        } else if (isMagic(data) && !spellcaster) {
            problems.add(weapon.get().name() + " is a magic weapon, which needs a subclass with a Spellcast trait.");
        } else {
            return weapon;
        }
        return Optional.empty();
    }

    private static List<CatalogueRecord> startingWeapons(DaggerheartCatalogue catalogue, String slot, boolean spellcaster) {
        return catalogue.list(DaggerheartCatalogue.ITEM, data -> "weapon".equals(data.path("category").asString())
                && data.path("tier").asInt() == STARTING_TIER && slot.equals(data.path("slot").asString()) && (spellcaster || !isMagic(data)));
    }

    /** Magic damage, or a Spellcast trait, marks a weapon only a spellcaster can wield (SRD p. 55). */
    static boolean isMagic(JsonNode weapon) {
        return !"physical".equals(weapon.path("damage").path("type").asString()) || "Spellcast".equals(weapon.path("trait").asString());
    }

    /** "A romance novel or a letter never opened" offers two items. */
    static List<String> classItems(JsonNode playerClass) {
        String items = playerClass.path("classItems").asString("");
        if (items.isEmpty()) {
            return List.of();
        }
        return List.of(items.split(" or ")).stream().map(String::trim).map(DaggerheartCharacterCreationFlow::capitalize).toList();
    }

    // Step 7: Experiences

    private void experiences(DaggerheartBuild build, DaggerheartCatalogue catalogue, List<CreationChoice> choices, List<String> problems) {
        List<String> named = build.experiences().stream().filter(name -> !name.isEmpty()).toList();
        if (named.size() > EXPERIENCES) {
            problems.add("A new character has " + EXPERIENCES + " Experiences.");
        }
        if (new HashSet<>(named.stream().map(String::toLowerCase).toList()).size() < named.size()) {
            problems.add("The two Experiences must be different.");
        }
        choices.add(new CreationChoice("experiences", "experiences", null, "Experiences", null, EXPERIENCES, false, List.of(),
                named.stream().limit(EXPERIENCES).toList()));
        boolean purposefulDesign = DaggerheartSheetAssembler.ancestryFeatures(build, catalogue).stream()
                .anyMatch(pair -> DaggerheartSheetAssembler.PURPOSEFUL_DESIGN.equals(pair.feature().path("name").asString()));
        if (purposefulDesign) {
            int boosted = build.boostedExperience();
            boolean valid = boosted >= 0 && boosted < EXPERIENCES && boosted < build.experiences().size() && !build.experiences().get(boosted).isEmpty();
            choices.add(choice("boosted-experience", "experiences", "Experience for " + DaggerheartSheetAssembler.PURPOSEFUL_DESIGN, 1, false, List.of(),
                    valid ? Optional.of(String.valueOf(boosted)) : Optional.empty()));
        }
    }

    // Step 8: domain cards

    private void domainCards(DaggerheartBuild build, DaggerheartCatalogue catalogue, Optional<CatalogueRecord> playerClass,
            List<CreationChoice> choices, List<String> problems) {
        Set<String> domains = new HashSet<>();
        playerClass.ifPresent(entry -> entry.data().path("domains").forEach(domain -> domains.add(domain.asString())));
        List<String> picked = new ArrayList<>();
        for (String slug : build.domainCards()) {
            Optional<CatalogueRecord> card = known(catalogue, DOMAIN_CARD, slug, "domain card", problems);
            if (card.isEmpty()) {
                continue;
            }
            JsonNode data = card.get().data();
            if (data.path("level").asInt() != 1) {
                problems.add(card.get().name() + " is a level " + data.path("level").asInt() + " card; a new character takes level 1 cards.");
            } else if (playerClass.isPresent() && !domains.contains(data.path("domain").asString())) {
                problems.add(card.get().name() + " is not from the " + playerClass.get().name() + "'s domains.");
            } else if (picked.contains(slug)) {
                problems.add(card.get().name() + " is picked twice.");
            } else {
                picked.add(slug);
            }
        }
        if (picked.size() > DOMAIN_CARDS) {
            problems.add("A new character takes " + DOMAIN_CARDS + " domain cards.");
        }
        List<CatalogueRecord> options = catalogue.list(DOMAIN_CARD, data -> data.path("level").asInt() == 1 && domains.contains(data.path("domain").asString()));
        choices.add(new CreationChoice("domain-cards", "domains", null, "Domain cards", null, DOMAIN_CARDS, false,
                options(options, DaggerheartCharacterCreationFlow::cardSummary), picked.stream().limit(DOMAIN_CARDS).toList()));
    }

    // Helpers

    private static Optional<CatalogueRecord> known(DaggerheartCatalogue catalogue, String kind, String slug, String label, List<String> problems) {
        if (slug.isEmpty()) {
            return Optional.empty();
        }
        Optional<CatalogueRecord> entry = catalogue.find(kind, slug);
        if (entry.isEmpty()) {
            problems.add("Unknown " + label + ": " + slug);
        }
        return entry;
    }

    private static CreationChoice choice(String id, String step, String prompt, int count, boolean optional,
            List<CreationChoiceOption> options, Optional<String> selected) {
        return new CreationChoice(id, step, null, prompt, null, count, optional, options, selected.stream().toList());
    }

    private static List<CreationChoiceOption> options(List<CatalogueRecord> entries, Function<CatalogueRecord, String> summary) {
        return entries.stream()
                .sorted(Comparator.comparing(CatalogueRecord::name))
                .map(entry -> new CreationChoiceOption(entry.slug(), entry.name(), entry.sourceBook(), summary.apply(entry)))
                .toList();
    }

    private static String classSummary(CatalogueRecord entry) {
        JsonNode data = entry.data();
        List<String> domains = new ArrayList<>();
        data.path("domains").forEach(domain -> domains.add(capitalize(domain.asString())));
        return String.join(" & ", domains) + " · Evasion " + data.path("startingEvasion").asInt() + " · HP " + data.path("startingHitPoints").asInt();
    }

    private static String subclassSummary(CatalogueRecord entry) {
        String trait = entry.data().path("spellcastTrait").asString("");
        return trait.isEmpty() ? "No Spellcast trait" : "Spellcast: " + trait;
    }

    private static String featureNames(CatalogueRecord entry) {
        JsonNode data = entry.data();
        List<String> names = new ArrayList<>();
        data.path("features").forEach(feature -> names.add(feature.path("name").asString()));
        if (data.path("feature").isObject()) {
            names.add(data.path("feature").path("name").asString());
        }
        return String.join(" · ", names);
    }

    private static String weaponSummary(CatalogueRecord entry) {
        JsonNode data = entry.data();
        JsonNode damage = data.path("damage");
        int bonus = damage.path("bonus").asInt();
        return data.path("trait").asString() + " · " + data.path("range").asString() + " · " + damage.path("die").asString()
                + (bonus > 0 ? "+" + bonus : "") + " " + damage.path("type").asString() + " · "
                + (data.path("burden").asInt() == 2 ? "Two-Handed" : "One-Handed");
    }

    private static String armorSummary(CatalogueRecord entry) {
        JsonNode data = entry.data();
        return "Thresholds " + data.path("baseThresholds").path("major").asInt() + " / " + data.path("baseThresholds").path("severe").asInt()
                + " · Score " + data.path("baseScore").asInt();
    }

    private static String cardSummary(CatalogueRecord entry) {
        JsonNode data = entry.data();
        return capitalize(data.path("domain").asString()) + " " + capitalize(data.path("type").asString()) + " · Recall " + data.path("recallCost").asInt();
    }

    static String capitalize(String text) {
        return text.isEmpty() ? text : Character.toUpperCase(text.charAt(0)) + text.substring(1);
    }

    static String signed(int value) {
        return value > 0 ? "+" + value : value < 0 ? "−" + -value : "+0";
    }
}

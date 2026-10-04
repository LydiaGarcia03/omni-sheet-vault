package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import dev.omnisheetvault.api.ruleset.ChoicePlacement;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Catalogue fixtures follow the shapes the A1–A3 converters write (trimmed PHB Dwarf, Soldier, Fighter, Rogue). */
class Dnd5eBuildPlannerTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void asksForTheSubspeciesBeforeAnythingThatDependsOnIt() {
        BuildPlan plan = plan(build(null, "fighter", null, 1, List.of()));

        assertThat(ids(plan)).startsWith("build.abilityScores", "build.species", "build.subspecies");
        assertThat(ids(plan)).noneMatch(id -> id.startsWith("species:"));
    }

    @Test
    void anEmptyDraftAsksForItsStructureFirst() {
        BuildPlan plan = plan(new Dnd5eCharacterBuild(null, null, null, null, null, null, null, null, null, null));

        assertThat(plan.pending()).extracting(CreationChoice::id)
                .containsExactly("build.abilityScores", "build.species", "build.background", "build.classes");
        assertThat(choice(plan, "build.species").options()).extracting(CreationChoiceOption::label).isSortedAccordingTo(String.CASE_INSENSITIVE_ORDER);
        assertThat(choice(plan, "build.species").options()).extracting(CreationChoiceOption::key).contains("dwarf", "changeling");
        assertThat(choice(plan, "build.classes").options()).extracting(CreationChoiceOption::key).contains("fighter", "rogue");
        assertThat(plan.problems()).isEmpty();
    }

    @Test
    void echoesTheChosenStructureAsAnsweredChoices() {
        BuildPlan plan = plan(build("Mountain", "fighter", null, 1, List.of()));

        assertThat(choice(plan, "build.species").selected()).containsExactly("dwarf");
        assertThat(choice(plan, "build.background").selected()).containsExactly("soldier");
        assertThat(choice(plan, "build.classes").selected()).containsExactly("fighter");
        assertThat(choice(plan, "build.abilityScores").selected()).containsExactly("STANDARD_ARRAY");
        assertThat(plan.pending()).extracting(CreationChoice::id).noneMatch(id -> id.startsWith("build.") && !id.equals("build.class.fighter.subclass"));
    }

    @Test
    void offersEachAbilityScoreMethodWithItsRulesAsData() {
        CreationChoice methods = choice(plan(build("Mountain", "fighter", null, 1, List.of())), "build.abilityScores");

        CreationChoiceOption pointBuy = methods.options().stream().filter(option -> option.key().equals("POINT_BUY")).findFirst().orElseThrow();
        CreationChoiceOption array = methods.options().stream().filter(option -> option.key().equals("STANDARD_ARRAY")).findFirst().orElseThrow();
        assertThat(pointBuy.data().path("budget").asInt()).isEqualTo(27);
        assertThat(pointBuy.data().path("costs").path("15").asInt()).isEqualTo(9);
        assertThat(array.data().path("values").toString()).isEqualTo("[15,14,13,12,10,8]");
        CreationChoiceOption manual = methods.options().stream().filter(option -> option.key().equals("MANUAL")).findFirst().orElseThrow();
        assertThat(manual.data().path("roll").toString()).isEqualTo("{\"count\":4,\"sides\":6,\"keepHighest\":3,\"label\":\"4d6 drop lowest\"}");
    }

    @Test
    void appliesOtherModifiersAndOverridesAfterEverythingElse() {
        Dnd5eCharacterBuild base = build("Mountain", "fighter", null, 1, List.of());
        Dnd5eCharacterBuild adjusted = new Dnd5eCharacterBuild(base.speciesSlug(), base.subspeciesName(), null, base.backgroundSlug(),
                base.classes(), base.abilityScoreMethod(), base.baseAbilityScores(), null, null, null,
                Map.of("strength", new Dnd5eAbilityAdjustment(1, null), "wisdom", new Dnd5eAbilityAdjustment(null, 18)));

        Dnd5eBuildPlanner planner = new Dnd5eBuildPlanner(adjusted, catalogue());
        planner.plan();

        assertThat(planner.state().score("strength")).isEqualTo(15 + 2 + 1);
        assertThat(planner.state().abilityContributions().get("strength")).extracting(Dnd5eDerivation.SourcedAmount::source)
                .containsExactly("Base", "Mountain Dwarf", "Other modifier");
        assertThat(planner.state().score("wisdom")).isEqualTo(18);
    }

    @Test
    void offersOnlyEntriesFromAllowedSources() {
        CatalogueLookup catalogue = catalogue();
        Dnd5ePreferences onlyCore = new Dnd5ePreferences(Set.of("PHB"), null, null, null, null, null, null);
        Dnd5eCharacterBuild draft = new Dnd5eCharacterBuild(null, null, null, null, null, null, null, null, null, null, null, onlyCore);
        CatalogueLookup withExpansion = new CatalogueLookup() {
            @Override
            public Optional<CatalogueRecord> find(String kind, String slug) {
                return catalogue.find(kind, slug);
            }

            @Override
            public List<CatalogueRecord> list(String kind) {
                List<CatalogueRecord> entries = new ArrayList<>(catalogue.list(kind));
                entries.add(new CatalogueRecord(kind, "expansion-" + kind.toLowerCase(), "Expansion " + kind, "Volo's Guide to Monsters", 1, "", MAPPER.readTree("{}")));
                return entries;
            }
        };

        BuildPlan plan = new Dnd5eBuildPlanner(draft, withExpansion).plan();

        assertThat(choice(plan, "build.species").options()).extracting(CreationChoiceOption::key)
                .doesNotContain("expansion-species").contains("dwarf");
        assertThat(new Dnd5ePreferences(Set.of(), null, null, null, null, null, null).allowsSource("Player's Handbook")).isTrue();
        assertThat(new Dnd5ePreferences(Set.of(), null, null, null, null, null, null).allowsSource("Dungeon Master's Guide")).isTrue();
    }

    @Test
    void offersPlaytestEntriesOnlyWhenPlaytestContentIsOn() {
        CatalogueLookup withPlaytest = withExtraSpecies(new CatalogueRecord("SPECIES", "playtest-species", "Playtest Species", "UA: Test", 1, "",
                MAPPER.readTree("{}"), true, null));
        Dnd5ePreferences withPlaytestOn = new Dnd5ePreferences(null, null, null, null, null, null, null, true, null, null);

        assertThat(speciesKeys(Dnd5ePreferences.forNewCharacter(), withPlaytest)).doesNotContain("playtest-species");
        assertThat(speciesKeys(withPlaytestOn, withPlaytest)).contains("playtest-species", "dwarf");
        assertThat(Dnd5ePreferences.defaults().playtestContent()).isFalse();
    }

    @Test
    void offersPartneredEntriesByPartnerWhateverTheBookList() {
        CatalogueLookup withPartner = withExtraSpecies(new CatalogueRecord("SPECIES", "partner-species", "Partner Species", "Explorer's Guide to Wildemount", 1, "",
                MAPPER.readTree("{}"), false, "Critical Role"));
        Dnd5ePreferences coreOnlyEveryPartner = new Dnd5ePreferences(Set.of(), null, null, null, null, null, null, false, null, null);
        Dnd5ePreferences noPartners = new Dnd5ePreferences(null, null, null, null, null, null, null, false, true, Set.of());
        Dnd5ePreferences criticalRoleOnly = new Dnd5ePreferences(null, null, null, null, null, null, null, false, true, Set.of("Critical Role"));
        Dnd5ePreferences partneredContentOff = new Dnd5ePreferences(null, null, null, null, null, null, null, false, false, Set.of("Critical Role"));

        assertThat(speciesKeys(coreOnlyEveryPartner, withPartner)).contains("partner-species");
        assertThat(speciesKeys(noPartners, withPartner)).doesNotContain("partner-species").contains("dwarf");
        assertThat(speciesKeys(criticalRoleOnly, withPartner)).contains("partner-species");
        assertThat(speciesKeys(partneredContentOff, withPartner)).doesNotContain("partner-species");
        assertThat(Dnd5ePreferences.defaults().partneredContent()).isTrue();
    }

    private CatalogueLookup withExtraSpecies(CatalogueRecord extra) {
        CatalogueLookup catalogue = catalogue();
        return new CatalogueLookup() {
            @Override
            public Optional<CatalogueRecord> find(String kind, String slug) {
                return catalogue.find(kind, slug);
            }

            @Override
            public List<CatalogueRecord> list(String kind) {
                List<CatalogueRecord> entries = new ArrayList<>(catalogue.list(kind));
                if (kind.equals(extra.kind())) {
                    entries.add(extra);
                }
                return entries;
            }
        };
    }

    private static List<String> speciesKeys(Dnd5ePreferences preferences, CatalogueLookup catalogue) {
        Dnd5eCharacterBuild draft = new Dnd5eCharacterBuild(null, null, null, null, null, null, null, null, null, null, null, preferences);
        return choice(new Dnd5eBuildPlanner(draft, catalogue).plan(), "build.species").options().stream().map(CreationChoiceOption::key).toList();
    }

    @Test
    void skipsMulticlassRequirementsWhenThePreferenceIsOff() {
        Dnd5eCharacterBuild base = new Dnd5eCharacterBuild(
                "dwarf", "Mountain", null, "soldier", List.of(new Dnd5eBuildClass("fighter", null, 1), new Dnd5eBuildClass("rogue", null, 1)),
                Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 15, "dexterity", 8, "constitution", 14, "intelligence", 8, "wisdom", 12, "charisma", 10),
                null, null, null);
        Dnd5eCharacterBuild relaxed = new Dnd5eCharacterBuild(base.speciesSlug(), base.subspeciesName(), null, base.backgroundSlug(),
                base.classes(), base.abilityScoreMethod(), base.baseAbilityScores(), null, null, null, null,
                new Dnd5ePreferences(null, null, null, false, null, null, null));

        assertThat(plan(base).problems()).anyMatch(problem -> problem.contains("multiclass requires"));
        assertThat(plan(relaxed).problems()).noneMatch(problem -> problem.contains("multiclass requires"));
    }

    @Test
    void reportsAnUnknownSpeciesOnceAndLeavesItUnanswered() {
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(
                "not-a-species", null, null, "soldier", List.of(new Dnd5eBuildClass("fighter", null, 1)),
                Dnd5eAbilityScoreMethod.MANUAL, Map.of(), null, null, null);

        BuildPlan plan = plan(build);

        assertThat(plan.problems()).containsOnlyOnce("Unknown species: not-a-species");
        assertThat(choice(plan, "build.species").isPending()).isTrue();
        assertThat(choice(plan, "build.abilityScores").isPending()).isTrue();
    }

    @Test
    void combinesSpeciesAndSubspeciesGrantsInsteadOfOfferingThemAsAlternatives() {
        BuildPlan plan = plan(build("Mountain", "fighter", null, 1, List.of()));

        assertThat(ids(plan)).doesNotContain("species:dwarf:ability-set");
        assertThat(choice(plan, "species:dwarf:tools:0").options()).extracting(CreationChoiceOption::key)
                .containsExactly("smiths-tools", "brewers-supplies");
    }

    @Test
    void labelsBackgroundProficienciesAndDropsOnesAnotherSourceGranted() {
        BuildPlan plan = plan(build("Mountain", "fighter", null, 1, List.of()));

        List<CreationChoiceOption> skills = choice(plan, "class:fighter:skills:0").options();
        assertThat(skills).extracting(CreationChoiceOption::label).contains("Athletics (Soldier)", "Perception");
    }

    @Test
    void pickingAProficiencyTheBackgroundAlreadyGaveAsksForAReplacement() {
        BuildPlan plan = plan(build("Mountain", "fighter", null, 1, List.of(answer("class:fighter:skills:0", "athletics"))));

        CreationChoice replacement = choice(plan, "class:fighter:skills:0:replace:athletics");
        assertThat(replacement.prompt()).isEqualTo("Replace duplicate Athletics (already from Soldier)");
        assertThat(replacement.isPending()).isTrue();
        assertThat(replacement.options()).extracting(CreationChoiceOption::key).doesNotContain("athletics").contains("perception");
    }

    @Test
    void theReplacementProficiencyIsGrantedInsteadOfTheDuplicate() {
        BuildPlan plan = plan(build("Mountain", "fighter", null, 1, List.of(
                answer("class:fighter:skills:0", "athletics"),
                answer("class:fighter:skills:0:replace:athletics", "perception"))));

        assertThat(choice(plan, "class:fighter:skills:0:replace:athletics").isPending()).isFalse();
        assertThat(plan.problems()).isEmpty();
    }

    @Test
    void anAnsweredAsiOpensAnAbilitySubChoiceAndLaterPicksSeeTheIncrease() {
        BuildPlan plan = plan(build("Mountain", "fighter", null, 4, List.of(
                answer("class:fighter:4:asi-or-feat", "asi"),
                answer("class:fighter:4:asi", "strength", "strength"))));

        CreationChoice increase = choice(plan, "class:fighter:4:asi");
        assertThat(increase.parentChoiceId()).isEqualTo("class:fighter:4:asi-or-feat");
        assertThat(increase.isPending()).isFalse();
        assertThat(plan.problems()).isEmpty();
    }

    @Test
    void offersOnlyTheNewOptionalFeaturesAtEachLevel() {
        BuildPlan plan = plan(build("Mountain", "fighter", "fighter-battle-master", 7, List.of()));

        assertThat(choice(plan, "class:fighter:1:fighting-style").count()).isEqualTo(1);
        assertThat(choice(plan, "subclass:fighter-battle-master:3:maneuvers").count()).isEqualTo(3);
        assertThat(choice(plan, "subclass:fighter-battle-master:7:maneuvers").count()).isEqualTo(2);
        assertThat(choice(plan, "class:fighter:1:fighting-style").options()).extracting(CreationChoiceOption::key)
                .containsExactly("defense").doesNotContain("blind-fighting");
    }

    @Test
    void expertiseOffersOnlyProficienciesTheCharacterAlreadyHas() {
        BuildPlan plan = plan(build("Mountain", "rogue", null, 1, List.of(answer("class:rogue:skills:0", "stealth"))));

        assertThat(choice(plan, "class:rogue:1:expertise:0").options()).extracting(CreationChoiceOption::key)
                .containsExactlyInAnyOrder("athletics", "intimidation", "stealth", "thieves-tools");
    }

    @Test
    void reportsRuleProblemsWithoutBlockingThePlan() {
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(
                "dwarf", "Mountain", null, "soldier",
                List.of(new Dnd5eBuildClass("fighter", null, 1), new Dnd5eBuildClass("rogue", null, 1)),
                Dnd5eAbilityScoreMethod.STANDARD_ARRAY,
                Map.of("strength", 15, "dexterity", 8, "constitution", 14, "intelligence", 10, "wisdom", 12, "charisma", 15),
                Dnd5eHitPointMethod.ROLLED, List.of(), List.of(answer("class:wizard:1:nonsense", "x")));

        BuildPlan plan = plan(build);

        assertThat(plan.problems()).anyMatch(problem -> problem.startsWith("Standard array"));
        assertThat(plan.problems()).anyMatch(problem -> problem.startsWith("Rogue multiclass requires dexterity"));
        assertThat(plan.problems()).anyMatch(problem -> problem.contains("class:wizard:1:nonsense"));
        assertThat(choice(plan, "build.rolledHitPoints").isPending()).isTrue();
    }

    @Test
    void filesEachClassChoiceUnderItsClassAndLevel() {
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(
                "dwarf", "Mountain", null, "soldier", List.of(new Dnd5eBuildClass("fighter", null, 4)),
                Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 15, "dexterity", 13, "constitution", 14, "intelligence", 8, "wisdom", 12, "charisma", 10),
                Dnd5eHitPointMethod.FIXED, List.of(), List.of());

        BuildPlan plan = plan(build);

        assertThat(choice(plan, "class:fighter:skills:0").placement()).isEqualTo(new ChoicePlacement("fighter", 1));
        assertThat(choice(plan, "class:fighter:4:asi-or-feat").placement()).isEqualTo(new ChoicePlacement("fighter", 4));
        assertThat(choice(plan, "build.species").placement()).isNull();
        assertThat(choice(plan, "build.classes").placement()).isNull();
    }

    @Test
    void reportsAnAbilityScoreImprovementThatExceedsTwenty() {
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(
                "dwarf", "Mountain", null, "soldier", List.of(new Dnd5eBuildClass("fighter", null, 4)),
                Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 17, "dexterity", 13, "constitution", 14, "intelligence", 8, "wisdom", 12, "charisma", 10),
                Dnd5eHitPointMethod.FIXED, List.of(),
                List.of(answer("class:fighter:4:asi-or-feat", "asi"), answer("class:fighter:4:asi", "strength", "strength")));

        BuildPlan plan = plan(build);

        assertThat(plan.problems()).anyMatch(problem -> problem.contains("strength to 21, above its maximum of 20"));
    }

    @Test
    void weightedAbilityIncreasesApplyInSelectionOrderToDistinctAbilities() {
        CatalogueLookup catalogue = catalogue();
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(
                "changeling", null, null, "soldier", List.of(new Dnd5eBuildClass("rogue", null, 1)),
                Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 8, "dexterity", 13, "constitution", 12, "intelligence", 10, "wisdom", 10, "charisma", 12),
                Dnd5eHitPointMethod.FIXED, List.of(),
                List.of(answer("species:changeling:ability-set", "0"), answer("species:changeling:ability:0", "dexterity", "charisma")));

        BuildPlan plan = new Dnd5eBuildPlanner(build, catalogue).plan();

        assertThat(choice(plan, "species:changeling:ability:0").prompt()).contains("+2, +1");
        assertThat(choice(plan, "species:changeling:ability:0").isPending()).isFalse();
        assertThat(plan.problems()).isEmpty();
    }

    // ---- fixtures -----------------------------------------------------------------

    private static BuildPlan plan(Dnd5eCharacterBuild build) {
        return new Dnd5eBuildPlanner(build, catalogue()).plan();
    }

    private static Dnd5eCharacterBuild build(String subspecies, String classSlug, String subclassSlug, int level, List<Dnd5eBuildChoice> answers) {
        return new Dnd5eCharacterBuild(
                "dwarf", subspecies, null, "soldier", List.of(new Dnd5eBuildClass(classSlug, subclassSlug, level)),
                Dnd5eAbilityScoreMethod.STANDARD_ARRAY,
                Map.of("strength", 15, "dexterity", 13, "constitution", 14, "intelligence", 8, "wisdom", 12, "charisma", 10),
                Dnd5eHitPointMethod.FIXED, List.of(), answers);
    }

    private static Dnd5eBuildChoice answer(String id, String... selections) {
        return new Dnd5eBuildChoice(id, List.of(selections));
    }

    private static List<String> ids(BuildPlan plan) {
        return plan.choices().stream().map(CreationChoice::id).toList();
    }

    private static CreationChoice choice(BuildPlan plan, String id) {
        return plan.choices().stream().filter(choice -> choice.id().equals(id)).findFirst()
                .orElseThrow(() -> new AssertionError("No choice " + id + " in " + ids(plan)));
    }

    private static CatalogueLookup catalogue() {
        Map<String, Map<String, CatalogueRecord>> byKind = new HashMap<>();
        add(byKind, "SPECIES", "dwarf", "Dwarf", """
                {"size":["medium"],"speed":{"walk":25},"abilityAlternatives":[{"fixed":{"constitution":2},"choices":[],"maximum":null}],
                 "toolAlternatives":[{"fixed":[],"choices":[{"from":["smiths-tools","brewers-supplies"],"category":null,"fromFilter":null,"count":1,"amount":null}]}],
                 "subspecies":[{"name":"Mountain","sourceBook":"PHB","overwrite":[],"abilityAlternatives":[{"fixed":{"strength":2},"choices":[],"maximum":null}],
                   "armorAlternatives":[{"fixed":["light","medium"],"choices":[]}],"variants":[]}],
                 "variants":[]}""");
        add(byKind, "SPECIES", "changeling", "Changeling", """
                {"size":["medium"],"abilityAlternatives":[
                   {"fixed":{},"choices":[{"from":["strength","dexterity","constitution","intelligence","wisdom","charisma"],"weights":[2,1],"count":2,"amount":null}],"maximum":null},
                   {"fixed":{},"choices":[{"from":["strength","dexterity","constitution","intelligence","wisdom","charisma"],"weights":[1,1,1],"count":3,"amount":null}],"maximum":null}],
                 "subspecies":[],"variants":[]}""");
        add(byKind, "BACKGROUND", "soldier", "Soldier", """
                {"skillAlternatives":[{"fixed":["athletics","intimidation"],"choices":[]}],"toolAlternatives":[{"fixed":["thieves-tools"],"choices":[]}],
                 "startingEquipment":[]}""");
        add(byKind, "CLASS", "fighter", "Fighter", """
                {"savingThrows":["strength","constitution"],"subclassTitle":"Martial Archetype","subclassLevel":3,"abilityScoreImprovementLevels":[4],
                 "proficiencies":{"armor":["light"],"weaponCategories":["simple"],"weaponItems":[],
                   "skillAlternatives":[{"fixed":[],"choices":[{"from":["athletics","perception"],"category":null,"fromFilter":null,"count":1,"amount":null}]}],
                   "toolAlternatives":[]},
                 "startingEquipment":{"goldAlternative":null,"groups":[]},
                 "multiclassing":{"allOf":{"strength":13},"anyOf":{},"proficienciesGained":{}},
                 "optionalFeatureProgressions":[{"name":"Fighting Style","featureTypes":["FS:F"],"countByLevel":[1,1,1,1,1,1,1]}],
                 "features":[{"name":"Martial Archetype","level":3,"optional":false,"grantsSubclassFeature":true,"choices":[]}]}""");
        add(byKind, "CLASS", "rogue", "Rogue", """
                {"savingThrows":["dexterity","intelligence"],"subclassTitle":"Roguish Archetype","subclassLevel":3,"abilityScoreImprovementLevels":[4],
                 "proficiencies":{"armor":["light"],"weaponCategories":["simple"],"weaponItems":[],
                   "skillAlternatives":[{"fixed":[],"choices":[{"from":["stealth","perception"],"category":null,"fromFilter":null,"count":1,"amount":null}]}],
                   "toolAlternatives":[{"fixed":["thieves-tools"],"choices":[]}]},
                 "startingEquipment":{"goldAlternative":null,"groups":[]},
                 "multiclassing":{"allOf":{"dexterity":13},"anyOf":{},"proficienciesGained":{}},
                 "optionalFeatureProgressions":[],
                 "features":[{"name":"Expertise","level":1,"optional":false,"grantsSubclassFeature":false,
                   "choices":[{"type":"EXPERTISE","count":2,"from":["proficientSkills","thieves-tools"]}]}]}""");
        add(byKind, "SUBCLASS", "fighter-battle-master", "Battle Master", """
                {"classSlug":"fighter","features":[],
                 "optionalFeatureProgressions":[{"name":"Maneuvers","featureTypes":["MV:B"],"countByLevel":[0,0,3,3,3,3,5]}]}""");
        add(byKind, "OPTIONAL_FEATURE", "defense", "Defense", """
                {"featureTypes":["FS:F"],"optional":false,"prerequisites":null}""");
        add(byKind, "OPTIONAL_FEATURE", "blind-fighting", "Blind Fighting", """
                {"featureTypes":["FS:F"],"optional":true,"prerequisites":null}""");
        for (String maneuver : List.of("riposte", "parry", "trip-attack", "feint-attack", "rally")) {
            add(byKind, "OPTIONAL_FEATURE", maneuver, maneuver, """
                    {"featureTypes":["MV:B"],"optional":false,"prerequisites":null}""");
        }
        add(byKind, "FEAT", "alert", "Alert", """
                {"prerequisites":null,"repeatable":false}""");
        return new CatalogueLookup() {
            @Override
            public Optional<CatalogueRecord> find(String kind, String slug) {
                return Optional.ofNullable(byKind.getOrDefault(kind, Map.of()).get(slug));
            }

            @Override
            public List<CatalogueRecord> list(String kind) {
                return new ArrayList<>(byKind.getOrDefault(kind, Map.of()).values());
            }
        };
    }

    private static void add(Map<String, Map<String, CatalogueRecord>> byKind, String kind, String slug, String name, String data) {
        byKind.computeIfAbsent(kind, key -> new LinkedHashMap<>())
                .put(slug, new CatalogueRecord(kind, slug, name, "PHB", 1, "", MAPPER.readTree(data)));
    }
}

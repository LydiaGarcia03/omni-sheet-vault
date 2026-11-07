package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.MAPPER;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.json;
import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.valid;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationPreview;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.SelectionDetail;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

class DaggerheartCharacterCreationFlowTest {

    private final DaggerheartCharacterCreationFlow flow = new DaggerheartCharacterCreationFlow(MAPPER);
    private final DaggerheartTestCatalogue catalogue = new DaggerheartTestCatalogue();

    @Test
    void anEmptyDraftHasEveryStepPendingAndNoProblems() {
        BuildPlan plan = flow.plan(flow.emptyDraft(), catalogue);

        assertThat(plan.pending()).extracting(CreationChoice::id).containsExactly(
                "class", "subclass", "ancestry", "community", "traits", "primary-weapon", "armor", "potion", "class-item",
                "experiences", "domain-cards");
        assertThat(plan.problems()).isEmpty();
    }

    @Test
    void offersOptionsInAlphabeticalOrder() {
        BuildPlan plan = flow.plan(flow.emptyDraft(), catalogue);

        assertThat(plan.choices().getFirst().options()).extracting(option -> option.label()).containsExactly("Brawler", "Ranger", "Wizard");
    }

    @Test
    void aCompleteBuildHasNothingPendingAndNoProblems() {
        BuildPlan plan = flow.plan(DaggerheartTestCatalogue.VALID, catalogue);

        assertThat(plan.pending()).isEmpty();
        assertThat(plan.problems()).isEmpty();
    }

    @Test
    void materializesALevelOneSheet() {
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, DaggerheartTestCatalogue.VALID, catalogue).sheetJson());

        assertThat(sheet.path("level").asInt()).isEqualTo(1);
        assertThat(sheet.path("identity").path("class").path("name").asString()).isEqualTo("Ranger");
        assertThat(sheet.path("identity").path("subclass").path("spellcastTrait").asString()).isEqualTo("Agility");
        assertThat(sheet.path("identity").path("domains")).hasSize(2);
        assertThat(sheet.path("features")).extracting(feature -> feature.path("name").asString())
                .containsExactly("Hold Them Off", "Ranger’s Focus", "Ruthless Predator", "Natural Climber", "Nimble", "Scoundrel");
        assertThat(sheet.path("experiences").get(0).path("bonus").asInt()).isEqualTo(2);
        assertThat(sheet.path("domainCards")).extracting(card -> card.path("name").asString()).containsExactly("Bone Card", "Sage Card");
        assertThat(sheet.path("weapons").path("primary").path("name").asString()).isEqualTo("Shortbow");
        assertThat(sheet.path("inventory")).extracting(item -> item.path("name").asString()).containsExactly(
                "Torch", "50 feet of rope", "Basic supplies", "Minor Health Potion", "A trophy from your first kill");
        assertThat(sheet.path("gold").path("handfuls").asInt()).isEqualTo(1);
        assertThat(sheet.path("trackers").path("hope").asInt()).isEqualTo(2);
        assertThat(sheet.path("build").path("class").asString()).isEqualTo("ranger");
        assertThat(flow.buildOf(json(sheet))).isPresent();
    }

    @Test
    void doesNotMaterializeAnUnfinishedBuild() {
        MaterializedSheet materialized = flow.materialize(null, flow.emptyDraft(), catalogue);

        assertThat(materialized.isMaterialized()).isFalse();
        assertThat(materialized.plan().pending()).isNotEmpty();
    }

    @Test
    void traitsUseTheStartingModifiersOnceEach() {
        ObjectNode build = valid();
        ((ObjectNode) build.path("traits")).put("knowledge", 2);

        assertThat(flow.plan(json(build), catalogue).problems()).containsExactly(
                "Traits use the modifiers +2, +1, +1, +0, +0 and −1 once each; Knowledge can't be +2.");
    }

    @Test
    void theSubclassMustBelongToTheClass() {
        ObjectNode build = valid().put("subclass", "school-of-knowledge");

        BuildPlan plan = flow.plan(json(build), catalogue);

        assertThat(plan.problems()).containsExactly("School of Knowledge is not a Ranger subclass.");
        assertThat(plan.pending()).extracting(CreationChoice::id).contains("subclass");
    }

    @Test
    void aTwoHandedPrimaryLeavesNoHandForASecondary() {
        ObjectNode build = valid().put("secondaryWeapon", "round-shield");

        assertThat(flow.plan(json(build), catalogue).problems()).containsExactly(
                "Shortbow is two-handed, so it leaves no hand for a secondary weapon.");
    }

    @Test
    void aOneHandedPrimaryAsksForASecondary() {
        ObjectNode build = valid().put("primaryWeapon", "broadsword");

        assertThat(flow.plan(json(build), catalogue).pending()).extracting(CreationChoice::id).containsExactly("secondary-weapon");
        assertThat(flow.plan(json(build.put("secondaryWeapon", "round-shield")), catalogue).pending()).isEmpty();
    }

    @Test
    void startingWeaponsAndArmorAreTierOne() {
        ObjectNode build = valid().put("primaryWeapon", "improved-broadsword").put("armor", "improved-leather-armor");

        assertThat(flow.plan(json(build), catalogue).problems()).containsExactly(
                "Improved Broadsword is not a Tier 1 weapon.", "Improved Leather Armor is not Tier 1 armor.");
    }

    @Test
    void magicWeaponsNeedASpellcaster() {
        ObjectNode brawler = valid().put("class", "brawler").put("subclass", "juggernaut").put("primaryWeapon", "wand")
                .put("classItem", "Hand wraps");
        ((tools.jackson.databind.node.ArrayNode) brawler.putArray("domainCards")).add("bone-card");

        assertThat(flow.plan(json(brawler), catalogue).problems())
                .contains("Wand is a magic weapon, which needs a subclass with a Spellcast trait.");
    }

    @Test
    void theBrawlerMayGoUnarmed() {
        ObjectNode build = valid().put("class", "brawler").put("subclass", "juggernaut").put("primaryWeapon", "").put("classItem", "A book");

        BuildPlan plan = flow.plan(json(build), catalogue);

        assertThat(plan.pending()).extracting(CreationChoice::id).doesNotContain("primary-weapon");
        assertThat(plan.problems()).contains("Sage Card is not from the Brawler's domains.");
    }

    @Test
    void domainCardsAreLevelOneFromTheClassDomains() {
        ObjectNode build = valid();
        build.putArray("domainCards").add("codex-card").add("sage-two");

        BuildPlan plan = flow.plan(json(build), catalogue);

        assertThat(plan.problems()).containsExactly(
                "Codex Card is not from the Ranger's domains.", "Sage Two is a level 2 card; a new character takes level 1 cards.");
        assertThat(plan.pending()).extracting(CreationChoice::id).containsExactly("domain-cards");
    }

    @Test
    void theClassItemIsOneOfTheClasssTwo() {
        ObjectNode build = valid().put("classItem", "A lucky coin");

        assertThat(flow.plan(json(build), catalogue).problems()).containsExactly("\"A lucky coin\" is not one of the Ranger's class items.");
    }

    @Test
    void aMixedAncestryTakesTheTopFeatureOfOneAndTheBottomOfAnother() {
        ObjectNode build = valid().put("ancestry", "");
        build.putObject("mixedAncestry").put("name", "Clank-Simiah").put("top", "clank").put("bottom", "simiah");

        BuildPlan plan = flow.plan(json(build), catalogue);
        assertThat(plan.pending()).extracting(CreationChoice::id).containsExactly("boosted-experience");

        build.put("boostedExperience", 1);
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), catalogue).sheetJson());
        assertThat(sheet.path("identity").path("ancestry").path("name").asString()).isEqualTo("Clank-Simiah");
        assertThat(sheet.path("features")).extracting(feature -> feature.path("name").asString()).contains("Purposeful Design", "Nimble")
                .doesNotContain("Efficient", "Natural Climber");
        assertThat(sheet.path("experiences").get(1).path("bonus").asInt()).isEqualTo(3);
    }

    @Test
    void aMixedAncestryNeedsTwoDifferentAncestries() {
        ObjectNode build = valid();
        build.putObject("mixedAncestry").put("top", "simiah").put("bottom", "simiah");

        assertThat(flow.plan(json(build), catalogue).problems()).containsExactly(
                "A mixed ancestry takes its two features from two different ancestries.");
    }

    @Test
    void aTransformationIsOfferedOnlyWhenTheGmAllowsIt() {
        ObjectNode build = valid().put("transformation", "vampire");
        assertThat(flow.plan(json(build), catalogue).choices()).extracting(CreationChoice::id).doesNotContain("transformation");

        build.put("transformationsAllowed", true);
        BuildPlan plan = flow.plan(json(build), catalogue);
        assertThat(plan.choices()).filteredOn(choice -> choice.id().equals("transformation")).singleElement()
                .satisfies(choice -> assertThat(choice.optional()).isTrue());
        JsonNode sheet = MAPPER.readTree(flow.materialize(null, json(build), catalogue).sheetJson());
        assertThat(sheet.path("identity").path("transformation").path("name").asString()).isEqualTo("Vampire");
    }

    @Test
    void theTwoExperiencesMustDiffer() {
        ObjectNode build = valid();
        build.putArray("experiences").add("Tracker").add("tracker");

        assertThat(flow.plan(json(build), catalogue).problems()).containsExactly("The two Experiences must be different.");
    }

    @Test
    void previewsTheDraftAsSelectionCards() {
        CreationPreview preview = flow.preview(DaggerheartTestCatalogue.VALID, catalogue);

        SelectionDetail summary = preview.selections().get("summary");
        assertThat(summary.facts()).extracting(SelectionDetail.Entry::label, SelectionDetail.Entry::text).contains(
                org.assertj.core.groups.Tuple.tuple("Evasion", "13"),
                org.assertj.core.groups.Tuple.tuple("Damage thresholds", "7 / 14"),
                org.assertj.core.groups.Tuple.tuple("Agility", "+2"),
                org.assertj.core.groups.Tuple.tuple("Shortbow", "Agility +2 · Far · 1d6+3 physical"));
        assertThat(preview.selections().get("class").grants()).extracting(SelectionDetail.Entry::label)
                .containsExactly("Hold Them Off", "Ranger’s Focus");
        assertThat(preview.sheetJson()).isNull();
    }

    @Test
    void aRedactedCataloguePreviewsWithoutProse() {
        CreationPreview preview = flow.preview(DaggerheartTestCatalogue.VALID, new DaggerheartTestCatalogue().redacted());

        assertThat(preview.selections().get("ancestry").grants()).isNotEmpty().allSatisfy(grant -> assertThat(grant.text()).isNull());
    }

    @Test
    void anEmptyDraftPreviewsOnlyTheSummary() {
        assertThat(flow.preview(flow.emptyDraft(), catalogue).selections()).containsOnlyKeys("summary");
    }

    @Test
    void rejectsABuildThatIsNotAnObject() {
        assertThatThrownBy(() -> flow.plan("[]", catalogue)).isInstanceOf(InvalidBuildException.class);
        assertThatThrownBy(() -> flow.plan("{", catalogue)).isInstanceOf(InvalidBuildException.class);
        assertThat(Optional.of(flow.systemId())).contains("daggerheart");
    }
}

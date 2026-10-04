package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationChoiceOption;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class Dnd5eSpellPlannerTest {

    @Test
    void cantripsAndKnownSpellsArePooledUpToTheClassLevelAndNameTheLevelsThatGrantThem() {
        BuildPlan plan = plan("human", warlock(2), List.of());

        CreationChoice cantrips = choice(plan, "class:warlock:cantrips");
        CreationChoice spells = choice(plan, "class:warlock:spells");
        assertThat(cantrips.count()).isEqualTo(1);
        assertThat(cantrips.sourceLabel()).isEqualTo("Warlock 1");
        assertThat(spells.count()).isEqualTo(2);
        assertThat(spells.sourceLabel()).isEqualTo("Warlock 1, 2");
        assertThat(plan.choices()).extracting(CreationChoice::id)
                .doesNotContain("class:warlock:1:cantrips", "class:warlock:1:spells", "class:warlock:2:spells");
    }

    @Test
    void spellbookAdditionsArePooledIntoOneChoice() {
        BuildPlan plan = plan("human", List.of(new Dnd5eBuildClass("wizard", "wizard-evocation", 3)), List.of());

        CreationChoice spellbook = choice(plan, "class:wizard:spellbook");
        assertThat(spellbook.count()).isEqualTo(5);
        assertThat(spellbook.sourceLabel()).isEqualTo("Wizard 1–3");
    }

    @Test
    void aPactCasterIsLimitedToItsSlotLevelAndGetsItsPatronsExpandedList() {
        BuildPlan plan = plan("human", warlock(2), List.of());

        assertThat(keys(choice(plan, "class:warlock:spells"))).containsExactlyInAnyOrder("hex", "burning-hands");
    }

    @Test
    void aSpellPrerequisiteSeesThePooledSpellsEvenThoughTheyAreOfferedAfterTheLevels() {
        BuildPlan without = plan("human", warlock(2), List.of(answer("class:warlock:cantrips", "light")));
        BuildPlan with = plan("human", warlock(2), List.of(answer("class:warlock:cantrips", "eldritch-blast")));

        assertThat(keys(choice(without, "class:warlock:2:eldritch-invocations"))).containsExactly("devils-sight");
        assertThat(keys(choice(with, "class:warlock:2:eldritch-invocations"))).containsExactlyInAnyOrder("devils-sight", "agonizing-blast");
        assertThat(choice(with, "class:warlock:cantrips").selected()).containsExactly("eldritch-blast");
        assertThat(with.problems()).isEmpty();
    }

    @Test
    void theGrantingLevelsCollapseRunsOfThreeOrMore() {
        assertThat(Dnd5eSpellPlanner.gainSource("Bard", List.of(1, 4, 10))).isEqualTo("Bard 1, 4, 10");
        assertThat(Dnd5eSpellPlanner.gainSource("Bard", List.of(1, 2, 5))).isEqualTo("Bard 1, 2, 5");
        assertThat(Dnd5eSpellPlanner.gainSource("Wizard", List.of(1, 2, 3, 4, 5, 7))).isEqualTo("Wizard 1–5, 7");
    }

    @Test
    void aSpeciesSpellChoiceOffersItsFilteredList() {
        BuildPlan plan = plan("elf", warlock(1), List.of());

        assertThat(keys(choice(plan, "species:elf:known:1:0"))).containsExactlyInAnyOrder("fire-bolt", "light");
    }

    @Test
    void aSpellAlreadyKnownIsNotOfferedAgain() {
        BuildPlan plan = plan("elf", warlock(1), List.of(answer("species:elf:known:1:0", "light")));

        assertThat(keys(choice(plan, "class:warlock:cantrips"))).containsExactly("eldritch-blast");
    }

    @Test
    void aSubclassAddsItsOwnSpellListToItsClass() {
        BuildPlan chronurgist = plan("human", List.of(new Dnd5eBuildClass("wizard", "wizard-chronurgy", 3)), List.of());
        BuildPlan evoker = plan("human", List.of(new Dnd5eBuildClass("wizard", "wizard-evocation", 3)), List.of());

        assertThat(keys(choice(chronurgist, "class:wizard:spellbook"))).contains("magnify-gravity");
        assertThat(keys(choice(evoker, "class:wizard:spellbook"))).doesNotContain("magnify-gravity");
    }

    // ---- fixtures -----------------------------------------------------------------

    private static List<Dnd5eBuildClass> warlock(int level) {
        return List.of(new Dnd5eBuildClass("warlock", "warlock-fiend", level));
    }

    private static BuildPlan plan(String species, List<Dnd5eBuildClass> classes, List<Dnd5eBuildChoice> answers) {
        Dnd5eCharacterBuild build = new Dnd5eCharacterBuild(species, null, null, "acolyte", classes, Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 8, "dexterity", 14, "constitution", 14, "intelligence", 12, "wisdom", 10, "charisma", 15),
                Dnd5eHitPointMethod.FIXED, List.of(), answers);
        return new Dnd5eBuildPlanner(build, Dnd5eBuildFixtures.catalogue()).plan();
    }

    private static Dnd5eBuildChoice answer(String id, String... selections) {
        return new Dnd5eBuildChoice(id, List.of(selections));
    }

    private static List<String> keys(CreationChoice choice) {
        return choice.options().stream().map(CreationChoiceOption::key).toList();
    }

    private static CreationChoice choice(BuildPlan plan, String id) {
        return plan.choices().stream().filter(choice -> choice.id().equals(id)).findFirst()
                .orElseThrow(() -> new AssertionError("No choice " + id + " in " + plan.choices().stream().map(CreationChoice::id).toList()));
    }
}

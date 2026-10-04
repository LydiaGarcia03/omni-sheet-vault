package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.LevelUp;
import dev.omnisheetvault.api.ruleset.LevelUpNotAllowedException;
import jakarta.validation.Validation;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Adding a level to a finished build: the class level it reaches and the choices that level asks for (PHB 2014). */
class Dnd5eLevelUpTest {

    private static final ObjectMapper MAPPER = Dnd5eBuildFixtures.MAPPER;
    private static final CatalogueLookup CATALOGUE = Dnd5eBuildFixtures.catalogue();

    private final Dnd5eCharacterCreationFlow flow =
            new Dnd5eCharacterCreationFlow(MAPPER, Validation.buildDefaultValidatorFactory().getValidator());

    @Test
    void levelsAClassTheCharacterHas() {
        LevelUp levelUp = flow.withLevelUp(json(fighter(1, null, 13)), "fighter", CATALOGUE);

        assertThat(levelUp.classLevel()).isEqualTo(2);
        assertThat(read(levelUp).classes()).containsExactly(new Dnd5eBuildClass("fighter", null, 2));
    }

    @Test
    void theSubclassLevelAsksForTheSubclass() {
        BuildPlan plan = plan(flow.withLevelUp(json(fighter(2, null, 13)), "fighter", CATALOGUE));

        assertThat(placedAt(plan, "fighter", 3)).anySatisfy(choice -> {
            assertThat(choice.type()).isEqualTo("SUBCLASS");
            assertThat(choice.isPending()).isTrue();
        });
    }

    @Test
    void anAbilityScoreImprovementLevelAsksForAnAsiOrAFeat() {
        BuildPlan plan = plan(flow.withLevelUp(json(fighter(3, "fighter-eldritch-knight", 13)), "fighter", CATALOGUE));

        assertThat(placedAt(plan, "fighter", 4)).anySatisfy(choice -> {
            assertThat(choice.type()).isEqualTo("ASI_OR_FEAT");
            assertThat(choice.isPending()).isTrue();
        });
    }

    @Test
    void aSpellcasterLevelAsksForItsNewSpells() {
        Dnd5eCharacterBuild wizard = build(List.of(new Dnd5eBuildClass("wizard", null, 1)), 13, List.of(
                answer("class:wizard:cantrips", "light"),
                answer("class:wizard:spellbook", "magic-missile", "sleep", "burning-hands", "misty-step", "detect-magic"),
                answer("class:wizard:prepared", "magic-missile")));

        BuildPlan plan = plan(flow.withLevelUp(json(wizard), "wizard", CATALOGUE));

        assertThat(plan.pending()).extracting(CreationChoice::id).contains("class:wizard:prepared", "build.class.wizard.subclass");
    }

    @Test
    void spellsSavedUnderPerLevelIdsCarryIntoTheLevelUp() {
        String legacyWizard = json(build(List.of(new Dnd5eBuildClass("wizard", null, 1)), 13, List.of()))
                .replace("\"choices\":[]", """
                        "choices":[{"id":"class:wizard:1:cantrips","selections":["light"]},\
                        {"id":"class:wizard:1:spellbook","selections":["magic-missile","sleep"]}]""");

        BuildPlan plan = plan(flow.withLevelUp(legacyWizard, "wizard", CATALOGUE));

        assertThat(plan.problems()).noneMatch(problem -> problem.contains("doesn't offer"));
        assertThat(plan.choices()).filteredOn(choice -> choice.id().equals("class:wizard:cantrips"))
                .singleElement().satisfies(choice -> assertThat(choice.selected()).contains("light"));
    }

    @Test
    void rolledHitPointsKeepTheirLevelsAndTheNewLevelWaitsForItsRoll() {
        Dnd5eCharacterBuild rolled = rolledMulticlass();

        assertThat(read(flow.withLevelUp(json(rolled), "wizard", CATALOGUE)).rolledHitPoints()).containsExactly(3, 5, null, 7, 2, 9);
        assertThat(read(flow.withLevelUp(json(rolled), "fighter", CATALOGUE)).rolledHitPoints()).containsExactly(3, 5, 7, 2, 9, null);
        assertThat(plan(flow.withLevelUp(json(rolled), "wizard", CATALOGUE)).pending())
                .extracting(CreationChoice::id).contains("build.rolledHitPoints");
    }

    @Test
    void fixedHitPointsAskNothingForTheNewLevel() {
        BuildPlan plan = plan(flow.withLevelUp(json(fighter(1, null, 13)), "fighter", CATALOGUE));

        assertThat(plan.choices()).extracting(CreationChoice::type).doesNotContain("ROLLED_HIT_POINTS");
    }

    @Test
    void anotherClassStartsAtItsFirstLevel() {
        LevelUp levelUp = flow.withLevelUp(json(fighter(3, "fighter-eldritch-knight", 13)), "wizard", CATALOGUE);

        assertThat(levelUp.classLevel()).isEqualTo(1);
        assertThat(read(levelUp).classes()).extracting(Dnd5eBuildClass::classSlug).containsExactly("fighter", "wizard");
        assertThat(plan(levelUp).problems()).isEmpty();
    }

    @Test
    void multiclassingChecksThePrerequisites() {
        LevelUp levelUp = flow.withLevelUp(json(fighter(3, "fighter-eldritch-knight", 12)), "wizard", CATALOGUE);

        assertThat(plan(levelUp).problems()).anyMatch(problem -> problem.contains("Wizard multiclass requires intelligence 13"));
    }

    @Test
    void noLevelUpPastTwenty() {
        assertThatThrownBy(() -> flow.withLevelUp(json(fighter(20, "fighter-eldritch-knight", 13)), "fighter", CATALOGUE))
                .isInstanceOf(LevelUpNotAllowedException.class);
    }

    @Test
    void anUnknownClassIsRefused() {
        assertThatThrownBy(() -> flow.withLevelUp(json(fighter(1, null, 13)), "artificer-of-doom", CATALOGUE))
                .isInstanceOf(LevelUpNotAllowedException.class);
    }

    @Test
    void theBuildIsReadFromTheSheet() {
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 1, 10, 30, java.util.Set.of(), java.util.Set.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), java.util.Set.of(), 0, 10, 0, 0, false, List.of(),
                List.of(), List.of(), List.of(), List.of(), 0, 0, 0, 0, 0, List.of(),
                new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", ""),
                List.of(), 0, List.of(), List.of(), List.of(), false, fighter(1, null, 13), null, null, null, null,
                Dnd5eSheet.SCHEMA_VERSION);

        assertThat(flow.buildOf(MAPPER.writeValueAsString(sheet))).isPresent();
    }

    private static List<CreationChoice> placedAt(BuildPlan plan, String classSlug, int level) {
        return plan.choices().stream()
                .filter(choice -> choice.placement() != null && classSlug.equals(choice.placement().group())
                        && Integer.valueOf(level).equals(choice.placement().level()))
                .toList();
    }

    private BuildPlan plan(LevelUp levelUp) {
        return flow.plan(levelUp.buildJson(), CATALOGUE);
    }

    private static Dnd5eCharacterBuild read(LevelUp levelUp) {
        return MAPPER.readValue(levelUp.buildJson(), Dnd5eCharacterBuild.class);
    }

    private static Dnd5eCharacterBuild fighter(int level, String subclass, int intelligence) {
        return build(List.of(new Dnd5eBuildClass("fighter", subclass, level)), intelligence, List.of(
                answer("class:fighter:skills:0", "athletics"),
                answer("class:fighter:cantrips", "fire-bolt"),
                answer("class:fighter:spells", "shield")));
    }

    private static Dnd5eCharacterBuild build(List<Dnd5eBuildClass> classes, int intelligence, List<Dnd5eBuildChoice> answers) {
        return new Dnd5eCharacterBuild("human", null, null, "acolyte", classes, Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 15, "dexterity", 12, "constitution", 14, "intelligence", intelligence, "wisdom", 10, "charisma", 8),
                Dnd5eHitPointMethod.FIXED, List.of(), answers);
    }

    /** Wizard 3 then Fighter 3, rolled: Wizard 2–3 then Fighter 1–3. */
    private static Dnd5eCharacterBuild rolledMulticlass() {
        return new Dnd5eCharacterBuild("human", null, null, "acolyte",
                List.of(new Dnd5eBuildClass("wizard", null, 3), new Dnd5eBuildClass("fighter", null, 3)), Dnd5eAbilityScoreMethod.MANUAL,
                Map.of("strength", 15, "dexterity", 12, "constitution", 14, "intelligence", 13, "wisdom", 10, "charisma", 8),
                Dnd5eHitPointMethod.ROLLED, List.of(3, 5, 7, 2, 9), List.of());
    }

    private static Dnd5eBuildChoice answer(String id, String... selected) {
        return new Dnd5eBuildChoice(id, List.of(selected));
    }

    private static String json(Dnd5eCharacterBuild build) {
        return MAPPER.writeValueAsString(build);
    }
}

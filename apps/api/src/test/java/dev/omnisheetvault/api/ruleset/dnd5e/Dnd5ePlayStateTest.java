package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.DeathSaves;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

/** Play state on the D&D 5e sheet: death saves (PHB 2014, "Dropping to 0 Hit Points"), experience points and the sheet theme. */
class Dnd5ePlayStateTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Dnd5eSheetMutator mutator = new Dnd5eSheetMutator(objectMapper);
    private final Dnd5eSheetCalculator calculator = new Dnd5eSheetCalculator(objectMapper);

    @Test
    void droppingToZeroStartsFreshDeathSaves() {
        String dying = mutator.applyDamage(sheetJson(5, new Dnd5eDeathSaves(2, 2)), 5);

        DeathSaves saves = calculator.calculateVitals(dying).deathSaves();

        assertThat(saves).isEqualTo(new DeathSaves(0, 0, true, false, false));
    }

    @Test
    void aboveZeroTheCharacterIsNotDying() {
        assertThat(calculator.calculateVitals(sheetJson(5, null)).deathSaves()).isEqualTo(DeathSaves.NOT_DYING);
    }

    @Test
    void damageAtZeroIsOneFailureAndACriticalIsTwo() {
        String dying = sheetJson(0, Dnd5eDeathSaves.NONE);

        assertThat(read(mutator.applyDamage(dying, 3)).deathSaves()).isEqualTo(new Dnd5eDeathSaves(0, 1));
        assertThat(read(mutator.applyDamage(dying, 3, true)).deathSaves()).isEqualTo(new Dnd5eDeathSaves(0, 2));
    }

    @Test
    void damageAtLeastTheMaximumOverZeroKillsOutright() {
        String sheet = sheetJson(10, null);
        int maximum = Dnd5eFormulas.maxHitPoints(read(sheet));

        DeathSaves saves = calculator.calculateVitals(mutator.applyDamage(sheet, 10 + maximum)).deathSaves();

        assertThat(saves.dead()).isTrue();
    }

    @Test
    void aStableCharacterWhoTakesDamageStartsDyingAgain() {
        String stable = sheetJson(0, new Dnd5eDeathSaves(3, 1));

        assertThat(read(mutator.applyDamage(stable, 2)).deathSaves()).isEqualTo(new Dnd5eDeathSaves(0, 1));
    }

    @Test
    void tenOrHigherSucceedsAndLowerFails() {
        String dying = sheetJson(0, new Dnd5eDeathSaves(1, 1));

        assertThat(read(mutator.applyDeathSaveRoll(dying, 10)).deathSaves()).isEqualTo(new Dnd5eDeathSaves(2, 1));
        assertThat(read(mutator.applyDeathSaveRoll(dying, 9)).deathSaves()).isEqualTo(new Dnd5eDeathSaves(1, 2));
    }

    @Test
    void aNaturalOneIsTwoFailures() {
        String dying = sheetJson(0, new Dnd5eDeathSaves(0, 1));

        DeathSaves saves = calculator.calculateVitals(mutator.applyDeathSaveRoll(dying, 1)).deathSaves();

        assertThat(saves.failures()).isEqualTo(3);
        assertThat(saves.dead()).isTrue();
    }

    @Test
    void aNaturalTwentyBringsTheCharacterBackWithOneHitPoint() {
        Dnd5eSheet result = read(mutator.applyDeathSaveRoll(sheetJson(0, new Dnd5eDeathSaves(1, 2)), 20));

        assertThat(result.currentHitPoints()).isEqualTo(1);
        assertThat(result.deathSavesOrDefault()).isEqualTo(Dnd5eDeathSaves.NONE);
    }

    @Test
    void threeSuccessesStabilize() {
        DeathSaves saves = calculator.calculateVitals(
                mutator.applyDeathSaveRoll(sheetJson(0, new Dnd5eDeathSaves(2, 0)), 15)).deathSaves();

        assertThat(saves.stable()).isTrue();
        assertThat(saves.dying()).isTrue();
    }

    @Test
    void anyHealingClearsTheCounts() {
        Dnd5eSheet result = read(mutator.applyHealing(sheetJson(0, new Dnd5eDeathSaves(1, 2)), 1));

        assertThat(result.currentHitPoints()).isEqualTo(1);
        assertThat(result.deathSavesOrDefault()).isEqualTo(Dnd5eDeathSaves.NONE);
    }

    @Test
    void countsSetByHandOnlyApplyAtZero() {
        assertThat(read(mutator.setDeathSaves(sheetJson(0, null), 2, 1)).deathSaves()).isEqualTo(new Dnd5eDeathSaves(2, 1));
        assertThat(read(mutator.setDeathSaves(sheetJson(4, null), 2, 1)).deathSaves()).isNull();
    }

    @Test
    void experiencePointsAreShownAgainstTheCurrentLevelsBand() {
        String sheet = mutator.setExperiencePoints(sheetJson(10, null), 7_000);

        var experience = calculator.calculateVitals(sheet).experience();

        assertThat(experience.points()).isEqualTo(7_000);
        assertThat(experience.levelFromPoints()).isEqualTo(5);
        assertThat(experience.currentLevelAt()).isEqualTo(6_500);
        assertThat(experience.nextLevelAt()).isEqualTo(14_000);
        assertThat(experience.advancement()).isEqualTo("MILESTONE");
        assertThat(experience.canLevelUp()).isFalse();
    }

    @Test
    void experiencePointsNeverGoBelowWhereTheLevelStarts() {
        assertThat(read(mutator.setExperiencePoints(sheetJson(10, null), -40)).experiencePointsOrDefault()).isEqualTo(6_500);
        assertThat(read(mutator.setExperiencePoints(sheetJson(10, null), 300)).experiencePoints()).isEqualTo(6_500);
    }

    @Test
    void aLevelFiveSheetWithoutExperiencePointsReadsAsTheLevelsStart() {
        assertThat(read(sheetJson(10, null)).experiencePointsOrDefault()).isEqualTo(6_500);
        assertThat(calculator.calculateVitals(sheetJson(10, null)).experience().levelFromPoints()).isEqualTo(5);
    }

    @Test
    void aSheetWithoutAThemeIsDrawnInDdbRed() {
        assertThat(calculator.calculateVitals(sheetJson(10, null)).sheetTheme()).isEqualTo("ddb-red");
    }

    @Test
    void theThemeCanBeAnyOfDndBeyondsSheetThemes() {
        String sheet = mutator.setSheetTheme(sheetJson(10, null), "cleric-silver");

        assertThat(calculator.calculateVitals(sheet).sheetTheme()).isEqualTo("cleric-silver");
    }

    @Test
    void anUnknownThemeIsRefused() {
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> mutator.setSheetTheme(sheetJson(10, null), "neon-pink"))
                .isInstanceOf(dev.omnisheetvault.api.ruleset.InvalidSheetThemeException.class);
    }

    @Test
    void reMaterializingKeepsPlayState() {
        Dnd5eSheet previous = read(sheetJson(0, new Dnd5eDeathSaves(1, 1))).withExperiencePoints(9_000)
                .withAppearance(new Dnd5eAppearance("cleric-silver"));

        Dnd5eSheet carried = read(sheetJson(0, null)).withPlayStateOf(previous);

        assertThat(carried.deathSaves()).isEqualTo(new Dnd5eDeathSaves(1, 1));
        assertThat(carried.experiencePointsOrDefault()).isEqualTo(9_000);
        assertThat(carried.appearanceOrDefault().theme()).isEqualTo("cleric-silver");
    }

    private String sheetJson(int currentHitPoints, Dnd5eDeathSaves deathSaves) {
        Dnd5eSheet sheet = new Dnd5eSheet(10, 10, 14, 10, 10, 10, 5, 10, 30,
                Set.of(), Set.of(), List.of(), List.of(), List.of(), List.of(),
                List.of(), List.of(), List.of(), List.of(), Set.of(), 0,
                currentHitPoints, 0, 0, false, List.of(), List.of(), List.of(), List.of(),
                List.of(), 0, 0, 0, 0, 0, List.of(), emptyBackground(), List.of(), 0, List.of(), List.of(), List.of(), false,
                null, null, null, null, null, Map.of(), List.of(), deathSaves, null, null, Dnd5eSheet.SCHEMA_VERSION);
        return objectMapper.writeValueAsString(sheet);
    }

    private Dnd5eSheet read(String sheetJson) {
        return objectMapper.readValue(sheetJson, Dnd5eSheet.class);
    }

    private static Dnd5eBackground emptyBackground() {
        return new Dnd5eBackground("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

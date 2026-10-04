package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.ChoicePlacement;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import java.util.List;
import org.junit.jupiter.api.Test;

class LevelUpPlanTest {

    private static final CharacterLevelUpService.StoredLevelUp FIGHTER_4 = new CharacterLevelUpService.StoredLevelUp("fighter", 4, null, null);

    @Test
    void keepsTheNewLevelsChoicesAndWhatTheyOpen() {
        BuildPlan plan = new BuildPlan(List.of(
                choice("class:fighter:1:style", null, "fighter", 1, true),
                choice("class:fighter:4:asi", null, "fighter", 4, false),
                choice("class:fighter:4:asi:feat", "class:fighter:4:asi", null, null, false)), List.of());

        assertThat(CharacterLevelUpService.forNewLevel(plan, FIGHTER_4).choices())
                .extracting(CreationChoice::id)
                .containsExactly("class:fighter:4:asi", "class:fighter:4:asi:feat");
    }

    @Test
    void keepsAnyPendingChoiceAndWhereItComesFrom() {
        BuildPlan plan = new BuildPlan(List.of(
                choice("class:wizard:spellbook", null, "wizard", null, false),
                choice("class:wizard:spellbook:swap", "class:wizard:spellbook", "wizard", null, false)), List.of());
        BuildPlan withPending = new BuildPlan(List.of(plan.choices().get(0), pending(plan.choices().get(1))), List.of());

        assertThat(CharacterLevelUpService.forNewLevel(withPending, FIGHTER_4).choices())
                .extracting(CreationChoice::id)
                .containsExactly("class:wizard:spellbook", "class:wizard:spellbook:swap");
    }

    @Test
    void keepsAChoiceTheNewLevelOpenedOnceItIsAnswered() {
        BuildPlan plan = new BuildPlan(List.of(
                choice("class:fighter:cantrips", null, "fighter", null, true),
                choice("build.rolledHitPoints", null, null, null, true),
                choice("class:fighter:skills", null, "fighter", null, true)), List.of());
        CharacterLevelUpService.StoredLevelUp stored = new CharacterLevelUpService.StoredLevelUp(
                "fighter", 4, null, java.util.Set.of("class:fighter:cantrips", "build.rolledHitPoints"));

        assertThat(CharacterLevelUpService.forNewLevel(plan, stored).choices())
                .extracting(CreationChoice::id)
                .containsExactly("class:fighter:cantrips", "build.rolledHitPoints");
    }

    private static CreationChoice choice(String id, String parent, String group, Integer level, boolean answered) {
        return new CreationChoice(id, "TEST", parent, id, "Fighter", 1, false, List.of(), answered ? List.of("x") : List.of("x"),
                group == null ? null : new ChoicePlacement(group, level));
    }

    private static CreationChoice pending(CreationChoice choice) {
        return new CreationChoice(choice.id(), choice.type(), choice.parentChoiceId(), choice.prompt(), choice.sourceLabel(),
                choice.count(), false, choice.options(), List.of(), choice.placement());
    }
}

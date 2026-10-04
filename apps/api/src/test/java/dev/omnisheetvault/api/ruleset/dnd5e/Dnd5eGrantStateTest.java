package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;

class Dnd5eGrantStateTest {

    @Test
    void twoIncreasesFromOneSourceShowAsOneContribution() {
        Dnd5eGrantState state = new Dnd5eGrantState(Map.of("strength", 15));

        state.increase("strength", 1, "Ability Score Improvement (Fighter 4)");
        state.increase("strength", 1, "Ability Score Improvement (Fighter 4)");
        state.increase("strength", 1, "Ability Score Improvement (Fighter 6)");

        assertThat(state.score("strength")).isEqualTo(18);
        assertThat(state.abilityContributions().get("strength")).containsExactly(
                new Dnd5eDerivation.SourcedAmount("Base", 15),
                new Dnd5eDerivation.SourcedAmount("Ability Score Improvement (Fighter 4)", 2),
                new Dnd5eDerivation.SourcedAmount("Ability Score Improvement (Fighter 6)", 1));
    }
}

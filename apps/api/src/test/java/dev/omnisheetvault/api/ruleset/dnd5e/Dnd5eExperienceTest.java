package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** PHB 2014, "Character Advancement" table. */
class Dnd5eExperienceTest {

    @ParameterizedTest
    @CsvSource({
            "0, 1", "299, 1", "300, 2", "899, 2", "900, 3", "2700, 4", "6500, 5", "14000, 6", "23000, 7",
            "34000, 8", "48000, 9", "64000, 10", "85000, 11", "100000, 12", "120000, 13", "140000, 14",
            "165000, 15", "195000, 16", "225000, 17", "264999, 17", "265000, 18", "305000, 19", "355000, 20",
            "999999, 20"})
    void levelForPointsFollowsThePhbTable(int points, int level) {
        assertThat(Dnd5eExperience.levelFor(points)).isEqualTo(level);
    }

    @Test
    void eachLevelStartsAtItsThreshold() {
        assertThat(Dnd5eExperience.threshold(1)).isZero();
        assertThat(Dnd5eExperience.threshold(18)).isEqualTo(265_000);
        assertThat(Dnd5eExperience.threshold(20)).isEqualTo(355_000);
    }

    @Test
    void aLevelUpIsAvailableOnceThePointsReachTheNextLevel() {
        assertThat(Dnd5eExperience.levelUpAvailable(299, 1)).isFalse();
        assertThat(Dnd5eExperience.levelUpAvailable(300, 1)).isTrue();
        assertThat(Dnd5eExperience.levelUpAvailable(900, 3)).isFalse();
    }

    @Test
    void noLevelUpPastTwenty() {
        assertThat(Dnd5eExperience.levelUpAvailable(999_999, 20)).isFalse();
    }
}

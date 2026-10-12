package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** The Monster Manual (2014) challenge table. */
class Dnd5eChallengeRatingsTest {

    @Test
    void readsExperienceAndProficiencyBonusByChallenge() {
        assertThat(Dnd5eChallengeRatings.experience("0")).isEqualTo(10);
        assertThat(Dnd5eChallengeRatings.experience("1/4")).isEqualTo(50);
        assertThat(Dnd5eChallengeRatings.experience("17")).isEqualTo(18000);
        assertThat(Dnd5eChallengeRatings.proficiencyBonus("1/2")).isEqualTo(2);
        assertThat(Dnd5eChallengeRatings.proficiencyBonus("4")).isEqualTo(2);
        assertThat(Dnd5eChallengeRatings.proficiencyBonus("5")).isEqualTo(3);
        assertThat(Dnd5eChallengeRatings.proficiencyBonus("13")).isEqualTo(5);
        assertThat(Dnd5eChallengeRatings.proficiencyBonus("30")).isEqualTo(9);
        assertThat(Dnd5eChallengeRatings.experience("—")).isNull();
        assertThat(Dnd5eChallengeRatings.proficiencyBonus("—")).isNull();
    }
}

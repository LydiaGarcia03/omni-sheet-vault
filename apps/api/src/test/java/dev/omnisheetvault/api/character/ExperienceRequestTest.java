package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ExperienceRequestTest {

    @Test
    void addsRemovesOrSets() {
        assertThat(new ExperienceRequest(300, null, null).applyTo(100)).isEqualTo(400);
        assertThat(new ExperienceRequest(null, 50, null).applyTo(100)).isEqualTo(50);
        assertThat(new ExperienceRequest(null, null, 900).applyTo(100)).isEqualTo(900);
    }

    @Test
    void removingNeverGoesBelowZero() {
        assertThat(new ExperienceRequest(null, 500, null).applyTo(100)).isZero();
    }
}

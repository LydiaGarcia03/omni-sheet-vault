package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DraftPreviewResponseTest {

    @Test
    void turnsCamelCaseSkillKeysIntoTitlesWithMinorWordsLowerCase() {
        assertThat(DraftPreviewResponse.words("sleightOfHand")).isEqualTo("Sleight of Hand");
        assertThat(DraftPreviewResponse.words("animalHandling")).isEqualTo("Animal Handling");
        assertThat(DraftPreviewResponse.words("perception")).isEqualTo("Perception");
    }
}

package dev.omnisheetvault.api.ruleset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

class CharacterCreationFlowTest {

    private final CharacterCreationFlow flow = mock(CharacterCreationFlow.class, CALLS_REAL_METHODS);

    @Test
    void aSystemWithoutFeatRulesAcceptsEveryFeat() {
        assertThat(flow.featProblems("{}", null, null)).isEmpty();
    }

    @Test
    void aSystemWithoutSourceRulesOffersEveryEntry() {
        assertThat(flow.offersCatalogueEntry("{}", null)).isTrue();
    }
}

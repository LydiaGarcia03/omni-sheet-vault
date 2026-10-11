package dev.omnisheetvault.api.ruleset.vtm5;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.SummaryFact;
import org.junit.jupiter.api.Test;

class Vtm5CharacterSummarizerTest {

    private final Vtm5CharacterSummarizer summarizer = new Vtm5CharacterSummarizer(Vtm5TestBuilds.MAPPER);

    @Test
    void aCardShowsClanAgeAndPredatorFromTheSheet() {
        assertThat(summarizer.summarize(Vtm5TestBuilds.json(Vtm5TestBuilds.validSheet()), null, null))
                .containsExactly(new SummaryFact("Clan", "Toreador"), new SummaryFact("Age", "Neonate"), new SummaryFact("Predator", "Consensualist"));
    }

    @Test
    void aDraftShowsOnlyWhatIsChosen() {
        assertThat(summarizer.summarize(null, "{\"age\":\"Neonate\"}", null)).containsExactly(new SummaryFact("Age", "Neonate"));
        assertThat(summarizer.systemId()).isEqualTo("vtm-v5");
        assertThat(new Vtm5GameSystem().displayName()).isEqualTo("Vampire: The Masquerade 5e");
    }
}

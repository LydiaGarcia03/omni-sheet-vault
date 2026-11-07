package dev.omnisheetvault.api.ruleset.daggerheart;

import static dev.omnisheetvault.api.ruleset.daggerheart.DaggerheartTestCatalogue.MAPPER;
import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.SummaryFact;
import org.junit.jupiter.api.Test;

class DaggerheartCharacterSummarizerTest {

    private final DaggerheartCharacterSummarizer summarizer = new DaggerheartCharacterSummarizer(MAPPER);
    private final DaggerheartTestCatalogue catalogue = new DaggerheartTestCatalogue();

    @Test
    void summarizesAPlayableCharactersSheet() {
        String sheet = new DaggerheartCharacterCreationFlow(MAPPER).materialize(null, DaggerheartTestCatalogue.VALID, catalogue).sheetJson();

        assertThat(summarizer.summarize(sheet, null, catalogue)).containsExactly(
                new SummaryFact("Class", "Ranger (Wayfinder) 1"), new SummaryFact("Heritage", "Slyborne Simiah"));
    }

    @Test
    void addsTheMulticlassAfterTheLevel() {
        String sheet = """
                {"level":6,"identity":{"class":{"name":"Ranger"},"subclass":{"name":"Wayfinder"},"multiclass":{"class":{"name":"Wizard"}},
                 "community":{"name":"Slyborne"},"ancestry":{"name":"Simiah"}}}""";

        assertThat(summarizer.summarize(sheet, null, catalogue)).first().isEqualTo(new SummaryFact("Class", "Ranger (Wayfinder) 6 · Wizard"));
    }

    @Test
    void summarizesADraftFromWhatItHasSoFar() {
        assertThat(summarizer.summarize(null, "{\"class\":\"wizard\"}", catalogue)).containsExactly(new SummaryFact("Class", "Wizard"));
        assertThat(summarizer.summarize(null, DaggerheartBuild.EMPTY, catalogue)).isEmpty();
        assertThat(new DaggerheartGameSystem().displayName()).isEqualTo("Daggerheart");
    }
}

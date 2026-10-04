package dev.omnisheetvault.api.ruleset.dnd5e;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.SummaryFact;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class Dnd5eCharacterSummarizerTest {

    private final Dnd5eCharacterSummarizer summarizer = new Dnd5eCharacterSummarizer(new ObjectMapper());

    @Test
    void aSheetListsEachClassWithItsOwnLevelThenTheSpecies() {
        String sheet = """
                {"level":7,
                 "classLevels":[
                   {"classSlug":"fighter","className":"Fighter","level":4,"hitDieSize":10},
                   {"classSlug":"wizard","className":"Wizard","level":3,"hitDieSize":6}],
                 "build":{"speciesSlug":"elf","subspeciesName":"High","classes":[]}}""";

        assertThat(summarizer.summarize(sheet, null, Dnd5eBuildFixtures.catalogue())).containsExactly(
                new SummaryFact("Classes", "Fighter 4 / Wizard 3"),
                new SummaryFact("Species", "High Elf"));
    }

    @Test
    void aDraftShowsWhatItsBuildHasSoFar() {
        String build = """
                {"speciesSlug":"tiefling","classes":[{"classSlug":"warlock","subclassSlug":null,"level":2}],"choices":[]}""";

        assertThat(summarizer.summarize(null, build, Dnd5eBuildFixtures.catalogue())).containsExactly(
                new SummaryFact("Classes", "Warlock 2"),
                new SummaryFact("Species", "Tiefling"));
    }

    @Test
    void anEmptyDraftHasNoFacts() {
        String build = """
                {"speciesSlug":null,"classes":[],"choices":[]}""";

        assertThat(summarizer.summarize(null, build, Dnd5eBuildFixtures.catalogue())).isEmpty();
    }

    @Test
    void aSlugTheCatalogueNoLongerHasStillReadsAsWords() {
        String build = """
                {"speciesSlug":"rock-gnome","classes":[{"classSlug":"blood-hunter","level":1}],"choices":[]}""";

        assertThat(summarizer.summarize(null, build, Dnd5eBuildFixtures.catalogue())).extracting(SummaryFact::value)
                .containsExactly("Blood hunter 1", "Rock gnome");
    }
}

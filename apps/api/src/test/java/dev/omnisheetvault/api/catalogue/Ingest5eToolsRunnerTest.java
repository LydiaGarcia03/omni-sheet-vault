package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Synthetic fixtures only — see SpellConverterTest's own note on why. */
class Ingest5eToolsRunnerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Ingest5eToolsRunner runner = new Ingest5eToolsRunner(objectMapper);

    private Path dataRoot;
    private Path outputDirectory;

    /** See CatalogueImportServiceTest's own note on why this isn't a JUnit @TempDir. */
    @BeforeEach
    void createDirectories() throws IOException {
        dataRoot = Files.createTempDirectory("ingest-5etools-runner-test-data");
        outputDirectory = Files.createTempDirectory("ingest-5etools-runner-test-output");
        Files.createDirectories(dataRoot.resolve("spells"));
        Files.writeString(dataRoot.resolve("books.json"), """
                {"book":[{"name":"Player's Handbook (2014)","id":"PHB","source":"PHB"},
                         {"name":"Plane Shift: Kaladesh","id":"PS-K","source":"PSK"}]}""");
        Files.writeString(dataRoot.resolve("adventures.json"), """
                {"adventure":[{"name":"Lost Mine of Phandelver","id":"LMoP","source":"LMoP"}]}""");
    }

    @Test
    void resolvesSourceCodesToFullNamesFromBooksAndAdventures() throws IOException {
        writeSpellFile("spells-a.json", spellFile("Book Bolt", "PSK"));
        writeSpellFile("spells-b.json", spellFile("Adventure Bolt", "lmop"));
        writeSpellFile("spells-c.json", spellFile("Unlisted Bolt", "EET"));
        writeSpellFile("spells-d.json", spellFile("Companion Bolt", "EEPC"));

        runner.run(
                new SpellConverter(objectMapper),
                new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()),
                outputDirectory);

        assertThat(entryText(outputDirectory, "book-bolt")).contains("\"sourceBook\" : \"Plane Shift: Kaladesh\"");
        assertThat(entryText(outputDirectory, "adventure-bolt")).contains("\"sourceBook\" : \"Lost Mine of Phandelver\"");
        assertThat(entryText(outputDirectory, "unlisted-bolt")).contains("\"sourceBook\" : \"Elemental Evil: Trinkets\"");
        assertThat(entryText(outputDirectory, "companion-bolt")).contains("\"sourceBook\" : \"Elemental Evil Player's Companion\"");
    }

    @Test
    void convertsOnlyInScopeSpellsIntoTheKindsEnglishFile() throws IOException {
        writeSpellFile("spells-phb.json", """
                {"spell":[
                    {"name":"Test Bolt","source":"PHB","page":1,"level":0,"school":"V",
                     "time":[{"number":1,"unit":"action"}],
                     "range":{"type":"point","distance":{"type":"feet","amount":120}},
                     "components":{"v":true,"s":true},"duration":[{"type":"instant"}],
                     "entries":["A test entry."]},
                    {"name":"Test Revision","source":"XPHB","page":1,"level":0,"school":"V",
                     "time":[{"number":1,"unit":"action"}],
                     "range":{"type":"point","distance":{"type":"self"}},
                     "components":{"v":true},"duration":[{"type":"instant"}],
                     "entries":["Should be dropped, it's the 2024 revision."]}
                ]}""");

        FiveEToolsDataSource dataSource = new FiveEToolsDataSource(dataRoot, objectMapper);
        FiveEToolsSourceClassifier classifier = new FiveEToolsSourceClassifier(Set.of("XPHB"));
        SpellConverter converter = new SpellConverter(objectMapper);

        int count = runner.run(converter, dataSource, classifier, outputDirectory);

        assertThat(count).isEqualTo(1);
        assertThat(slugs(outputDirectory)).contains("test-bolt");
        assertThat(slugs(outputDirectory)).doesNotContain("test-revision");
        assertThat(entryText(outputDirectory, "test-bolt")).contains("\"name\" : \"Test Bolt\"");
    }

    @Test
    void reRunningAgainstTheSameInputProducesByteIdenticalOutput() throws IOException {
        writeSpellFile("spells-phb.json", """
                {"spell":[{"name":"Test Bolt","source":"PHB","page":1,"level":0,"school":"V",
                    "time":[{"number":1,"unit":"action"}],
                    "range":{"type":"point","distance":{"type":"feet","amount":120}},
                    "components":{"v":true,"s":true},"duration":[{"type":"instant"}],
                    "entries":["A test entry."]}]}""");

        FiveEToolsDataSource dataSource = new FiveEToolsDataSource(dataRoot, objectMapper);
        FiveEToolsSourceClassifier classifier = new FiveEToolsSourceClassifier(Set.of());
        SpellConverter converter = new SpellConverter(objectMapper);

        runner.run(converter, dataSource, classifier, outputDirectory);
        String firstRun = Files.readString(CatalogueEntryFiles.englishFile(outputDirectory));

        runner.run(converter, dataSource, classifier, outputDirectory);
        String secondRun = Files.readString(CatalogueEntryFiles.englishFile(outputDirectory));

        assertThat(secondRun).isEqualTo(firstRun);
    }

    @Test
    void createsTheOutputDirectoryWhenItDoesNotExistYet() throws IOException {
        writeSpellFile("spells-phb.json", """
                {"spell":[{"name":"Test Bolt","source":"PHB","page":1,"level":0,"school":"V",
                    "time":[{"number":1,"unit":"action"}],
                    "range":{"type":"point","distance":{"type":"self"}},
                    "components":{"v":true},"duration":[{"type":"instant"}],
                    "entries":["A test entry."]}]}""");
        Path missingOutputDirectory = outputDirectory.resolve("nested/does-not-exist-yet");

        int count = runner.run(
                new SpellConverter(objectMapper),
                new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()),
                missingOutputDirectory);

        assertThat(count).isEqualTo(1);
        assertThat(slugs(missingOutputDirectory)).contains("test-bolt");
    }

    @Test
    void keepsSameNamedEntriesFromDifferentSourcesAsSeparateSourceSuffixedEntries() throws IOException {
        Files.writeString(outputDirectory.resolve("test-bolt.json"), "stale merged entry");
        writeSpellFile("spells-a.json", spellFile("Test Bolt", "ERLW"));
        writeSpellFile("spells-b.json", spellFile("Test Bolt", "EFA"));

        int count = runner.run(
                new SpellConverter(objectMapper),
                new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()),
                outputDirectory);

        assertThat(count).isEqualTo(2);
        assertThat(slugs(outputDirectory)).contains("test-bolt-erlw");
        assertThat(slugs(outputDirectory)).contains("test-bolt-efa");
        assertThat(slugs(outputDirectory)).doesNotContain("test-bolt");
        assertThat(entryText(outputDirectory, "test-bolt-efa")).contains("\"slug\" : \"test-bolt-efa\"");
    }

    @Test
    void dropsEntriesItNoLongerProducesAndOlderPerEntryFiles() throws IOException {
        Files.writeString(outputDirectory.resolve("dropped-bolt.json"), "an entry from a source now out of scope");
        writeSpellFile("spells-phb.json", spellFile("Test Bolt", "PHB"));

        runner.run(new SpellConverter(objectMapper), new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()), outputDirectory);

        assertThat(outputDirectory.resolve("dropped-bolt.json")).doesNotExist();
        assertThat(entryText(outputDirectory, "test-bolt")).contains("\"sourceBook\" : \"Player's Handbook\"");
    }

    @Test
    void writesEachEntrysSourceCodeNextToItsSourceName() throws IOException {
        writeSpellFile("spells-phb.json", spellFile("Test Bolt", "PHB"));

        runner.run(new SpellConverter(objectMapper), new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()), outputDirectory);

        assertThat(entryText(outputDirectory, "test-bolt")).contains("\"sourceCode\" : \"PHB\"");
    }

    @Test
    void sourcesPublishedWithOrAfterThe2024PlayersHandbookAreOutOfScope() throws IOException {
        Files.writeString(dataRoot.resolve("books.json"), """
                {"book":[{"name":"Player's Handbook (2014)","source":"PHB","published":"2014-08-19"},
                         {"name":"Player's Handbook (2024)","source":"XPHB","published":"2024-09-17"},
                         {"name":"Heroes Book","source":"HB","published":"2025-11-11"}]}""");
        Files.writeString(dataRoot.resolve("adventures.json"), """
                {"adventure":[{"name":"Old Adventure","source":"OA","published":"2020-01-01"}]}""");

        FiveEToolsSourceClassifier classifier =
                FiveEToolsSourceClassifier.forRules2014(FiveEToolsSourceNames.load(new FiveEToolsDataSource(dataRoot, objectMapper)));

        assertThat(classifier.isInScope("PHB")).isTrue();
        assertThat(classifier.isInScope("oa")).isTrue();
        assertThat(classifier.isInScope("XPHB")).isFalse();
        assertThat(classifier.isInScope("hb")).isFalse();
    }

    @Test
    void failsWhenTheSameNameAppearsTwiceInTheSameSource() throws IOException {
        writeSpellFile("spells-a.json", spellFile("Test Bolt", "PHB"));
        writeSpellFile("spells-b.json", spellFile("Test Bolt", "PHB"));

        assertThatThrownBy(() -> runner.run(
                new SpellConverter(objectMapper),
                new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()),
                outputDirectory))
                .isInstanceOf(FiveEToolsIngestException.class)
                .hasMessageContaining("test-bolt-phb");
    }

    @Test
    void keepsTranslationFiles() throws IOException {
        Path translation = outputDirectory.resolve("spells.pt-BR.json");
        Files.writeString(translation, "{}");
        writeSpellFile("spells-phb.json", spellFile("Test Bolt", "PHB"));

        runner.run(new SpellConverter(objectMapper), new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()), outputDirectory);

        assertThat(translation).exists();
    }

    /** The slugs in the directory's English file, in file order. */
    private List<String> slugs(Path directory) throws IOException {
        return CatalogueEntryFiles.entries(CatalogueEntryFiles.englishFile(directory), objectMapper).stream()
                .map(entry -> entry.get("slug").asString())
                .toList();
    }

    /** One entry of the directory's English file, pretty-printed as the runner writes it. */
    private String entryText(Path directory, String slug) throws IOException {
        JsonNode entry = CatalogueEntryFiles.entries(CatalogueEntryFiles.englishFile(directory), objectMapper).stream()
                .filter(candidate -> slug.equals(candidate.get("slug").asString()))
                .findFirst()
                .orElseThrow();
        return objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(entry);
    }

    private static String spellFile(String name, String source) {
        return """
                {"spell":[{"name":"%s","source":"%s","page":1,"level":0,"school":"V",
                    "time":[{"number":1,"unit":"action"}],
                    "range":{"type":"point","distance":{"type":"self"}},
                    "components":{"v":true},"duration":[{"type":"instant"}],
                    "entries":["A test entry."]}]}""".formatted(name, source);
    }

    private void writeSpellFile(String fileName, String content) throws IOException {
        Files.writeString(dataRoot.resolve("spells").resolve(fileName), content);
    }
}

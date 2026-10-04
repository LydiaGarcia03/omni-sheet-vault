package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

        assertThat(Files.readString(outputDirectory.resolve("book-bolt.json"))).contains("\"sourceBook\" : \"Plane Shift: Kaladesh\"");
        assertThat(Files.readString(outputDirectory.resolve("adventure-bolt.json"))).contains("\"sourceBook\" : \"Lost Mine of Phandelver\"");
        assertThat(Files.readString(outputDirectory.resolve("unlisted-bolt.json"))).contains("\"sourceBook\" : \"Elemental Evil: Trinkets\"");
        assertThat(Files.readString(outputDirectory.resolve("companion-bolt.json"))).contains("\"sourceBook\" : \"Elemental Evil Player's Companion\"");
    }

    @Test
    void convertsOnlyInScopeSpellsAndWritesOneFilePerSlug() throws IOException {
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
        assertThat(outputDirectory.resolve("test-bolt.json")).exists();
        assertThat(outputDirectory.resolve("test-revision.json")).doesNotExist();
        assertThat(Files.readString(outputDirectory.resolve("test-bolt.json"))).contains("\"name\" : \"Test Bolt\"");
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
        String firstRun = Files.readString(outputDirectory.resolve("test-bolt.json"));

        runner.run(converter, dataSource, classifier, outputDirectory);
        String secondRun = Files.readString(outputDirectory.resolve("test-bolt.json"));

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
        assertThat(missingOutputDirectory.resolve("test-bolt.json")).exists();
    }

    @Test
    void keepsSameNamedEntriesFromDifferentSourcesAsSeparateSourceSuffixedFiles() throws IOException {
        Files.writeString(outputDirectory.resolve("test-bolt.json"), "stale merged entry");
        writeSpellFile("spells-a.json", spellFile("Test Bolt", "ERLW"));
        writeSpellFile("spells-b.json", spellFile("Test Bolt", "EFA"));

        int count = runner.run(
                new SpellConverter(objectMapper),
                new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()),
                outputDirectory);

        assertThat(count).isEqualTo(2);
        assertThat(outputDirectory.resolve("test-bolt-erlw.json")).exists();
        assertThat(outputDirectory.resolve("test-bolt-efa.json")).exists();
        assertThat(outputDirectory.resolve("test-bolt.json")).doesNotExist();
        assertThat(Files.readString(outputDirectory.resolve("test-bolt-efa.json"))).contains("\"slug\" : \"test-bolt-efa\"");
    }

    @Test
    void deletesFilesOfEntriesItNoLongerProduces() throws IOException {
        Files.writeString(outputDirectory.resolve("dropped-bolt.json"), "an entry from a source now out of scope");
        writeSpellFile("spells-phb.json", spellFile("Test Bolt", "PHB"));

        runner.run(new SpellConverter(objectMapper), new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()), outputDirectory);

        assertThat(outputDirectory.resolve("dropped-bolt.json")).doesNotExist();
        assertThat(Files.readString(outputDirectory.resolve("test-bolt.json"))).contains("\"sourceBook\" : \"Player's Handbook\"");
    }

    @Test
    void writesEachEntrysSourceCodeNextToItsSourceName() throws IOException {
        writeSpellFile("spells-phb.json", spellFile("Test Bolt", "PHB"));

        runner.run(new SpellConverter(objectMapper), new FiveEToolsDataSource(dataRoot, objectMapper),
                new FiveEToolsSourceClassifier(Set.of()), outputDirectory);

        assertThat(Files.readString(outputDirectory.resolve("test-bolt.json"))).contains("\"sourceCode\" : \"PHB\"");
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

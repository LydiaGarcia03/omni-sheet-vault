package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Uses synthetic content, not real book text — see adr-0005 for why the real catalogue
 * is a legal exposure the owner accepted deliberately; test fixtures don't need to
 * carry that risk too.
 */
@SpringBootTest
@Testcontainers
@Transactional
class CatalogueImportServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CatalogueImportService catalogueImportService;

    @Autowired
    private CatalogueEntryRepository catalogueEntryRepository;

    private Path directory;

    /**
     * A hand-rolled temp directory instead of JUnit's {@code @TempDir}: on Windows,
     * its post-test cleanup intermittently throws {@code AccessDeniedException} (file
     * indexing/AV holding a handle), which JUnit reports as the test itself failing.
     * Not deleting afterward is a deliberate trade — the OS reclaims scratch temp
     * files on its own, and fighting Windows file locks in a test isn't worth it.
     */
    @BeforeEach
    void createTempDirectory() throws IOException {
        directory = Files.createTempDirectory("catalogue-import-test");
    }

    @Test
    void importsEveryJsonFileInTheDirectory() throws IOException {
        writeFixture("sparkling-bolt.json", """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"sparkling-bolt","name":"Sparkling Bolt",
                "sourceBook":"Test Fixtures","sourcePage":1,"tags":["evocation"],
                "description":"A fictional test spell.","data":{"level":1,"school":"evocation"}}""");
        writeFixture("traveler-pack.json", """
                {"systemId":"dnd-5e","kind":"ITEM","slug":"traveler-pack","name":"Traveler's Pack",
                "tags":["gear"],"description":"A fictional test item.","data":{"cost":2}}""");

        int imported = catalogueImportService.importFrom(directory);

        assertThat(imported).isEqualTo(2);
        assertThat(catalogueEntryRepository.findBySystemId("dnd-5e")).hasSize(2);
    }

    @Test
    void reimportingTheSameSlugUpdatesInPlaceInsteadOfDuplicating() throws IOException {
        writeFixture("sparkling-bolt.json", """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"sparkling-bolt","name":"Sparkling Bolt",
                "description":"First version.","data":{"level":1}}""");
        catalogueImportService.importFrom(directory);

        writeFixture("sparkling-bolt.json", """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"sparkling-bolt","name":"Sparkling Bolt",
                "description":"Second version.","data":{"level":2}}""");
        catalogueImportService.importFrom(directory);

        List<CatalogueEntry> entries = catalogueEntryRepository.findBySystemIdAndKind("dnd-5e", CatalogueEntryKind.SPELL);
        assertThat(entries).hasSize(1);
        assertThat(entries.get(0).description()).isEqualTo("Second version.");

        Optional<CatalogueEntry> bySlug =
                catalogueEntryRepository.findBySystemIdAndKindAndSlug("dnd-5e", CatalogueEntryKind.SPELL, "sparkling-bolt");
        assertThat(bySlug).isPresent();
    }

    @Test
    void removesEntriesOfAnImportedKindWhoseFileNoLongerExists() throws IOException {
        writeFixture("sparkling-bolt.json", """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"sparkling-bolt","name":"Sparkling Bolt","data":{}}""");
        writeFixture("traveler-pack.json", """
                {"systemId":"dnd-5e","kind":"ITEM","slug":"traveler-pack","name":"Traveler's Pack","data":{}}""");
        catalogueImportService.importFrom(directory);

        Path spellsOnly = Files.createTempDirectory("catalogue-import-test-renamed");
        Files.writeString(spellsOnly.resolve("sparkling-bolt-tst.json"), """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"sparkling-bolt-tst","name":"Sparkling Bolt","data":{}}""");
        catalogueImportService.importFrom(spellsOnly);

        assertThat(catalogueEntryRepository.findBySystemIdAndKind("dnd-5e", CatalogueEntryKind.SPELL))
                .extracting(CatalogueEntry::slug).containsExactly("sparkling-bolt-tst");
        assertThat(catalogueEntryRepository.findBySystemIdAndKind("dnd-5e", CatalogueEntryKind.ITEM))
                .extracting(CatalogueEntry::slug).containsExactly("traveler-pack");
    }

    @Test
    void keepsEachEntrysSourceCodeAndReportsItPerSourceBook() throws IOException {
        writeFixture("sparkling-bolt.json", """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"sparkling-bolt","name":"Sparkling Bolt",
                "sourceBook":"Test Fixtures","sourceCode":"TF","data":{}}""");
        writeFixture("glowing-bolt.json", """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"glowing-bolt","name":"Glowing Bolt",
                "sourceBook":"Test Fixtures","sourceCode":"TF","data":{}}""");
        catalogueImportService.importFrom(directory);

        assertThat(catalogueEntryRepository.findBySystemIdAndKindAndSlug("dnd-5e", CatalogueEntryKind.SPELL, "sparkling-bolt"))
                .map(CatalogueEntry::sourceCode).contains("TF");
        assertThat(catalogueEntryRepository.countBySourceBook("dnd-5e"))
                .extracting(CatalogueEntryRepository.SourceBookCount::getSourceBook, CatalogueEntryRepository.SourceBookCount::getSourceCode,
                        CatalogueEntryRepository.SourceBookCount::getEntryCount)
                .containsExactly(tuple("Test Fixtures", "TF", 2L));
    }

    @Test
    void rejectsAnEntryMissingARequiredField() throws IOException {
        writeFixture("invalid.json", """
                {"kind":"SPELL","slug":"no-system-id","name":"Missing System","data":{}}""");

        assertThatThrownBy(() -> catalogueImportService.importFrom(directory))
                .isInstanceOf(CatalogueImportException.class);
        assertThat(catalogueEntryRepository.findBySystemId("dnd-5e")).isEmpty();
    }

    private void writeFixture(String fileName, String content) throws IOException {
        Files.writeString(directory.resolve(fileName), content);
    }
}

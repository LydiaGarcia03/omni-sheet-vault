package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import dev.omnisheetvault.api.ruleset.registry.GameSystemRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.ObjectMapper;

/**
 * Constructs {@link CatalogueService} directly with each redaction value rather than
 * juggling Spring profiles mid-test — the constructor already takes the switch as a
 * plain argument (see its {@code @Value} parameter), so this is the direct way to
 * exercise both states.
 */
@SpringBootTest
@Testcontainers
@Transactional
class CatalogueServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CatalogueImportService catalogueImportService;

    @Autowired
    private CatalogueEntryRepository catalogueEntryRepository;

    @Autowired
    private GameSystemRegistry gameSystemRegistry;

    @Autowired
    private ObjectMapper objectMapper;

    private Path directory;

    /** See CatalogueImportServiceTest's doc comment on why this isn't {@code @TempDir}. */
    @BeforeEach
    void createTempDirectory() throws IOException {
        directory = Files.createTempDirectory("catalogue-service-test");
    }

    @Test
    void redactionEnabledHidesDescriptionButKeepsMechanics() throws IOException {
        importOneSpell();
        CatalogueService redacting = new CatalogueService(catalogueEntryRepository, gameSystemRegistry, objectMapper, true);

        CatalogueEntryResponse response = redacting.list("dnd-5e", CatalogueEntryKind.SPELL).get(0);

        assertThat(response.description().redacted()).isTrue();
        assertThat(response.description().value()).isNull();
        assertThat(response.name()).isEqualTo("Sparkling Bolt");
        assertThat(response.data().get("level").asInt()).isEqualTo(1);
    }

    @Test
    void redactionDisabledReturnsTheProse() throws IOException {
        importOneSpell();
        CatalogueService open = new CatalogueService(catalogueEntryRepository, gameSystemRegistry, objectMapper, false);

        CatalogueEntryResponse response = open.list("dnd-5e", CatalogueEntryKind.SPELL).get(0);

        assertThat(response.description().redacted()).isFalse();
        assertThat(response.description().value()).isEqualTo("A fictional test spell.");
    }

    @Test
    void rejectsAnUnknownSystem() {
        CatalogueService service = new CatalogueService(catalogueEntryRepository, gameSystemRegistry, objectMapper, false);

        assertThatThrownBy(() -> service.list("not-a-real-system", null))
                .isInstanceOf(UnsupportedGameSystemException.class);
    }

    @Test
    void gettingAMissingEntryThrows() {
        CatalogueService service = new CatalogueService(catalogueEntryRepository, gameSystemRegistry, objectMapper, false);

        assertThatThrownBy(() -> service.get(UUID.randomUUID())).isInstanceOf(CatalogueEntryNotFoundException.class);
    }

    @Test
    void listsSourcesAndMarksPlaytestAndPartneredOnes() throws IOException {
        Files.writeString(directory.resolve("wildemount-bolt.json"), """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"wildemount-bolt","name":"Wildemount Bolt",
                "sourceBook":"Explorer's Guide to Wildemount","sourceCode":"EGW","description":"","data":{"level":1}}""");
        Files.writeString(directory.resolve("psionic-bolt.json"), """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"psionic-bolt","name":"Psionic Bolt",
                "sourceBook":"UA: The Mystic Class","sourceCode":"UATheMysticClass","description":"","data":{"level":1}}""");
        Files.writeString(directory.resolve("plain-bolt.json"), """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"plain-bolt","name":"Plain Bolt",
                "sourceBook":"Player's Handbook","sourceCode":"PHB","description":"","data":{"level":1}}""");
        catalogueImportService.importFrom(directory);
        CatalogueService service = new CatalogueService(catalogueEntryRepository, gameSystemRegistry, objectMapper, false);

        assertThat(service.sources("dnd-5e"))
                .extracting(CatalogueSourceResponse::sourceCode, CatalogueSourceResponse::playtest, CatalogueSourceResponse::partner)
                .contains(tuple("UATheMysticClass", true, null), tuple("PHB", false, null), tuple("EGW", false, "Critical Role"));
        assertThat(service.partners("dnd-5e"))
                .containsExactly(new CataloguePartnerResponse("Critical Role", List.of("Explorer's Guide to Wildemount")));
    }

    private void importOneSpell() throws IOException {
        Files.writeString(directory.resolve("sparkling-bolt.json"), """
                {"systemId":"dnd-5e","kind":"SPELL","slug":"sparkling-bolt","name":"Sparkling Bolt",
                "description":"A fictional test spell.","data":{"level":1}}""");
        catalogueImportService.importFrom(directory);
    }
}

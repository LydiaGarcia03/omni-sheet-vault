package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

class FiveEToolsDataSourceTest {

    private Path rootDirectory;
    private FiveEToolsDataSource dataSource;

    /** See CatalogueImportServiceTest's own note on why this isn't a JUnit @TempDir. */
    @BeforeEach
    void createRootDirectory() throws IOException {
        rootDirectory = Files.createTempDirectory("5etools-data-source-test");
        dataSource = new FiveEToolsDataSource(rootDirectory, new ObjectMapper());
    }

    @Test
    void readsAJsonFileRelativeToTheRootDirectory() throws IOException {
        Files.createDirectories(rootDirectory.resolve("spells"));
        Files.writeString(rootDirectory.resolve("spells/spells-phb.json"), """
                {"spell":[{"name":"Test Spell","source":"PHB"}]}""");

        JsonNode result = dataSource.readDataFile("spells/spells-phb.json");

        assertThat(result.get("spell").get(0).get("name").asString()).isEqualTo("Test Spell");
    }

    @Test
    void wrapsAMissingFileInAFiveEToolsIngestException() {
        assertThatThrownBy(() -> dataSource.readDataFile("spells/does-not-exist.json"))
                .isInstanceOf(FiveEToolsIngestException.class);
    }

    @Test
    void toleratesALeadingUtf8Bom() throws IOException {
        Files.createDirectories(rootDirectory.resolve("spells"));
        Files.write(rootDirectory.resolve("spells/spells-phb.json"),
                ("﻿{\"spell\":[{\"name\":\"Test Spell\",\"source\":\"PHB\"}]}").getBytes(StandardCharsets.UTF_8));

        JsonNode result = dataSource.readDataFile("spells/spells-phb.json");

        assertThat(result.get("spell").get(0).get("name").asString()).isEqualTo("Test Spell");
    }
}

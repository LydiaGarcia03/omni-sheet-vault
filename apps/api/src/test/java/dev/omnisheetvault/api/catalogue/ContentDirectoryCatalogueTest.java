package dev.omnisheetvault.api.catalogue;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.ruleset.CatalogueRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ContentDirectoryCatalogueTest {

    @Test
    void readsEachKindFromItsOwnDirectory() throws IOException {
        Path root = Files.createTempDirectory("content-directory-catalogue-test");
        Files.createDirectories(root.resolve("species"));
        Files.writeString(root.resolve("species").resolve("dwarf.json"), """
                {"systemId":"dnd-5e","kind":"SPECIES","slug":"dwarf","name":"Dwarf","sourceBook":"PHB","sourcePage":18,
                 "tags":[],"description":"Short and sturdy.","data":{"speed":{"walk":25}}}""");
        ContentDirectoryCatalogue catalogue = new ContentDirectoryCatalogue(root, new ObjectMapper());

        CatalogueRecord dwarf = catalogue.find("SPECIES", "dwarf").orElseThrow();

        assertThat(dwarf.name()).isEqualTo("Dwarf");
        assertThat(dwarf.data().get("speed").get("walk").asInt()).isEqualTo(25);
        assertThat(catalogue.list("SPECIES")).hasSize(1);
        assertThat(catalogue.list("CLASS")).isEmpty();
        assertThat(catalogue.find("SPECIES", "elf")).isEmpty();
        assertThat(dwarf.playtest()).isFalse();
        assertThat(dwarf.partner()).isNull();
    }

    @Test
    void marksEntriesFromUnearthedArcanaAsPlaytest() throws IOException {
        Path root = Files.createTempDirectory("content-directory-catalogue-test");
        Files.createDirectories(root.resolve("classes"));
        Files.writeString(root.resolve("classes").resolve("mystic.json"), """
                {"systemId":"dnd-5e","kind":"CLASS","slug":"mystic","name":"Mystic","sourceBook":"UA: The Mystic Class",
                 "sourceCode":"UATheMysticClass","tags":[],"description":"","data":{}}""");
        ContentDirectoryCatalogue catalogue = new ContentDirectoryCatalogue(root, new ObjectMapper());

        assertThat(catalogue.find("CLASS", "mystic").orElseThrow().playtest()).isTrue();
    }

    @Test
    void readsDaggerheartKindsFromTheirPluralDirectories() throws IOException {
        Path root = Files.createTempDirectory("content-directory-catalogue-test");
        writeEntry(root, "ancestries", "ANCESTRY", "stonefolk");
        writeEntry(root, "communities", "COMMUNITY", "hillborne");
        writeEntry(root, "transformations", "TRANSFORMATION", "wraith");
        writeEntry(root, "domains", "DOMAIN", "ember");
        writeEntry(root, "domain_cards", "DOMAIN_CARD", "spark");
        ContentDirectoryCatalogue catalogue = new ContentDirectoryCatalogue(root, new ObjectMapper());

        assertThat(catalogue.find("ANCESTRY", "stonefolk")).isPresent();
        assertThat(catalogue.find("COMMUNITY", "hillborne")).isPresent();
        assertThat(catalogue.find("TRANSFORMATION", "wraith")).isPresent();
        assertThat(catalogue.find("DOMAIN", "ember")).isPresent();
        assertThat(catalogue.find("DOMAIN_CARD", "spark")).isPresent();
    }

    private static void writeEntry(Path root, String directory, String kind, String slug) throws IOException {
        Files.createDirectories(root.resolve(directory));
        Files.writeString(root.resolve(directory).resolve(slug + ".json"), """
                {"systemId":"daggerheart","kind":"%s","slug":"%s","name":"%s","sourceBook":"Test","sourceCode":"TEST",
                 "tags":[],"description":"","data":{}}""".formatted(kind, slug, slug));
    }
}

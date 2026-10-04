package dev.omnisheetvault.api.catalogue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Runs one {@link FiveEToolsConverter} end to end: load its raw entries, drop
 * out-of-scope sources (pipeline stage 2), convert, write one file per entry. Kept
 * separate from {@link Ingest5eToolsMain} so it's testable against a scratch
 * directory instead of the real {@code content/dnd-5e/} — {@code main()} stays a thin
 * CLI wrapper (arg parsing, default paths, the converter registry).
 */
final class Ingest5eToolsRunner {

    private final ObjectMapper objectMapper;

    Ingest5eToolsRunner(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * Safe to re-run — every output file is overwritten by its own deterministic slug,
     * and a file this run no longer produces (a source now out of scope, a plain slug
     * retired because several sources share it) is deleted.
     */
    int run(FiveEToolsConverter converter, FiveEToolsDataSource dataSource, FiveEToolsSourceClassifier classifier, Path outputDirectory) {
        try {
            Files.createDirectories(outputDirectory);
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not create output directory: " + outputDirectory, e);
        }

        FiveEToolsSourceNames sourceNames = FiveEToolsSourceNames.load(dataSource);
        List<FiveEToolsSlugs.SourcedEntry> converted = new ArrayList<>();
        for (JsonNode rawEntry : converter.loadRawEntries(dataSource)) {
            JsonNode sourceNode = rawEntry.get("source");
            if (sourceNode == null || !classifier.isInScope(sourceNode.asString())) {
                continue;
            }
            String sourceCode = sourceNode.asString();
            CatalogueEntryImport entry = converter.convert(rawEntry).withSource(sourceNames.nameOf(sourceCode), sourceCode);
            converted.add(new FiveEToolsSlugs.SourcedEntry(sourceCode, entry));
        }

        FiveEToolsSlugs.Disambiguated disambiguated = FiveEToolsSlugs.disambiguate(converted);
        Set<String> written = new HashSet<>();
        for (CatalogueEntryImport entry : disambiguated.entries()) {
            writeEntry(outputDirectory, entry);
            written.add(entry.slug() + ".json");
        }
        deleteFilesNotIn(outputDirectory, written);
        return disambiguated.entries().size();
    }

    private static void deleteFilesNotIn(Path outputDirectory, Set<String> written) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(outputDirectory, "*.json")) {
            for (Path file : files) {
                if (!written.contains(file.getFileName().toString())) {
                    Files.delete(file);
                }
            }
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not delete retired catalogue entries in " + outputDirectory, e);
        }
    }

    private void writeEntry(Path outputDirectory, CatalogueEntryImport imported) {
        Path file = outputDirectory.resolve(imported.slug() + ".json");
        try {
            String json = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(imported);
            Files.writeString(file, json + System.lineSeparator());
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not write catalogue entry: " + file, e);
        }
    }
}

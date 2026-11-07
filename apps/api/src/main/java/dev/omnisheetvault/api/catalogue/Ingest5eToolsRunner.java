package dev.omnisheetvault.api.catalogue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Runs one {@link FiveEToolsConverter} end to end: load its raw entries, drop
 * out-of-scope sources (pipeline stage 2), convert, write every entry to the kind's English file. Kept
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
     * Safe to re-run: the English file is rewritten whole, in slug order, so an entry this run no longer produces
     * (a source now out of scope, a plain slug retired because several sources share it) is gone. Older
     * one-file-per-entry files are deleted; translation files stay.
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
        List<CatalogueEntryImport> entries = disambiguated.entries().stream()
                .sorted(Comparator.comparing(CatalogueEntryImport::slug))
                .toList();
        Path englishFile = CatalogueEntryFiles.englishFile(outputDirectory);
        deleteEntryFilesOtherThan(outputDirectory, englishFile);
        writeEntries(englishFile, entries);
        return entries.size();
    }

    private static void deleteEntryFilesOtherThan(Path outputDirectory, Path englishFile) {
        try {
            for (Path file : CatalogueEntryFiles.entryFiles(outputDirectory)) {
                if (!file.equals(englishFile)) {
                    Files.delete(file);
                }
            }
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not delete retired catalogue entries in " + outputDirectory, e);
        }
    }

    private void writeEntries(Path file, List<CatalogueEntryImport> entries) {
        try {
            String json = objectMapper.writer().withDefaultPrettyPrinter().writeValueAsString(entries);
            Files.writeString(file, json + System.lineSeparator());
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not write catalogue entries: " + file, e);
        }
    }
}

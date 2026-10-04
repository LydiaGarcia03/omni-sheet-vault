package dev.omnisheetvault.api.catalogue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Reads raw 5etools JSON files from the owner's local, gitignored data folder
 * (tech-stack.md's "Local-only data" — {@code tools/5etools-data/}, populated by hand
 * from a 5etools-src release zip, never fetched from GitHub by this tool itself). Not a
 * Spring bean: constructed directly by whatever entry point drives an ingestion run —
 * see systems/dnd-5e/features/5etools-ingestion.md's pipeline stage 1.
 */
final class FiveEToolsDataSource {

    private final Path rootDirectory;
    private final ObjectMapper objectMapper;

    FiveEToolsDataSource(Path rootDirectory, ObjectMapper objectMapper) {
        this.rootDirectory = rootDirectory;
        this.objectMapper = objectMapper;
    }

    boolean hasDataFile(String relativePath) {
        return Files.isRegularFile(rootDirectory.resolve(relativePath));
    }

    JsonNode readDataFile(String relativePath) {
        Path file = rootDirectory.resolve(relativePath);
        try {
            return objectMapper.readTree(stripUtf8Bom(Files.readString(file)));
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not read 5etools data file: " + file, e);
        }
    }

    /** Reads every file matching {@code globPattern} inside a subdirectory — one converter kind's data is often split across several source-book files (e.g. {@code spells-phb.json}, {@code spells-xge.json}). */
    List<JsonNode> readDataFiles(String relativeDirectory, String globPattern) {
        Path directory = rootDirectory.resolve(relativeDirectory);
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, globPattern)) {
            List<JsonNode> nodes = new ArrayList<>();
            for (Path file : files) {
                nodes.add(objectMapper.readTree(stripUtf8Bom(Files.readString(file))));
            }
            return nodes;
        } catch (IOException e) {
            throw new FiveEToolsIngestException("Could not read 5etools data files matching " + globPattern + " in " + directory, e);
        }
    }

    /** Some Windows tooling writes a UTF-8 BOM; {@code Files.readString} doesn't strip it, and Jackson rejects it as an invalid token. */
    private static String stripUtf8Bom(String content) {
        return !content.isEmpty() && content.charAt(0) == '﻿' ? content.substring(1) : content;
    }
}

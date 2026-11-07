package dev.omnisheetvault.api.catalogue;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A kind's content files: {@code <kind>.en.json} holds every entry as a JSON array, and a file holding one JSON object is
 * one entry (Daggerheart's and the tests' layout). Translation files ({@code <kind>.pt-BR.json}) are not entries.
 */
final class CatalogueEntryFiles {

    /** A language other than English before {@code .json}: {@code spells.pt-BR.json}; group 1 is the language tag. */
    private static final Pattern TRANSLATION_FILE = Pattern.compile(".*\\.(?!en\\.json$)([a-z]{2}(?:-[A-Z]{2})?)\\.json$");

    private CatalogueEntryFiles() {
    }

    /** The English file a kind's entries are written to, e.g. {@code spells/spells.en.json}. */
    static Path englishFile(Path kindDirectory) {
        return kindDirectory.resolve(kindDirectory.getFileName() + ".en.json");
    }

    /** Every entry file of the directory, translations left out, in name order. */
    static List<Path> entryFiles(Path directory) throws IOException {
        List<Path> files = new ArrayList<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.json")) {
            for (Path file : stream) {
                if (!TRANSLATION_FILE.matcher(file.getFileName().toString()).matches()) {
                    files.add(file);
                }
            }
        }
        files.sort(null);
        return files;
    }

    /** The directory's translation files by language tag, e.g. {@code pt-BR → spells.pt-BR.json}. */
    static Map<String, Path> translationFiles(Path directory) throws IOException {
        Map<String, Path> files = new TreeMap<>();
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(directory, "*.json")) {
            for (Path file : stream) {
                Matcher matcher = TRANSLATION_FILE.matcher(file.getFileName().toString());
                if (matcher.matches()) {
                    files.put(matcher.group(1), file);
                }
            }
        }
        return files;
    }

    /** The file's entries as JSON trees: each element of an array, or the object itself. */
    static List<JsonNode> entries(Path file, ObjectMapper objectMapper) throws IOException {
        JsonNode root = objectMapper.readTree(Files.readString(file));
        List<JsonNode> entries = new ArrayList<>();
        if (root.isArray()) {
            root.forEach(entries::add);
        } else {
            entries.add(root);
        }
        return entries;
    }
}

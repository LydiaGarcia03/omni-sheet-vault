package dev.omnisheetvault.api.catalogue;

import dev.omnisheetvault.api.desktop.DesktopProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * The desktop edition imports the bundled {@code content/} tree on its own: on the first start, and again whenever
 * an update ships different content. A fingerprint of the content, kept in the data folder, tells the two apart.
 */
@Component
@Profile("desktop")
class DesktopCatalogueBootstrap implements ApplicationRunner {

    static final String FINGERPRINT_FILE = "catalogue.fingerprint";
    private static final Logger LOG = LoggerFactory.getLogger(DesktopCatalogueBootstrap.class);

    private final CatalogueImportService importService;
    private final DesktopProperties properties;

    DesktopCatalogueBootstrap(CatalogueImportService importService, DesktopProperties properties) {
        this.importService = importService;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
        Path content = properties.contentPath();
        if (!Files.isDirectory(content)) {
            LOG.warn("No catalogue content at {}; the catalogue stays as it is", content.toAbsolutePath());
            return;
        }
        Path fingerprintFile = properties.dataPath().resolve(FINGERPRINT_FILE);
        String fingerprint = fingerprint(content);
        if (Files.isRegularFile(fingerprintFile) && Files.readString(fingerprintFile).equals(fingerprint)) {
            return;
        }
        int imported = importAll(content);
        Files.writeString(fingerprintFile, fingerprint);
        LOG.info("Imported {} catalogue entries from {}", imported, content.toAbsolutePath());
    }

    private int importAll(Path content) throws IOException {
        int imported = 0;
        for (Path systemRoot : directoriesIn(content)) {
            for (CatalogueEntryKind kind : CatalogueEntryKind.values()) {
                Path kindDirectory = CatalogueContentLayout.directory(systemRoot, kind);
                if (Files.isDirectory(kindDirectory)) {
                    imported += importService.importFrom(kindDirectory);
                }
            }
        }
        return imported;
    }

    private static List<Path> directoriesIn(Path directory) throws IOException {
        try (Stream<Path> children = Files.list(directory)) {
            return children.filter(Files::isDirectory).sorted().toList();
        }
    }

    /** A digest of every file's relative path and bytes, in a stable order. */
    static String fingerprint(Path content) throws IOException {
        MessageDigest digest = sha256();
        try (Stream<Path> files = Files.walk(content)) {
            for (Path file : files.filter(Files::isRegularFile).sorted().toList()) {
                digest.update(content.relativize(file).toString().replace('\\', '/').getBytes());
                digest.update(readBytes(file));
            }
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    private static byte[] readBytes(Path file) {
        try {
            return Files.readAllBytes(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static MessageDigest sha256() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is always available", e);
        }
    }
}

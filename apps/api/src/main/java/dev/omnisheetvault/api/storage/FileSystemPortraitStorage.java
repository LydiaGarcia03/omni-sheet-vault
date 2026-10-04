package dev.omnisheetvault.api.storage;

import dev.omnisheetvault.api.desktop.DesktopProperties;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * {@link PortraitStorage} for the desktop edition: plain files in the user's data folder, served back by
 * {@link LocalPortraitFileController}. The app only answers this machine, so its URLs need no signature.
 */
@Component
@Profile("desktop")
class FileSystemPortraitStorage implements PortraitStorage {

    static final String URL_PREFIX = "/api/portrait-files/";

    private final Path root;

    FileSystemPortraitStorage(DesktopProperties properties) {
        this.root = properties.portraitPath().toAbsolutePath().normalize();
    }

    @Override
    public void store(String key, byte[] content, String contentType) {
        Path file = resolve(key);
        try {
            Files.createDirectories(file.getParent());
            Files.write(file, content);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not save portrait " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException e) {
            throw new UncheckedIOException("Could not delete portrait " + key, e);
        }
    }

    @Override
    public URI temporaryUrl(String key) {
        return URI.create(URL_PREFIX + key);
    }

    /** The stored file for a key, if there is one. */
    Optional<Path> find(String key) {
        Path file = resolve(key);
        return Files.isRegularFile(file) ? Optional.of(file) : Optional.empty();
    }

    /** A key always stays inside the portrait folder, whatever it contains. */
    private Path resolve(String key) {
        Path file = root.resolve(key).normalize();
        if (!file.startsWith(root) || file.equals(root)) {
            throw new IllegalArgumentException("Portrait key outside the portrait folder: " + key);
        }
        return file;
    }
}

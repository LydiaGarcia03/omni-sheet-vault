package dev.omnisheetvault.api.desktop;

import java.nio.file.Path;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The desktop edition's folders: {@code dataDirectory} holds the database, portraits and bookkeeping and survives
 * updates; {@code contentDirectory} is the bundled {@code content/} tree the catalogue is imported from;
 * {@code webDirectory} is the web app built for the desktop edition; {@code runtimeDirectory} is where the PostgreSQL
 * binaries are unpacked, once, and defaults to the data folder.
 * Bound as plain text, because Spring's own conversion to {@code Path} treats a relative path as a resource.
 */
@ConfigurationProperties("app.desktop")
public record DesktopProperties(String dataDirectory, String contentDirectory, String webDirectory, String runtimeDirectory) {

    public Path dataPath() {
        return Path.of(dataDirectory);
    }

    public Path contentPath() {
        return Path.of(contentDirectory);
    }

    public Path webPath() {
        return Path.of(webDirectory);
    }

    public Path runtimePath() {
        return runtimeDirectory == null || runtimeDirectory.isBlank() ? dataPath().resolve("runtime") : Path.of(runtimeDirectory);
    }

    public Path databasePath() {
        return dataPath().resolve("db");
    }

    public Path portraitPath() {
        return dataPath().resolve("portraits");
    }
}

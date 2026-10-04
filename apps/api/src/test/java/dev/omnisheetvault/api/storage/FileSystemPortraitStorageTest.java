package dev.omnisheetvault.api.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.desktop.DesktopProperties;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemPortraitStorageTest {

    @TempDir
    Path dataDirectory;

    private FileSystemPortraitStorage storage() {
        return new FileSystemPortraitStorage(new DesktopProperties(dataDirectory.toString(), dataDirectory.resolve("content").toString(), dataDirectory.resolve("web").toString(), null));
    }

    @Test
    void storesFindsAndDeletesAPortrait() {
        FileSystemPortraitStorage storage = storage();

        storage.store("uploads/a/b.png", new byte[] {1, 2, 3}, "image/png");

        assertThat(storage.find("uploads/a/b.png")).hasValueSatisfying(file -> assertThat(file).hasBinaryContent(new byte[] {1, 2, 3}));
        assertThat(storage.temporaryUrl("uploads/a/b.png")).hasToString("/api/portrait-files/uploads/a/b.png");

        storage.delete("uploads/a/b.png");

        assertThat(storage.find("uploads/a/b.png")).isEmpty();
    }

    @Test
    void aKeyCanNeverLeaveThePortraitFolder() {
        FileSystemPortraitStorage storage = storage();

        assertThatThrownBy(() -> storage.store("../catalogue.fingerprint", new byte[] {1}, "image/png"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> storage.find("uploads/../../db/postgresql.conf"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}

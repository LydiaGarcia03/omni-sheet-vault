package dev.omnisheetvault.api.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.util.regex.Pattern;
import org.springframework.context.annotation.Profile;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** Serves the desktop edition's uploaded portraits from disk, at the URLs {@link FileSystemPortraitStorage} hands out. */
@RestController
@Profile("desktop")
class LocalPortraitFileController {

    private static final Pattern UUID_PATTERN = Pattern.compile("[0-9a-fA-F-]{36}");
    private static final Pattern PORTRAIT_FILE = Pattern.compile("[0-9a-fA-F-]{36}\\.png");

    private final FileSystemPortraitStorage storage;

    LocalPortraitFileController(FileSystemPortraitStorage storage) {
        this.storage = storage;
    }

    @GetMapping(FileSystemPortraitStorage.URL_PREFIX + "uploads/{characterId}/{fileName}")
    ResponseEntity<byte[]> portrait(@PathVariable String characterId, @PathVariable String fileName) throws IOException {
        if (!UUID_PATTERN.matcher(characterId).matches() || !PORTRAIT_FILE.matcher(fileName).matches()) {
            return ResponseEntity.notFound().build();
        }
        var file = storage.find("uploads/" + characterId + "/" + fileName);
        if (file.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.maxAge(Duration.ofDays(1)).cachePrivate())
                .body(Files.readAllBytes(file.get()));
    }
}

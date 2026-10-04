package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.storage.MinioTestImage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class CharacterPortraitServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Container
    static MinIOContainer minio = new MinIOContainer(MinioTestImage.IMAGE);

    @DynamicPropertySource
    static void storage(DynamicPropertyRegistry registry) {
        registry.add("storage.endpoint", minio::getS3URL);
        registry.add("storage.access-key", minio::getUserName);
        registry.add("storage.secret-key", minio::getPassword);
        registry.add("storage.bucket", () -> "portrait-service-test");
    }

    private final HttpClient http = HttpClient.newHttpClient();

    @Autowired
    private CharacterService characterService;

    @Autowired
    private CharacterPortraitService portraitService;

    @Test
    void aPresetIsStoredAsItsKeyAndDescribedById() {
        Jwt owner = newPlayer();
        Character character = characterService.create(owner, "Aria", "dnd-5e");

        PortraitResponse portrait = portraitService.choosePreset(owner, character.id(), "10064");

        assertThat(portrait).isEqualTo(PortraitResponse.preset("10064"));
        assertThat(characterService.getMine(owner, character.id()).portraitKey()).isEqualTo("preset:10064");
    }

    @Test
    void anUploadIsServedThroughItsSignedUrl() throws Exception {
        Jwt owner = newPlayer();
        Character character = characterService.create(owner, "Borin", "dnd-5e");

        PortraitResponse portrait = portraitService.upload(owner, character.id(), png(300, 200));

        assertThat(portrait.kind()).isEqualTo(PortraitResponse.Kind.UPLOAD);
        HttpResponse<byte[]> image = get(URI.create(portrait.url()));
        assertThat(image.statusCode()).isEqualTo(200);
        assertThat(image.headers().firstValue("Content-Type")).contains("image/png");
    }

    @Test
    void replacingAnUploadDeletesTheOldObject() throws Exception {
        Jwt owner = newPlayer();
        Character character = characterService.create(owner, "Cael", "dnd-5e");
        String firstUrl = portraitService.upload(owner, character.id(), png(64, 64)).url();

        portraitService.choosePreset(owner, character.id(), "10070");

        assertThat(get(URI.create(firstUrl)).statusCode()).isEqualTo(404);
    }

    @Test
    void removingLeavesNoPortrait() {
        Jwt owner = newPlayer();
        Character character = characterService.create(owner, "Dara", "dnd-5e");
        portraitService.choosePreset(owner, character.id(), "10071");

        portraitService.remove(owner, character.id());

        assertThat(portraitService.describe(characterService.getMine(owner, character.id()))).isNull();
    }

    @Test
    void removingAnUploadDeletesItAndRemovingAgainIsHarmless() throws Exception {
        Jwt owner = newPlayer();
        Character character = characterService.create(owner, "Gale", "dnd-5e");
        String url = portraitService.upload(owner, character.id(), png(32, 32)).url();

        portraitService.remove(owner, character.id());
        portraitService.remove(owner, character.id());

        assertThat(get(URI.create(url)).statusCode()).isEqualTo(404);
        assertThat(characterService.getMine(owner, character.id()).portraitKey()).isNull();
    }

    @Test
    void aStaleCopySavedLaterDoesNotUndoThePortrait() {
        Jwt owner = newPlayer();
        Character character = characterService.create(owner, "Eryn", "dnd-5e");
        Character loadedByAnAutosave = characterService.getMine(owner, character.id());

        portraitService.choosePreset(owner, character.id(), "10072");
        characterService.save(loadedByAnAutosave);

        assertThat(characterService.getMine(owner, character.id()).portraitKey()).isEqualTo("preset:10072");
    }

    @Test
    void anotherPlayersCharacterIsNotFound() {
        Character character = characterService.create(newPlayer(), "Fenn", "dnd-5e");

        assertThatThrownBy(() -> portraitService.choosePreset(newPlayer(), character.id(), "10064"))
                .isInstanceOf(CharacterNotFoundException.class);
    }

    private HttpResponse<byte[]> get(URI url) throws IOException, InterruptedException {
        return http.send(HttpRequest.newBuilder(url).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
    }

    private static byte[] png(int width, int height) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return bytes.toByteArray();
    }

    private static Jwt newPlayer() {
        String subject = UUID.randomUUID().toString();
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("preferred_username", "player-" + subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}

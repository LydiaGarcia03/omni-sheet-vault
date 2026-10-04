package dev.omnisheetvault.api.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class S3PortraitStorageTest {

    @Container
    static MinIOContainer minio = new MinIOContainer(MinioTestImage.IMAGE);

    private final HttpClient http = HttpClient.newHttpClient();
    private S3PortraitStorage storage;

    @BeforeEach
    void connect() {
        storage = new S3PortraitStorage(new StorageProperties(
                URI.create(minio.getS3URL()), null, "test-portraits", minio.getUserName(), minio.getPassword(), null,
                Duration.ofMinutes(5)));
    }

    @AfterEach
    void close() {
        storage.destroy();
    }

    @Test
    void storesIntoANewBucketAndServesItThroughASignedUrl() throws IOException, InterruptedException {
        byte[] content = {1, 2, 3, 4};

        storage.store("uploads/one/portrait.png", content, "image/png");
        HttpResponse<byte[]> response = get(storage.temporaryUrl("uploads/one/portrait.png"));

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.body()).containsExactly(content);
        assertThat(response.headers().firstValue("Content-Type")).contains("image/png");
    }

    @Test
    void aDeletedPortraitIsNoLongerServed() throws IOException, InterruptedException {
        storage.store("uploads/two/portrait.png", new byte[] {9}, "image/png");

        storage.delete("uploads/two/portrait.png");

        assertThat(get(storage.temporaryUrl("uploads/two/portrait.png")).statusCode()).isEqualTo(404);
    }

    @Test
    void aSignedUrlCarriesItsExpiry() {
        storage.store("uploads/three/portrait.png", new byte[] {7}, "image/png");

        String query = storage.temporaryUrl("uploads/three/portrait.png").getQuery();

        assertThat(query).contains("X-Amz-Expires=300").contains("X-Amz-Signature=");
    }

    private HttpResponse<byte[]> get(URI url) throws IOException, InterruptedException {
        return http.send(HttpRequest.newBuilder(url).GET().build(), HttpResponse.BodyHandlers.ofByteArray());
    }
}

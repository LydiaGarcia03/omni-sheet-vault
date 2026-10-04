package dev.omnisheetvault.api.storage;

import java.net.URI;
import java.net.URISyntaxException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * {@link PortraitStorage} over the S3 API: MinIO locally, any S3-compatible provider
 * elsewhere. Clients are built on first use, so a context that never touches portraits
 * (most tests) needs neither the service nor its credentials; the bucket is created on
 * the first upload if it is missing.
 */
@Component
@Profile("!desktop")
@EnableConfigurationProperties(StorageProperties.class)
class S3PortraitStorage implements PortraitStorage, DisposableBean {

    private final StorageProperties properties;
    private S3Client client;
    private S3Presigner presigner;
    private boolean bucketReady;

    S3PortraitStorage(StorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void store(String key, byte[] content, String contentType) {
        ensureBucket();
        client().putObject(request -> request.bucket(properties.bucket()).key(key).contentType(contentType), RequestBody.fromBytes(content));
    }

    @Override
    public void delete(String key) {
        client().deleteObject(request -> request.bucket(properties.bucket()).key(key));
    }

    @Override
    public URI temporaryUrl(String key) {
        var signed = presigner().presignGetObject(request -> request
                .signatureDuration(properties.urlTtl())
                .getObjectRequest(get -> get.bucket(properties.bucket()).key(key)));
        try {
            return signed.url().toURI();
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Signed portrait URL is not a valid URI", e);
        }
    }

    private synchronized void ensureBucket() {
        if (bucketReady) {
            return;
        }
        try {
            client().headBucket(request -> request.bucket(properties.bucket()));
        } catch (NoSuchBucketException e) {
            client().createBucket(request -> request.bucket(properties.bucket()));
        }
        bucketReady = true;
    }

    private synchronized S3Client client() {
        if (client == null) {
            client = S3Client.builder()
                    .endpointOverride(properties.endpoint())
                    .region(Region.of(properties.region()))
                    .credentialsProvider(credentials())
                    .forcePathStyle(true)
                    .build();
        }
        return client;
    }

    private synchronized S3Presigner presigner() {
        if (presigner == null) {
            presigner = S3Presigner.builder()
                    .endpointOverride(properties.publicEndpoint())
                    .region(Region.of(properties.region()))
                    .credentialsProvider(credentials())
                    .serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build())
                    .build();
        }
        return presigner;
    }

    private StaticCredentialsProvider credentials() {
        return StaticCredentialsProvider.create(AwsBasicCredentials.create(properties.accessKey(), properties.secretKey()));
    }

    @Override
    public synchronized void destroy() {
        if (client != null) {
            client.close();
        }
        if (presigner != null) {
            presigner.close();
        }
    }
}

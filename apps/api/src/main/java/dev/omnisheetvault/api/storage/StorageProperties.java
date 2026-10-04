package dev.omnisheetvault.api.storage;

import java.net.URI;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The S3-compatible bucket for portraits. {@code publicEndpoint} is the address browsers
 * reach, for signed URLs, when it differs from the one the API uses (e.g. a Docker
 * network name); it defaults to {@code endpoint}.
 */
@ConfigurationProperties("storage")
public record StorageProperties(
        URI endpoint, URI publicEndpoint, String bucket, String accessKey, String secretKey, String region, Duration urlTtl) {

    private static final String DEFAULT_REGION = "us-east-1";
    private static final Duration DEFAULT_URL_TTL = Duration.ofHours(1);

    public StorageProperties {
        if (publicEndpoint == null) {
            publicEndpoint = endpoint;
        }
        if (region == null || region.isBlank()) {
            region = DEFAULT_REGION;
        }
        if (urlTtl == null) {
            urlTtl = DEFAULT_URL_TTL;
        }
    }
}

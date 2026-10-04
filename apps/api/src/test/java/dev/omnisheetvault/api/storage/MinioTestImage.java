package dev.omnisheetvault.api.storage;

import org.testcontainers.utility.DockerImageName;

/** The MinIO image the storage tests run against. */
public final class MinioTestImage {

    /**
     * Chainguard's build of MinIO, pinned by digest: MinIO no longer publishes public images,
     * so {@code minio/minio} cannot be pulled on a machine that doesn't already have it.
     */
    public static final DockerImageName IMAGE = DockerImageName
            .parse("cgr.dev/chainguard/minio@sha256:4cf4831a2bbcf13ddca09c1cbcc9faff716dd3c4247e0babc32864b8ee8e0034")
            .asCompatibleSubstituteFor("minio/minio");

    private MinioTestImage() {
    }
}

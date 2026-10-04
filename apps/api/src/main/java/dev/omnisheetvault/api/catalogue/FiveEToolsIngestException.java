package dev.omnisheetvault.api.catalogue;

/** Thrown by the 5etools ingestion tool only — never reaches an HTTP response. */
public class FiveEToolsIngestException extends RuntimeException {

    public FiveEToolsIngestException(String message) {
        super(message);
    }

    public FiveEToolsIngestException(String message, Throwable cause) {
        super(message, cause);
    }
}

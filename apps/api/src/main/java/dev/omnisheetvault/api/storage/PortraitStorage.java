package dev.omnisheetvault.api.storage;

import java.net.URI;

/** Where uploaded character portraits live; objects are private and read through short-lived URLs. */
public interface PortraitStorage {

    void store(String key, byte[] content, String contentType);

    void delete(String key);

    /** A time-limited URL a browser can load without the API's token. */
    URI temporaryUrl(String key);
}

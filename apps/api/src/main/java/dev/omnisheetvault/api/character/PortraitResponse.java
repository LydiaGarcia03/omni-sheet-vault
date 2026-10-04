package dev.omnisheetvault.api.character;

import java.net.URI;

/** A character's portrait: a preset the web app bundles ({@code presetId}), or an upload behind a short-lived {@code url}. */
public record PortraitResponse(Kind kind, String presetId, String url) {

    public enum Kind { PRESET, UPLOAD }

    static PortraitResponse preset(String presetId) {
        return new PortraitResponse(Kind.PRESET, presetId, null);
    }

    static PortraitResponse upload(URI url) {
        return new PortraitResponse(Kind.UPLOAD, null, url.toString());
    }
}

package dev.omnisheetvault.api.character;

import java.util.Optional;

/** What a character's {@code portrait_key} holds: one of the web app's bundled presets, or an uploaded object's key. */
sealed interface CharacterPortrait {

    String PRESET_PREFIX = "preset:";

    String key();

    record Preset(String presetId) implements CharacterPortrait {
        @Override
        public String key() {
            return PRESET_PREFIX + presetId;
        }
    }

    record Upload(String objectKey) implements CharacterPortrait {
        @Override
        public String key() {
            return objectKey;
        }
    }

    static Optional<CharacterPortrait> fromKey(String key) {
        if (key == null || key.isBlank()) {
            return Optional.empty();
        }
        if (key.startsWith(PRESET_PREFIX)) {
            return Optional.of(new Preset(key.substring(PRESET_PREFIX.length())));
        }
        return Optional.of(new Upload(key));
    }
}

package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.storage.PortraitStorage;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

/** A character's portrait: a bundled preset or an uploaded image, owned like the character itself. */
@Service
public class CharacterPortraitService {

    private static final Logger LOG = LoggerFactory.getLogger(CharacterPortraitService.class);

    private final CharacterService characterService;
    private final PortraitStorage storage;

    CharacterPortraitService(CharacterService characterService, PortraitStorage storage) {
        this.characterService = characterService;
        this.storage = storage;
    }

    public PortraitResponse choosePreset(Jwt jwt, UUID characterId, String presetId) {
        Character character = characterService.getMine(jwt, characterId);
        replace(character, new CharacterPortrait.Preset(presetId));
        return describe(character);
    }

    public PortraitResponse upload(Jwt jwt, UUID characterId, byte[] file) {
        Character character = characterService.getMine(jwt, characterId);
        byte[] portrait = PortraitImage.normalize(file);
        String key = "uploads/" + character.id() + "/" + UUID.randomUUID() + ".png";
        storage.store(key, portrait, PortraitImage.CONTENT_TYPE);
        replace(character, new CharacterPortrait.Upload(key));
        return describe(character);
    }

    public void remove(Jwt jwt, UUID characterId) {
        replace(characterService.getMine(jwt, characterId), null);
    }

    /** The portrait as the web app shows it, or null when the character has none. */
    public PortraitResponse describe(Character character) {
        return CharacterPortrait.fromKey(character.portraitKey())
                .map(portrait -> switch (portrait) {
                    case CharacterPortrait.Preset preset -> PortraitResponse.preset(preset.presetId());
                    case CharacterPortrait.Upload upload -> PortraitResponse.upload(storage.temporaryUrl(upload.objectKey()));
                })
                .orElse(null);
    }

    private void replace(Character character, CharacterPortrait next) {
        Optional<CharacterPortrait> previous = CharacterPortrait.fromKey(character.portraitKey());
        characterService.changePortrait(character, next == null ? null : next.key());
        previous.filter(CharacterPortrait.Upload.class::isInstance)
                .filter(old -> !old.equals(next))
                .ifPresent(old -> deleteQuietly(old.key()));
    }

    /** A leftover object costs storage, not correctness, so a failed cleanup never fails the change. */
    private void deleteQuietly(String key) {
        try {
            storage.delete(key);
        } catch (RuntimeException e) {
            LOG.warn("Could not delete replaced portrait {}", key, e);
        }
    }
}

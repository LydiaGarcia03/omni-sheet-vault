package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.identity.PlayerService;
import dev.omnisheetvault.api.ruleset.registry.GameSystemRegistry;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class CharacterService {

    private final CharacterRepository characterRepository;
    private final PlayerService playerService;
    private final GameSystemRegistry gameSystemRegistry;

    public CharacterService(
            CharacterRepository characterRepository, PlayerService playerService, GameSystemRegistry gameSystemRegistry) {
        this.characterRepository = characterRepository;
        this.playerService = playerService;
        this.gameSystemRegistry = gameSystemRegistry;
    }

    public Character create(Jwt jwt, String name, String systemId) {
        gameSystemRegistry.forSystem(systemId);
        UUID playerId = playerService.currentPlayer(jwt).id();
        return characterRepository.save(Character.create(playerId, name, systemId));
    }

    public List<Character> listMine(Jwt jwt) {
        UUID playerId = playerService.currentPlayer(jwt).id();
        return characterRepository.findByPlayerIdAndDeletedAtIsNull(playerId);
    }

    public Character getMine(Jwt jwt, UUID characterId) {
        UUID playerId = playerService.currentPlayer(jwt).id();
        return characterRepository.findByIdAndPlayerIdAndDeletedAtIsNull(characterId, playerId)
                .orElseThrow(() -> new CharacterNotFoundException(characterId));
    }

    /** Like {@link #getMine}, for anything that needs a playable sheet: a draft has none yet. */
    public Character getMineActive(Jwt jwt, UUID characterId) {
        Character character = getMine(jwt, characterId);
        if (character.isDraft()) {
            throw new CharacterIsDraftException(characterId);
        }
        return character;
    }

    /** Like {@link #getMine}, for the creation flow: only a draft can still be built. */
    Character getMineDraft(Jwt jwt, UUID characterId) {
        Character character = getMine(jwt, characterId);
        if (!character.isDraft()) {
            throw new CharacterNotDraftException(characterId);
        }
        return character;
    }

    /** Renames an active character; the name is trimmed. */
    public Character rename(Jwt jwt, UUID characterId, String name) {
        Character character = getMineActive(jwt, characterId);
        character.rename(name.strip());
        return characterRepository.save(character);
    }

    public void deleteMine(Jwt jwt, UUID characterId) {
        Character character = getMine(jwt, characterId);
        character.softDelete();
        characterRepository.save(character);
    }

    void save(Character character) {
        characterRepository.save(character);
    }

    void changePortrait(Character character, String portraitKey) {
        characterRepository.updatePortraitKey(character.id(), portraitKey, Instant.now());
        character.changePortrait(portraitKey);
    }
}

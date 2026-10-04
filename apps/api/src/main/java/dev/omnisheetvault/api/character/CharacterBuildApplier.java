package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.registry.CharacterCreationFlowRegistry;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * Writes a build file's character for a player: a freshly materialized sheet plus
 * its starting equipment. A character of the same name is replaced; otherwise one is
 * created. A build with pending choices or problems is refused.
 */
@Service
class CharacterBuildApplier {

    enum Outcome { CREATED, REPLACED }

    private final CharacterRepository characterRepository;
    private final CharacterCreationFlowRegistry creationFlows;
    private final CatalogueService catalogueService;
    private final CharacterSheetService characterSheetService;
    private final ObjectMapper objectMapper;

    CharacterBuildApplier(CharacterRepository characterRepository, CharacterCreationFlowRegistry creationFlows,
            CatalogueService catalogueService, CharacterSheetService characterSheetService, ObjectMapper objectMapper) {
        this.characterRepository = characterRepository;
        this.creationFlows = creationFlows;
        this.catalogueService = catalogueService;
        this.characterSheetService = characterSheetService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    Outcome apply(UUID playerId, CharacterBuildFile file) {
        MaterializedSheet materialized = creationFlows.forSystem(file.systemId())
                .materialize(null, objectMapper.writeValueAsString(file.build()), catalogueService.lookup(file.systemId()));
        if (!materialized.isMaterialized()) {
            throw new BuildNotReadyException(file.characterName(), materialized.plan());
        }
        String sheetJson = characterSheetService.withStartingEquipment(file.systemId(), materialized.sheetJson(),
                materialized.startingItems(), materialized.startingCopper());
        int schemaVersion = objectMapper.readTree(sheetJson).path("schemaVersion").asInt();

        Character existing = characterRepository.findByPlayerIdAndDeletedAtIsNull(playerId).stream()
                .filter(character -> character.name().equals(file.characterName()))
                .findFirst()
                .orElse(null);
        Character character = existing != null ? existing : Character.create(playerId, file.characterName(), file.systemId());
        character.replaceSheet(sheetJson, schemaVersion);
        characterRepository.save(character);
        return existing != null ? Outcome.REPLACED : Outcome.CREATED;
    }
}

package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.identity.PlayerService;
import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.CreationPreview;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.StartingEquipmentPreview;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import dev.omnisheetvault.api.ruleset.registry.CharacterCreationFlowRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetCalculatorRegistry;
import java.util.UUID;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

/**
 * The creation flow's persistence: a draft character holds its build so far, is
 * re-planned on every save, and becomes a playable character once nothing is pending.
 */
@Service
class CharacterDraftService {

    /** A draft with the plan its current build produces and what it adds up to so far ({@code vitals} null until a sheet can exist). */
    record DraftView(Character character, BuildPlan plan, CreationPreview preview, VitalsZone vitals) {
    }

    private final CharacterService characterService;
    private final CharacterRepository characterRepository;
    private final PlayerService playerService;
    private final CharacterCreationFlowRegistry creationFlows;
    private final CatalogueService catalogueService;
    private final CharacterSheetService characterSheetService;
    private final SheetCalculatorRegistry sheetCalculators;
    private final ObjectMapper objectMapper;

    CharacterDraftService(CharacterService characterService, CharacterRepository characterRepository, PlayerService playerService,
            CharacterCreationFlowRegistry creationFlows, CatalogueService catalogueService,
            CharacterSheetService characterSheetService, SheetCalculatorRegistry sheetCalculators, ObjectMapper objectMapper) {
        this.characterService = characterService;
        this.characterRepository = characterRepository;
        this.playerService = playerService;
        this.creationFlows = creationFlows;
        this.catalogueService = catalogueService;
        this.characterSheetService = characterSheetService;
        this.sheetCalculators = sheetCalculators;
        this.objectMapper = objectMapper;
    }

    DraftView create(Jwt jwt, String name, String systemId) {
        CharacterCreationFlow flow = creationFlows.forSystem(systemId);
        UUID playerId = playerService.currentPlayer(jwt).id();
        Character draft = characterRepository.save(Character.createDraft(playerId, name, systemId, flow.emptyDraft()));
        return view(draft);
    }

    DraftView get(Jwt jwt, UUID characterId) {
        return view(characterService.getMineDraft(jwt, characterId));
    }

    /** Validates the build by planning it before storing, so a draft is always readable. */
    @Transactional
    DraftView save(Jwt jwt, UUID characterId, String name, String buildJson) {
        Character draft = characterService.getMineDraft(jwt, characterId);
        plan(draft.systemId(), buildJson);
        draft.saveDraft(name, buildJson);
        characterRepository.save(draft);
        return view(draft);
    }

    @Transactional
    Character finish(Jwt jwt, UUID characterId) {
        Character draft = characterService.getMineDraft(jwt, characterId);
        MaterializedSheet materialized = creationFlows.forSystem(draft.systemId())
                .materialize(null, draft.creationDraft(), catalogueService.lookup(draft.systemId()));
        if (!materialized.isMaterialized()) {
            throw new BuildNotReadyException(draft.name(), materialized.plan());
        }
        String sheetJson = characterSheetService.withStartingEquipment(draft.systemId(), materialized.sheetJson(),
                materialized.startingItems(), materialized.startingCopper());
        draft.finish(sheetJson, objectMapper.readTree(sheetJson).path("schemaVersion").asInt());
        return characterRepository.save(draft);
    }

    private DraftView view(Character draft) {
        String systemId = draft.systemId();
        CreationPreview preview = creationFlows.forSystem(systemId).preview(draft.creationDraft(), catalogueService.lookup(systemId));
        VitalsZone vitals = preview.sheetJson() == null ? null : sheetCalculators.forSystem(systemId).calculateVitals(withStartingEquipment(systemId, preview));
        return new DraftView(draft, plan(systemId, draft.creationDraft()), preview, vitals);
    }

    /** The preview sheet carrying its starting items, so the summary's armor class and attacks follow what starts equipped. */
    private String withStartingEquipment(String systemId, CreationPreview preview) {
        StartingEquipmentPreview equipment = preview.startingEquipment();
        return equipment == null || equipment.items().isEmpty()
                ? preview.sheetJson()
                : characterSheetService.withStartingEquipment(systemId, preview.sheetJson(), equipment.items(), 0);
    }

    private BuildPlan plan(String systemId, String buildJson) {
        return creationFlows.forSystem(systemId).plan(buildJson, catalogueService.lookup(systemId));
    }
}

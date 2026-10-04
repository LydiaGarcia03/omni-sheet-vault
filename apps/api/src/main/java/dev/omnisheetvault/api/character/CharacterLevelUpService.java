package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.ruleset.BuildPlan;
import dev.omnisheetvault.api.ruleset.CatalogueLookup;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.CreationPreview;
import dev.omnisheetvault.api.ruleset.LevelUp;
import dev.omnisheetvault.api.ruleset.LevelUpNotAllowedException;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import dev.omnisheetvault.api.ruleset.registry.CharacterCreationFlowRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetCalculatorRegistry;
import dev.omnisheetvault.api.ruleset.registry.SheetMutatorRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * A level up in progress on an active character: its build with one more class level, kept apart
 * from the sheet until it is finished. Finishing re-materializes the sheet (play state kept) and
 * raises current hit points by the maximum gained.
 */
@Service
class CharacterLevelUpService {

    /**
     * The level up as stored: the class leveled, the level it reaches, the build with the new level, and the
     * choices the new level left pending when it started, which stay shown once answered.
     */
    record StoredLevelUp(String classSlug, int classLevel, JsonNode build, Set<String> openedChoiceIds) {

        StoredLevelUp {
            openedChoiceIds = openedChoiceIds == null ? Set.of() : Set.copyOf(openedChoiceIds);
        }

        StoredLevelUp withBuild(JsonNode newBuild) {
            return new StoredLevelUp(classSlug, classLevel, newBuild, openedChoiceIds);
        }
    }

    /**
     * A level up with the choices the new level asks for, what the build adds up to, and the
     * character's maximum hit points before and with the new level ({@code maxHitPointsAfter}
     * null until the build can form a sheet).
     */
    record LevelUpView(Character character, StoredLevelUp stored, BuildPlan plan, CreationPreview preview, VitalsZone vitals,
            int maxHitPointsBefore, Integer maxHitPointsAfter) {
    }

    private final CharacterService characterService;
    private final CharacterRepository characterRepository;
    private final CharacterCreationFlowRegistry creationFlows;
    private final CatalogueService catalogueService;
    private final SheetCalculatorRegistry sheetCalculators;
    private final SheetMutatorRegistry sheetMutators;
    private final ObjectMapper objectMapper;

    CharacterLevelUpService(CharacterService characterService, CharacterRepository characterRepository,
            CharacterCreationFlowRegistry creationFlows, CatalogueService catalogueService, SheetCalculatorRegistry sheetCalculators,
            SheetMutatorRegistry sheetMutators, ObjectMapper objectMapper) {
        this.characterService = characterService;
        this.characterRepository = characterRepository;
        this.creationFlows = creationFlows;
        this.catalogueService = catalogueService;
        this.sheetCalculators = sheetCalculators;
        this.sheetMutators = sheetMutators;
        this.objectMapper = objectMapper;
    }

    /** Starts (or restarts) a level up in {@code classSlug}; refused when the new level brings problems the build didn't have. */
    @Transactional
    LevelUpView start(Jwt jwt, UUID characterId, String classSlug) {
        Character character = characterService.getMineActive(jwt, characterId);
        CharacterCreationFlow flow = creationFlows.forSystem(character.systemId());
        CatalogueLookup catalogue = catalogueService.lookup(character.systemId());
        String build = flow.buildOf(character.sheet())
                .orElseThrow(() -> new LevelUpNotAllowedException("This character has no build to level up."));
        LevelUp leveled = flow.withLevelUp(build, classSlug, catalogue);
        BuildPlan leveledPlan = flow.plan(leveled.buildJson(), catalogue);
        List<String> newProblems = new ArrayList<>(leveledPlan.problems());
        newProblems.removeAll(flow.plan(build, catalogue).problems());
        if (!newProblems.isEmpty()) {
            throw new LevelUpNotAllowedException(String.join("; ", newProblems));
        }
        Set<String> opened = leveledPlan.choices().stream().filter(CreationChoice::isPending).map(CreationChoice::id)
                .collect(Collectors.toSet());
        store(character, new StoredLevelUp(classSlug, leveled.classLevel(), objectMapper.readTree(leveled.buildJson()), opened));
        return view(character);
    }

    LevelUpView get(Jwt jwt, UUID characterId) {
        return view(withLevelUp(jwt, characterId));
    }

    /** Validates the build by planning it before storing, as a creation draft does. */
    @Transactional
    LevelUpView save(Jwt jwt, UUID characterId, JsonNode build) {
        Character character = withLevelUp(jwt, characterId);
        StoredLevelUp stored = stored(character);
        creationFlows.forSystem(character.systemId())
                .plan(objectMapper.writeValueAsString(build), catalogueService.lookup(character.systemId()));
        store(character, stored.withBuild(build));
        return view(character);
    }

    @Transactional
    void cancel(Jwt jwt, UUID characterId) {
        Character character = characterService.getMineActive(jwt, characterId);
        character.saveLevelUpDraft(null);
        characterRepository.save(character);
    }

    @Transactional
    Character finish(Jwt jwt, UUID characterId) {
        Character character = withLevelUp(jwt, characterId);
        String systemId = character.systemId();
        MaterializedSheet materialized = creationFlows.forSystem(systemId)
                .materialize(character.sheet(), objectMapper.writeValueAsString(stored(character).build()), catalogueService.lookup(systemId));
        if (!materialized.isMaterialized()) {
            throw new BuildNotReadyException(character.name(), materialized.plan());
        }
        int gained = maxHitPoints(systemId, materialized.sheetJson()) - maxHitPoints(systemId, character.sheet());
        String sheetJson = gained > 0 ? sheetMutators.forSystem(systemId).applyHealing(materialized.sheetJson(), gained) : materialized.sheetJson();
        character.finishLevelUp(sheetJson, objectMapper.readTree(sheetJson).path("schemaVersion").asInt());
        return characterRepository.save(character);
    }

    private Character withLevelUp(Jwt jwt, UUID characterId) {
        Character character = characterService.getMineActive(jwt, characterId);
        if (character.levelUpDraft() == null) {
            throw new LevelUpNotStartedException(character.name());
        }
        return character;
    }

    private void store(Character character, StoredLevelUp stored) {
        character.saveLevelUpDraft(objectMapper.writeValueAsString(stored));
        characterRepository.save(character);
    }

    private StoredLevelUp stored(Character character) {
        return objectMapper.readValue(character.levelUpDraft(), StoredLevelUp.class);
    }

    private LevelUpView view(Character character) {
        String systemId = character.systemId();
        StoredLevelUp stored = stored(character);
        String buildJson = objectMapper.writeValueAsString(stored.build());
        CatalogueLookup catalogue = catalogueService.lookup(systemId);
        CharacterCreationFlow flow = creationFlows.forSystem(systemId);
        CreationPreview preview = flow.preview(buildJson, catalogue);
        VitalsZone vitals = preview.sheetJson() == null ? null : sheetCalculators.forSystem(systemId).calculateVitals(preview.sheetJson());
        return new LevelUpView(character, stored, forNewLevel(flow.plan(buildJson, catalogue), stored), preview, vitals,
                maxHitPoints(systemId, character.sheet()), vitals == null ? null : vitals.hitPoints().value());
    }

    /**
     * The choices the new level asks for: those filed under the leveled class at its new level, those
     * the new level left pending when it started, and any still pending, with the choices they come
     * from and the ones they open.
     */
    static BuildPlan forNewLevel(BuildPlan plan, StoredLevelUp stored) {
        Map<String, CreationChoice> byId = new HashMap<>();
        plan.choices().forEach(choice -> byId.put(choice.id(), choice));
        Set<String> kept = new HashSet<>();
        for (CreationChoice choice : plan.choices()) {
            boolean atNewLevel = choice.placement() != null && stored.classSlug().equals(choice.placement().group())
                    && Integer.valueOf(stored.classLevel()).equals(choice.placement().level());
            if (atNewLevel || choice.isPending() || stored.openedChoiceIds().contains(choice.id())) {
                for (CreationChoice ancestor = choice; ancestor != null; ancestor = byId.get(ancestor.parentChoiceId())) {
                    kept.add(ancestor.id());
                }
            }
        }
        boolean grew = true;
        while (grew) {
            grew = plan.choices().stream()
                    .filter(choice -> choice.parentChoiceId() != null && kept.contains(choice.parentChoiceId()))
                    .map(choice -> kept.add(choice.id()))
                    .reduce(false, Boolean::logicalOr);
        }
        return new BuildPlan(plan.choices().stream().filter(choice -> kept.contains(choice.id())).toList(), plan.problems());
    }

    private int maxHitPoints(String systemId, String sheetJson) {
        return sheetCalculators.forSystem(systemId).calculateVitals(sheetJson).hitPoints().value();
    }
}

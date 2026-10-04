package dev.omnisheetvault.api.character;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.JsonNode;

@RestController
class CharacterController {

    private final CharacterService characterService;
    private final CharacterSheetService characterSheetService;
    private final CharacterPortraitService portraitService;
    private final CharacterSummaryService summaryService;

    CharacterController(
            CharacterService characterService, CharacterSheetService characterSheetService, CharacterPortraitService portraitService,
            CharacterSummaryService summaryService) {
        this.characterService = characterService;
        this.characterSheetService = characterSheetService;
        this.portraitService = portraitService;
        this.summaryService = summaryService;
    }

    @PostMapping("/api/characters")
    @ResponseStatus(HttpStatus.CREATED)
    CharacterResponse create(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody CreateCharacterRequest request) {
        return respond(characterService.create(jwt, request.name(), request.systemId()));
    }

    @GetMapping("/api/characters")
    List<CharacterResponse> listMine(@AuthenticationPrincipal Jwt jwt) {
        return characterService.listMine(jwt).stream().map(this::respond).toList();
    }

    @GetMapping("/api/characters/{id}")
    CharacterResponse getMine(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return respond(characterService.getMine(jwt, id));
    }

    @PutMapping("/api/characters/{id}/name")
    CharacterResponse rename(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody RenameCharacterRequest request) {
        return respond(characterService.rename(jwt, id, request.name()));
    }

    private CharacterResponse respond(Character character) {
        return CharacterResponse.from(character, portraitService.describe(character), summaryService.summarize(character));
    }

    @GetMapping("/api/characters/{id}/sheet")
    CharacterSheetResponse getSheet(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return CharacterSheetResponse.from(characterSheetService.getVitals(jwt, id));
    }

    @PostMapping("/api/characters/{id}/hit-points/damage")
    CharacterSheetResponse applyDamage(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody HitPointAdjustmentRequest request) {
        return CharacterSheetResponse.from(characterSheetService.applyDamage(jwt, id, request.amount(), request.isCritical()));
    }

    @PutMapping("/api/characters/{id}/appearance")
    CharacterSheetResponse setAppearance(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody AppearanceRequest request) {
        return CharacterSheetResponse.from(characterSheetService.setSheetTheme(jwt, id, request.theme()));
    }

    @PutMapping("/api/characters/{id}/customizations/{group}/{target}")
    CharacterSheetResponse customize(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String group, @PathVariable String target,
            @RequestBody JsonNode value) {
        return CharacterSheetResponse.from(characterSheetService.customize(jwt, id, group, target, value.toString()));
    }

    @DeleteMapping("/api/characters/{id}/customizations/{group}/{target}")
    CharacterSheetResponse removeCustomization(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String group, @PathVariable String target) {
        return CharacterSheetResponse.from(characterSheetService.removeCustomization(jwt, id, group, target));
    }

    @PostMapping("/api/characters/{id}/experience")
    CharacterSheetResponse changeExperience(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ExperienceRequest request) {
        return CharacterSheetResponse.from(characterSheetService.changeExperience(jwt, id, request::applyTo));
    }

    @PutMapping("/api/characters/{id}/death-saves")
    CharacterSheetResponse setDeathSaves(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody DeathSavesRequest request) {
        return CharacterSheetResponse.from(characterSheetService.setDeathSaves(jwt, id, request.successes(), request.failures()));
    }

    @DeleteMapping("/api/characters/{id}/death-saves")
    CharacterSheetResponse clearDeathSaves(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return CharacterSheetResponse.from(characterSheetService.clearDeathSaves(jwt, id));
    }

    @PostMapping("/api/characters/{id}/death-saves/roll")
    DeathSaveRollResponse rollDeathSave(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        DeathSaveRollResult result = characterSheetService.rollDeathSave(jwt, id);
        return new DeathSaveRollResponse(CharacterSheetResponse.from(result.vitals()), HitDiceRollResponse.from(result.roll()));
    }

    @PostMapping("/api/characters/{id}/hit-points/heal")
    CharacterSheetResponse applyHealing(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody HitPointAdjustmentRequest request) {
        return CharacterSheetResponse.from(characterSheetService.applyHealing(jwt, id, request.amount()));
    }

    @PostMapping("/api/characters/{id}/hit-points/temporary")
    CharacterSheetResponse setTemporaryHitPoints(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody HitPointAdjustmentRequest request) {
        return CharacterSheetResponse.from(characterSheetService.setTemporaryHitPoints(jwt, id, request.amount()));
    }

    @PostMapping("/api/characters/{id}/inspiration/toggle")
    CharacterSheetResponse toggleInspiration(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return CharacterSheetResponse.from(characterSheetService.toggleInspiration(jwt, id));
    }

    @PostMapping("/api/characters/{id}/conditions/{condition}/toggle")
    CharacterSheetResponse toggleCondition(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String condition) {
        return CharacterSheetResponse.from(characterSheetService.toggleCondition(jwt, id, condition));
    }

    @PostMapping("/api/characters/{id}/exhaustion")
    CharacterSheetResponse setExhaustionLevel(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ExhaustionLevelRequest request) {
        return CharacterSheetResponse.from(characterSheetService.setExhaustionLevel(jwt, id, request.level()));
    }

    @PostMapping("/api/characters/{id}/items")
    CharacterSheetResponse addItem(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody AddItemRequest request) {
        return CharacterSheetResponse.from(characterSheetService.addItem(
                jwt, id, request.name(), request.quantity(), request.cost(), request.notes(), request.requiresAttunement(),
                request.storageLocation()));
    }

    @PostMapping("/api/characters/{id}/items/from-catalogue")
    CharacterSheetResponse addCatalogueItem(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody AddCatalogueItemRequest request) {
        return CharacterSheetResponse.from(
                characterSheetService.addCatalogueItem(jwt, id, UUID.fromString(request.catalogueEntryId())));
    }

    @DeleteMapping("/api/characters/{id}/items/{itemKey}")
    CharacterSheetResponse removeItem(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String itemKey) {
        return CharacterSheetResponse.from(characterSheetService.removeItem(jwt, id, itemKey));
    }

    @PostMapping("/api/characters/{id}/items/{itemKey}/equip/toggle")
    CharacterSheetResponse toggleItemEquipped(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String itemKey) {
        return CharacterSheetResponse.from(characterSheetService.toggleItemEquipped(jwt, id, itemKey));
    }

    @PostMapping("/api/characters/{id}/items/{itemKey}/attune/toggle")
    CharacterSheetResponse toggleItemAttuned(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String itemKey) {
        return CharacterSheetResponse.from(characterSheetService.toggleItemAttuned(jwt, id, itemKey));
    }

    @PostMapping("/api/characters/{id}/items/{itemKey}/quantity")
    CharacterSheetResponse setItemQuantity(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String itemKey,
            @Valid @RequestBody ItemQuantityRequest request) {
        return CharacterSheetResponse.from(characterSheetService.setItemQuantity(jwt, id, itemKey, request.quantity()));
    }

    @PostMapping("/api/characters/{id}/items/{itemKey}/move")
    CharacterSheetResponse moveItem(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String itemKey,
            @Valid @RequestBody MoveItemRequest request) {
        return CharacterSheetResponse.from(characterSheetService.moveItem(jwt, id, itemKey, request.storageLocation()));
    }

    @PostMapping("/api/characters/{id}/encumbrance/tracking")
    CharacterSheetResponse setTrackEncumbrance(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody TrackEncumbranceRequest request) {
        return CharacterSheetResponse.from(characterSheetService.setTrackEncumbrance(jwt, id, request.trackEncumbrance()));
    }

    @PostMapping("/api/characters/{id}/custom-actions")
    CharacterSheetResponse addCustomAction(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody AddCustomActionRequest request) {
        return CharacterSheetResponse.from(characterSheetService.addCustomAction(jwt, id, request));
    }

    @PutMapping("/api/characters/{id}/custom-actions/{actionKey}")
    CharacterSheetResponse updateCustomAction(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String actionKey,
            @Valid @RequestBody AddCustomActionRequest request) {
        return CharacterSheetResponse.from(characterSheetService.updateCustomAction(jwt, id, actionKey, request));
    }

    @DeleteMapping("/api/characters/{id}/custom-actions/{actionKey}")
    CharacterSheetResponse removeCustomAction(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String actionKey) {
        return CharacterSheetResponse.from(characterSheetService.removeCustomAction(jwt, id, actionKey));
    }

    @PostMapping("/api/characters/{id}/coins/{denomination}")
    CharacterSheetResponse addCoins(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String denomination,
            @Valid @RequestBody CoinAdjustmentRequest request) {
        return CharacterSheetResponse.from(characterSheetService.addCoins(jwt, id, denomination, request.amount()));
    }

    @DeleteMapping("/api/characters/{id}/coins/{denomination}")
    CharacterSheetResponse removeCoins(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String denomination,
            @RequestParam int amount) {
        return CharacterSheetResponse.from(characterSheetService.removeCoins(jwt, id, denomination, amount));
    }

    @PostMapping("/api/characters/{id}/extras/{extraKey}/hit-points/damage")
    CharacterSheetResponse applyExtraDamage(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String extraKey,
            @Valid @RequestBody HitPointAdjustmentRequest request) {
        return CharacterSheetResponse.from(characterSheetService.applyExtraDamage(jwt, id, extraKey, request.amount()));
    }

    @PostMapping("/api/characters/{id}/extras/{extraKey}/hit-points/heal")
    CharacterSheetResponse applyExtraHealing(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String extraKey,
            @Valid @RequestBody HitPointAdjustmentRequest request) {
        return CharacterSheetResponse.from(characterSheetService.applyExtraHealing(jwt, id, extraKey, request.amount()));
    }

    @PostMapping("/api/characters/{id}/extras/{extraKey}/hit-points/temporary")
    CharacterSheetResponse setExtraTemporaryHitPoints(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String extraKey,
            @Valid @RequestBody HitPointAdjustmentRequest request) {
        return CharacterSheetResponse.from(
                characterSheetService.setExtraTemporaryHitPoints(jwt, id, extraKey, request.amount()));
    }

    @DeleteMapping("/api/characters/{id}/extras/{extraKey}")
    CharacterSheetResponse removeExtra(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String extraKey) {
        return CharacterSheetResponse.from(characterSheetService.removeExtra(jwt, id, extraKey));
    }

    @PostMapping("/api/characters/{id}/feature-actions/{featureKey}/use")
    CharacterSheetResponse useFeatureAction(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String featureKey) {
        return CharacterSheetResponse.from(characterSheetService.useFeatureAction(jwt, id, featureKey));
    }

    @PostMapping("/api/characters/{id}/feature-actions/{featureKey}/restore")
    CharacterSheetResponse restoreFeatureAction(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String featureKey) {
        return CharacterSheetResponse.from(characterSheetService.restoreFeatureAction(jwt, id, featureKey));
    }

    @PostMapping("/api/characters/{id}/feature-traits/{featureKey}/use")
    CharacterSheetResponse useFeatureTraitUse(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String featureKey) {
        return CharacterSheetResponse.from(characterSheetService.useFeatureTraitUse(jwt, id, featureKey));
    }

    @PostMapping("/api/characters/{id}/feature-traits/{featureKey}/restore")
    CharacterSheetResponse restoreFeatureTraitUse(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String featureKey) {
        return CharacterSheetResponse.from(characterSheetService.restoreFeatureTraitUse(jwt, id, featureKey));
    }

    @PostMapping("/api/characters/{id}/spell-slots/{level}/consume")
    CharacterSheetResponse consumeSpellSlot(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable int level,
            @RequestParam(defaultValue = "false") boolean pact) {
        return CharacterSheetResponse.from(characterSheetService.consumeSpellSlot(jwt, id, level, pact));
    }

    @PostMapping("/api/characters/{id}/spell-slots/{level}/restore")
    CharacterSheetResponse restoreSpellSlot(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable int level,
            @RequestParam(defaultValue = "false") boolean pact) {
        return CharacterSheetResponse.from(characterSheetService.restoreSpellSlot(jwt, id, level, pact));
    }

    @PostMapping("/api/characters/{id}/spells/{spellKey}/cast")
    CharacterSheetResponse castSpell(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String spellKey,
            @Valid @RequestBody CastSpellRequest request) {
        return CharacterSheetResponse.from(characterSheetService.castSpell(
                jwt, id, spellKey, request.slotLevel(), request.pactOrDefault(), request.onSelfOrDefault()));
    }

    @PostMapping("/api/characters/{id}/active-effects/{effectKey}/end")
    CharacterSheetResponse endActiveEffect(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String effectKey) {
        return CharacterSheetResponse.from(characterSheetService.endActiveEffect(jwt, id, effectKey));
    }

    @PostMapping("/api/characters/{id}/spells/{spellKey}/cast-from-item")
    CharacterSheetResponse castItemGrantedSpell(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String spellKey) {
        return CharacterSheetResponse.from(characterSheetService.castItemGrantedSpell(jwt, id, spellKey));
    }

    @PostMapping("/api/characters/{id}/rest/short")
    ShortRestResponse applyShortRest(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody ShortRestRequest request) {
        return ShortRestResponse.from(characterSheetService.applyShortRest(jwt, id, request.hitDiceSpent(), request.hitDiceBySize()));
    }

    @PostMapping("/api/characters/{id}/rest/long")
    CharacterSheetResponse applyLongRest(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody(required = false) LongRestRequest request) {
        return CharacterSheetResponse.from(
                characterSheetService.applyLongRest(jwt, id, request == null ? null : request.hitDiceRecovered()));
    }

    @PostMapping("/api/characters/{id}/spells/learn")
    CharacterSheetResponse learnSpell(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody LearnSpellRequest request) {
        return CharacterSheetResponse.from(
                characterSheetService.learnSpell(jwt, id, UUID.fromString(request.catalogueEntryId()), request.className()));
    }

    @DeleteMapping("/api/characters/{id}/spells/{spellKey}")
    CharacterSheetResponse removeSpell(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String spellKey) {
        return CharacterSheetResponse.from(characterSheetService.removeSpell(jwt, id, spellKey));
    }

    @PostMapping("/api/characters/{id}/feats/learn")
    CharacterSheetResponse learnFeat(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody LearnFeatRequest request) {
        return CharacterSheetResponse.from(
                characterSheetService.learnFeat(jwt, id, UUID.fromString(request.catalogueEntryId())));
    }

    @DeleteMapping("/api/characters/{id}/feats/{featureKey}")
    CharacterSheetResponse removeFeat(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String featureKey) {
        return CharacterSheetResponse.from(characterSheetService.removeFeat(jwt, id, featureKey));
    }

    @PostMapping("/api/characters/{id}/spells/{spellKey}/prepare")
    CharacterSheetResponse prepareSpell(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String spellKey) {
        return CharacterSheetResponse.from(characterSheetService.prepareSpell(jwt, id, spellKey));
    }

    @PostMapping("/api/characters/{id}/spells/{spellKey}/unprepare")
    CharacterSheetResponse unprepareSpell(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String spellKey) {
        return CharacterSheetResponse.from(characterSheetService.unprepareSpell(jwt, id, spellKey));
    }

    @PostMapping("/api/characters/{id}/background/{field}")
    CharacterSheetResponse updateBackgroundField(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @PathVariable String field,
            @Valid @RequestBody UpdateBackgroundFieldRequest request) {
        return CharacterSheetResponse.from(characterSheetService.updateBackgroundField(jwt, id, field, request.value()));
    }

    @PostMapping("/api/characters/{id}/hit-dice/spend")
    SpendHitDiceResponse spendHitDice(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID id, @Valid @RequestBody SpendHitDiceRequest request) {
        HitDiceSpendResult result = characterSheetService.spendHitDice(jwt, id, request.count(), request.dieSize());
        return new SpendHitDiceResponse(CharacterSheetResponse.from(result.vitals()), HitDiceRollResponse.from(result.roll()));
    }

    @DeleteMapping("/api/characters/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteMine(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        characterService.deleteMine(jwt, id);
    }
}

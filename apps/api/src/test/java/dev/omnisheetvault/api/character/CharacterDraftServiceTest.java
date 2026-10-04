package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.catalogue.CatalogueImportService;
import dev.omnisheetvault.api.ruleset.CreationChoice;
import dev.omnisheetvault.api.ruleset.InvalidBuildException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/** Synthetic catalogue fixtures (see CatalogueImportServiceTest's note), planned and materialized from Postgres. */
@SpringBootTest
@Testcontainers
@Transactional
class CharacterDraftServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CharacterDraftService draftService;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private CharacterSheetService characterSheetService;

    @Autowired
    private CatalogueImportService catalogueImportService;

    @Autowired
    private CharacterLevelUpService levelUpService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Jwt jwt;

    @BeforeEach
    void importCatalogueAndPlayer() throws IOException {
        Path directory = Files.createTempDirectory("draft-service-test");
        write(directory, "SPECIES", "human", "Human", """
                {"size":["medium"],"speed":{"walk":30},"abilityAlternatives":[],"subspecies":[],"variants":[]}""");
        write(directory, "BACKGROUND", "acolyte", "Acolyte", """
                {"skillAlternatives":[{"fixed":["insight"],"choices":[]}],
                 "features":[{"name":"Shelter of the Faithful","description":"You command respect."}],
                 "startingEquipment":[{"options":[{"label":null,"grants":[
                   {"itemSlug":"shovel","equipmentType":null,"special":null,"valueCp":null,"quantity":1,"displayName":null,"containsValueCp":null,"worthValueCp":null}]}]}]}""");
        write(directory, "CLASS", "fighter", "Fighter", """
                {"hitDie":10,"savingThrows":["strength","constitution"],"subclassTitle":"Martial Archetype","subclassLevel":3,
                 "abilityScoreImprovementLevels":[4],
                 "proficiencies":{"armor":["light"],"weaponCategories":["simple"],"weaponItems":[],
                   "skillAlternatives":[{"fixed":[],"choices":[{"from":["athletics","perception"],"category":null,"fromFilter":null,"count":1,"amount":null}]}],
                   "toolAlternatives":[]},
                 "startingEquipment":{"goldAlternative":null,"groups":[]},
                 "multiclassing":{"allOf":{"strength":13},"anyOf":{},"proficienciesGained":{}},
                 "spellcasting":null,"optionalFeatureProgressions":[],"features":[]}""");
        write(directory, "ITEM", "shovel", "Shovel", """
                {"itemKind":"GEAR","typeLabel":"Adventuring Gear","rarity":"none","focusType":null,"requiresAttunement":false,
                 "attunementRequirement":null,"weightLb":5.0,"costGp":2.0,"weaponCategory":null,"attackType":null,
                 "damageDiceCount":null,"damageDiceSides":null,"damageType":null,"versatileDamageDiceCount":null,
                 "versatileDamageDiceSides":null,"properties":[],"finesse":false,"normalRange":null,"longRange":null,
                 "armorCategory":null,"baseArmorClass":null,"stealthDisadvantage":false,"strengthRequirement":null,
                 "weaponAttackBonus":null,"weaponDamageBonus":null,"armorClassBonus":null,"charges":null,
                 "rechargeTrigger":null,"rechargeFormula":null,"grantedSpells":[]}""");
        catalogueImportService.importFrom(directory);

        jwt = Jwt.withTokenValue("token").header("alg", "none").subject("draft-service-" + UUID.randomUUID())
                .claim("preferred_username", "drafter").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
    }

    @Test
    void aNewDraftAsksForItsStructureAndHasNoSheetYet() {
        CharacterDraftService.DraftView draft = draftService.create(jwt, "Thorin", "dnd-5e");

        assertThat(draft.character().status()).isEqualTo(CharacterStatus.DRAFT);
        assertThat(draft.plan().pending()).extracting(CreationChoice::id)
                .containsExactly("build.abilityScores", "build.species", "build.background", "build.classes");
        assertThat(characterService.listMine(jwt)).extracting(Character::status).containsExactly(CharacterStatus.DRAFT);
        assertThatThrownBy(() -> characterSheetService.getVitals(jwt, draft.character().id()))
                .isInstanceOf(CharacterIsDraftException.class);
    }

    @Test
    void savingReplansAndKeepsTheNewName() {
        UUID id = draftService.create(jwt, "Thorin", "dnd-5e").character().id();

        CharacterDraftService.DraftView saved = draftService.save(jwt, id, "Thorin Stonehelm", build(false));

        assertThat(saved.character().name()).isEqualTo("Thorin Stonehelm");
        assertThat(saved.plan().pending()).extracting(CreationChoice::id).containsExactly("class:fighter:skills:0");
        assertThat(objectMapper.readTree(draftService.get(jwt, id).character().creationDraft()).path("speciesSlug").asString())
                .isEqualTo("human");
    }

    @Test
    void anEmptyDraftPreviewsItsScoresButNoSheetYet() {
        CharacterDraftService.DraftView draft = draftService.create(jwt, "Thorin", "dnd-5e");

        assertThat(draft.preview().abilityScores()).containsOnlyKeys(
                "strength", "dexterity", "constitution", "intelligence", "wisdom", "charisma");
        assertThat(draft.preview().sheetJson()).isNull();
        assertThat(draft.preview().progressions()).isEmpty();
        assertThat(draft.vitals()).isNull();
    }

    @Test
    void aDraftWithAClassAndScoresPreviewsItsSheetBeforeEveryChoiceIsMade() {
        UUID id = draftService.create(jwt, "Thorin", "dnd-5e").character().id();

        CharacterDraftService.DraftView saved = draftService.save(jwt, id, "Thorin", build(false));

        assertThat(saved.plan().pending()).isNotEmpty();
        assertThat(saved.preview().abilityScores().get("strength").value()).isEqualTo(15);
        assertThat(saved.preview().abilityScores().get("strength").contributions()).extracting(c -> c.source()).containsExactly("Base");
        assertThat(saved.vitals()).isNotNull();
        assertThat(saved.vitals().level()).isEqualTo(1);
        assertThat(saved.vitals().hitPoints().value()).isEqualTo(12);
        assertThat(saved.vitals().savingThrowProficiencies()).containsEntry("strength", "FULL");
    }

    @Test
    void refusesAnUnreadableBuildAndKeepsTheLastGoodOne() {
        UUID id = draftService.create(jwt, "Thorin", "dnd-5e").character().id();

        assertThatThrownBy(() -> draftService.save(jwt, id, "Thorin", "{\"baseAbilityScores\":{\"luck\":12}}"))
                .isInstanceOf(InvalidBuildException.class);
        assertThat(draftService.get(jwt, id).character().creationDraft()).doesNotContain("luck");
    }

    @Test
    void refusesToFinishWhileChoicesArePending() {
        UUID id = draftService.create(jwt, "Thorin", "dnd-5e").character().id();
        draftService.save(jwt, id, "Thorin", build(false));

        assertThatThrownBy(() -> draftService.finish(jwt, id))
                .isInstanceOf(BuildNotReadyException.class)
                .hasMessageContaining("class:fighter:skills:0");
        assertThat(characterService.getMine(jwt, id).isDraft()).isTrue();
    }

    @Test
    void finishingMaterializesTheSheetWithItsStartingEquipment() {
        UUID id = draftService.create(jwt, "Thorin", "dnd-5e").character().id();
        draftService.save(jwt, id, "Thorin", build(true));

        Character finished = draftService.finish(jwt, id);

        JsonNode sheet = objectMapper.readTree(finished.sheet());
        assertThat(finished.status()).isEqualTo(CharacterStatus.ACTIVE);
        assertThat(finished.creationDraft()).isNull();
        assertThat(sheet.path("level").asInt()).isEqualTo(1);
        assertThat(sheet.path("items")).extracting(item -> item.path("name").asString()).containsExactly("Shovel");
        assertThat(characterSheetService.getVitals(jwt, id)).isNotNull();
        assertThatThrownBy(() -> draftService.save(jwt, id, "Thorin", build(true))).isInstanceOf(CharacterNotDraftException.class);
    }

    @Test
    void levelingUpAddsTheLevelKeepsPlayStateAndRaisesCurrentHitPointsByTheMaximumGained() {
        UUID id = draftService.create(jwt, "Thorin", "dnd-5e").character().id();
        draftService.save(jwt, id, "Thorin", build(true));
        draftService.finish(jwt, id);
        characterSheetService.applyDamage(jwt, id, 5);
        characterSheetService.changeExperience(jwt, id, points -> 300);
        characterSheetService.setSheetTheme(jwt, id, "cleric-silver");

        CharacterLevelUpService.LevelUpView started = levelUpService.start(jwt, id, "fighter");
        assertThat(started.stored().classLevel()).isEqualTo(2);
        assertThat(started.maxHitPointsBefore()).isEqualTo(12);
        assertThat(started.maxHitPointsAfter()).isEqualTo(20);
        assertThat(characterSheetService.getVitals(jwt, id).level()).isEqualTo(1);

        levelUpService.finish(jwt, id);

        var vitals = characterSheetService.getVitals(jwt, id);
        assertThat(vitals.level()).isEqualTo(2);
        assertThat(vitals.hitPoints().value()).isEqualTo(20);
        assertThat(vitals.currentHitPoints()).isEqualTo(15);
        assertThat(vitals.experience().points()).isEqualTo(300);
        assertThat(vitals.sheetTheme()).isEqualTo("cleric-silver");
        assertThat(characterService.getMine(jwt, id).levelUpDraft()).isNull();
    }

    @Test
    void aCancelledLevelUpLeavesTheSheetAsItWas() {
        UUID id = draftService.create(jwt, "Thorin", "dnd-5e").character().id();
        draftService.save(jwt, id, "Thorin", build(true));
        draftService.finish(jwt, id);
        levelUpService.start(jwt, id, "fighter");

        levelUpService.cancel(jwt, id);

        assertThat(characterSheetService.getVitals(jwt, id).level()).isEqualTo(1);
        assertThatThrownBy(() -> levelUpService.get(jwt, id)).isInstanceOf(LevelUpNotStartedException.class);
    }

    private String build(boolean answered) {
        String choices = answered ? "[{\"id\":\"class:fighter:skills:0\",\"selections\":[\"athletics\"]}]" : "[]";
        return """
                {"speciesSlug":"human","subspeciesName":null,"speciesVariantName":null,"backgroundSlug":"acolyte",
                 "classes":[{"classSlug":"fighter","subclassSlug":null,"level":1}],"abilityScoreMethod":"MANUAL",
                 "baseAbilityScores":{"strength":15,"dexterity":12,"constitution":14,"intelligence":10,"wisdom":10,"charisma":8},
                 "hitPointMethod":"FIXED","rolledHitPoints":[],"choices":%s}""".formatted(choices);
    }

    private static void write(Path directory, String kind, String slug, String name, String data) throws IOException {
        Files.writeString(directory.resolve(kind.toLowerCase() + "-" + slug + ".json"), """
                {"systemId":"dnd-5e","kind":"%s","slug":"%s","name":"%s","sourceBook":"Test Fixtures","sourcePage":1,
                 "tags":[],"description":"","data":%s}""".formatted(kind, slug, name, data));
    }
}

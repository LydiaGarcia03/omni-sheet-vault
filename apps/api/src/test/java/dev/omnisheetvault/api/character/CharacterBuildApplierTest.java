package dev.omnisheetvault.api.character;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import dev.omnisheetvault.api.catalogue.CatalogueImportService;
import dev.omnisheetvault.api.identity.PlayerService;
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

/** Synthetic catalogue fixtures (see CatalogueImportServiceTest's note), materialized from Postgres. */
@SpringBootTest
@Testcontainers
@Transactional
class CharacterBuildApplierTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CharacterBuildApplier applier;

    @Autowired
    private CharacterService characterService;

    @Autowired
    private PlayerService playerService;

    @Autowired
    private CatalogueImportService catalogueImportService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private Jwt jwt;
    private UUID playerId;

    @BeforeEach
    void importCatalogueAndPlayer() throws IOException {
        Path directory = Files.createTempDirectory("build-applier-test");
        write(directory, "SPECIES", "human", "Human", """
                {"size":["medium"],"speed":{"walk":30},"abilityAlternatives":[],"subspecies":[],"variants":[]}""");
        write(directory, "BACKGROUND", "acolyte", "Acolyte", """
                {"skillAlternatives":[{"fixed":["insight"],"choices":[]}],
                 "features":[{"name":"Shelter of the Faithful","description":"You command respect."}],
                 "startingEquipment":[{"options":[{"label":null,"grants":[
                   {"itemSlug":"shovel","equipmentType":null,"special":null,"valueCp":null,"quantity":2,"displayName":null,"containsValueCp":null,"worthValueCp":null},
                   {"itemSlug":null,"equipmentType":null,"special":"a holy symbol","valueCp":null,"quantity":1,"displayName":null,"containsValueCp":null,"worthValueCp":null},
                   {"itemSlug":"pouch","equipmentType":null,"special":null,"valueCp":null,"quantity":1,"displayName":null,"containsValueCp":1575,"worthValueCp":null}]}]}]}""");
        write(directory, "CLASS", "fighter", "Fighter", """
                {"hitDie":10,"savingThrows":["strength","constitution"],"subclassTitle":"Martial Archetype","subclassLevel":3,
                 "abilityScoreImprovementLevels":[4],
                 "proficiencies":{"armor":["light"],"weaponCategories":["simple"],"weaponItems":[],
                   "skillAlternatives":[{"fixed":[],"choices":[{"from":["athletics","perception"],"category":null,"fromFilter":null,"count":1,"amount":null}]}],
                   "toolAlternatives":[]},
                 "startingEquipment":{"goldAlternative":null,"groups":[]},
                 "multiclassing":{"allOf":{"strength":13},"anyOf":{},"proficienciesGained":{}},
                 "spellcasting":null,"optionalFeatureProgressions":[],"features":[]}""");
        write(directory, "ITEM", "shovel", "Shovel", item());
        write(directory, "ITEM", "pouch", "Pouch", item());
        catalogueImportService.importFrom(directory);

        jwt = Jwt.withTokenValue("token").header("alg", "none").subject("build-applier-" + UUID.randomUUID())
                .claim("preferred_username", "builder").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        playerId = playerService.currentPlayer(jwt).id();
    }

    @Test
    void createsTheCharacterWithItsStartingEquipmentAndCoins() {
        CharacterBuildApplier.Outcome outcome = applier.apply(playerId, buildFile("Aria Emberfall", true));

        Character character = characterService.listMine(jwt).getFirst();
        JsonNode sheet = objectMapper.readTree(character.sheet());
        assertThat(outcome).isEqualTo(CharacterBuildApplier.Outcome.CREATED);
        assertThat(character.name()).isEqualTo("Aria Emberfall");
        assertThat(sheet.path("level").asInt()).isEqualTo(1);
        assertThat(sheet.path("items")).extracting(item -> item.path("name").asString() + " x" + item.path("quantity").asInt())
                .containsExactly("Shovel x2", "a holy symbol x1", "Pouch x1");
        assertThat(sheet.path("goldPieces").asInt()).isEqualTo(15);
        assertThat(sheet.path("silverPieces").asInt()).isEqualTo(7);
        assertThat(sheet.path("copperPieces").asInt()).isEqualTo(5);
    }

    @Test
    void replacesACharacterOfTheSameNameFromScratch() {
        applier.apply(playerId, buildFile("Aria Emberfall", true));

        CharacterBuildApplier.Outcome outcome = applier.apply(playerId, buildFile("Aria Emberfall", true));

        assertThat(outcome).isEqualTo(CharacterBuildApplier.Outcome.REPLACED);
        assertThat(characterService.listMine(jwt)).hasSize(1);
        assertThat(objectMapper.readTree(characterService.listMine(jwt).getFirst().sheet()).path("items")).hasSize(3);
    }

    @Test
    void refusesABuildWithPendingChoices() {
        assertThatThrownBy(() -> applier.apply(playerId, buildFile("Unfinished", false)))
                .isInstanceOf(BuildNotReadyException.class)
                .hasMessageContaining("class:fighter:skills:0");
        assertThat(characterService.listMine(jwt)).isEmpty();
    }

    private CharacterBuildFile buildFile(String name, boolean answered) {
        String choices = answered ? "[{\"id\":\"class:fighter:skills:0\",\"selections\":[\"athletics\"]}]" : "[]";
        JsonNode build = objectMapper.readTree("""
                {"speciesSlug":"human","subspeciesName":null,"speciesVariantName":null,"backgroundSlug":"acolyte",
                 "classes":[{"classSlug":"fighter","subclassSlug":null,"level":1}],"abilityScoreMethod":"MANUAL",
                 "baseAbilityScores":{"strength":15,"dexterity":12,"constitution":14,"intelligence":10,"wisdom":10,"charisma":8},
                 "hitPointMethod":"FIXED","rolledHitPoints":[],"choices":%s}""".formatted(choices));
        return new CharacterBuildFile(name, "dnd-5e", build);
    }

    private static String item() {
        return """
                {"itemKind":"GEAR","typeLabel":"Adventuring Gear","rarity":"none","focusType":null,"requiresAttunement":false,
                 "attunementRequirement":null,"weightLb":1.0,"costGp":1.0,"weaponCategory":null,"attackType":null,
                 "damageDiceCount":null,"damageDiceSides":null,"damageType":null,"versatileDamageDiceCount":null,
                 "versatileDamageDiceSides":null,"properties":[],"finesse":false,"normalRange":null,"longRange":null,
                 "armorCategory":null,"baseArmorClass":null,"stealthDisadvantage":false,"strengthRequirement":null,
                 "weaponAttackBonus":null,"weaponDamageBonus":null,"armorClassBonus":null,"charges":null,
                 "rechargeTrigger":null,"rechargeFormula":null,"grantedSpells":[]}""";
    }

    private static void write(Path directory, String kind, String slug, String name, String data) throws IOException {
        Files.writeString(directory.resolve(kind.toLowerCase() + "-" + slug + ".json"), """
                {"systemId":"dnd-5e","kind":"%s","slug":"%s","name":"%s","sourceBook":"Test Fixtures","sourcePage":1,
                 "tags":[],"description":"","data":%s}""".formatted(kind, slug, name, data));
    }
}

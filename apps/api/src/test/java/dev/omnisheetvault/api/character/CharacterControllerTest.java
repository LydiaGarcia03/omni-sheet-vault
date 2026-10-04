package dev.omnisheetvault.api.character;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.ruleset.AttackRow;
import dev.omnisheetvault.api.ruleset.AttunementLimitExceededException;
import dev.omnisheetvault.api.ruleset.AttunementNotAllowedException;
import dev.omnisheetvault.api.ruleset.Background;
import dev.omnisheetvault.api.ruleset.CalculatedValue;
import dev.omnisheetvault.api.ruleset.Coins;
import dev.omnisheetvault.api.ruleset.Contribution;
import dev.omnisheetvault.api.ruleset.Encumbrance;
import dev.omnisheetvault.api.ruleset.Extra;
import dev.omnisheetvault.api.ruleset.ExtraStatBlock;
import dev.omnisheetvault.api.ruleset.FeatureAction;
import dev.omnisheetvault.api.ruleset.FeatureTrait;
import dev.omnisheetvault.api.ruleset.HitDice;
import dev.omnisheetvault.api.ruleset.SpellSlotLevel;
import dev.omnisheetvault.api.ruleset.InsufficientHitDiceException;
import dev.omnisheetvault.api.ruleset.InvalidHitDiceRecoveryException;
import dev.omnisheetvault.api.ruleset.InvalidCoinDenominationException;
import dev.omnisheetvault.api.ruleset.InvalidConditionException;
import dev.omnisheetvault.api.ruleset.InvalidCustomizationException;
import dev.omnisheetvault.api.ruleset.Item;
import dev.omnisheetvault.api.ruleset.SpecialSense;
import dev.omnisheetvault.api.ruleset.Spell;
import dev.omnisheetvault.api.ruleset.SpellcastingClassInfo;
import dev.omnisheetvault.api.ruleset.UnsupportedGameSystemException;
import dev.omnisheetvault.api.ruleset.VitalsZone;
import dev.omnisheetvault.api.shared.SecurityConfig;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CharacterController.class)
@Import(SecurityConfig.class)
class CharacterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CharacterService characterService;

    @MockitoBean
    private CharacterSheetService characterSheetService;

    @MockitoBean
    private CharacterPortraitService characterPortraitService;

    @MockitoBean
    private CharacterSummaryService characterSummaryService;

    @Test
    void createsACharacter() throws Exception {
        Character character = Character.create(UUID.randomUUID(), "Aria", "dnd-5e");
        when(characterService.create(any(Jwt.class), eq("Aria"), eq("dnd-5e"))).thenReturn(character);

        mockMvc.perform(post("/api/characters")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCharacterRequest("Aria", "dnd-5e"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(character.id().toString()))
                .andExpect(jsonPath("$.name").value("Aria"))
                .andExpect(jsonPath("$.systemId").value("dnd-5e"));
    }

    @Test
    void rejectsACharacterWithABlankName() throws Exception {
        mockMvc.perform(post("/api/characters")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCharacterRequest(" ", "dnd-5e"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void renamesACharacter() throws Exception {
        Character character = Character.create(UUID.randomUUID(), "Vex the Bold", "dnd-5e");
        when(characterService.rename(any(Jwt.class), eq(character.id()), eq("Vex the Bold"))).thenReturn(character);

        mockMvc.perform(put("/api/characters/" + character.id() + "/name")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Vex the Bold\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Vex the Bold"));
    }

    @Test
    void rejectsABlankOrTooLongName() throws Exception {
        for (String name : new String[] {" ", "x".repeat(129)}) {
            mockMvc.perform(put("/api/characters/" + UUID.randomUUID() + "/name")
                            .with(jwt())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new RenameCharacterRequest(name))))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    void listsTheCallersCharacters() throws Exception {
        Character character = Character.create(UUID.randomUUID(), "Aria", "dnd-5e");
        when(characterService.listMine(any(Jwt.class))).thenReturn(List.of(character));

        mockMvc.perform(get("/api/characters").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Aria"));
    }

    @Test
    void returnsNotFoundForAMissingCharacter() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(characterService.getMine(any(Jwt.class), eq(missingId))).thenThrow(new CharacterNotFoundException(missingId));

        mockMvc.perform(get("/api/characters/" + missingId).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsACharacterWithAnUnknownSystem() throws Exception {
        when(characterService.create(any(Jwt.class), eq("Fenn"), eq("not-a-real-system")))
                .thenThrow(new UnsupportedGameSystemException("not-a-real-system"));

        mockMvc.perform(post("/api/characters")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CreateCharacterRequest("Fenn", "not-a-real-system"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsTheCharacterSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        CalculatedValue armorClass = new CalculatedValue(12, List.of(new Contribution("Base (unarmored)", 10),
                new Contribution("Dexterity modifier", 2)));
        VitalsZone vitals = new VitalsZone(
                Map.of("dexterity", 14),
                Map.of("dexterity", new CalculatedValue(2, List.of(new Contribution("Dexterity score 14", 2)))),
                new CalculatedValue(3, List.of(new Contribution("Level 5", 3))),
                armorClass,
                new CalculatedValue(2, List.of(new Contribution("Dexterity modifier", 2))),
                new CalculatedValue(44, List.of(new Contribution("Hit die (level 1)", 10))),
                30,
                5,
                Map.of("strength", new CalculatedValue(6, List.of(new Contribution("Strength modifier", 3),
                        new Contribution("Proficiency bonus", 3)))),
                Map.of("strength", "FULL", "dexterity", "NONE"),
                Map.of("passivePerception", new CalculatedValue(11, List.of(new Contribution("Base", 10),
                        new Contribution("Wisdom modifier", 1)))),
                List.of("Light", "Medium", "Heavy", "Shields"),
                List.of("Simple", "Martial"),
                List.of(),
                List.of("Common"),
                Map.of("athletics", new CalculatedValue(6, List.of(new Contribution("Strength modifier", 3),
                        new Contribution("Proficiency bonus", 3)))),
                Map.of("athletics", "FULL", "stealth", "NONE"),
                Map.of("athletics", "strength", "stealth", "dexterity"),
                List.of("Poison"),
                List.of(),
                List.of(),
                List.of(),
                Set.of("prone"),
                0,
                30,
                5,
                0,
                true,
                Map.of("longsword", new AttackRow("Longsword", "5 ft",
                        new CalculatedValue(6, List.of(new Contribution("Strength modifier", 3),
                                new Contribution("Proficiency bonus", 3))),
                        1, 8, 3, "slashing", "Melee Weapon", "", "ACTION")),
                List.of(new FeatureAction("secondWind", "Second Wind", "BONUS_ACTION",
                        "Regain 1d10 + fighter level hit points.", 1, 0, "SHORT_OR_LONG_REST")),
                List.of(new SpellcastingClassInfo("Fighter", "intelligence",
                        new CalculatedValue(0, List.of(new Contribution("Intelligence modifier", 0))),
                        new CalculatedValue(3, List.of(new Contribution("Intelligence modifier", 0),
                                new Contribution("Proficiency bonus", 3))),
                        new CalculatedValue(11, List.of(new Contribution("Base", 8),
                                new Contribution("Intelligence modifier", 0),
                                new Contribution("Proficiency bonus", 3))),
                        "KNOWN", 2, 4, null)),
                List.of(new Spell(
                        "fireBolt", "Fire Bolt", "Fighter", 0, "evocation", "1 Action", "120 feet", false, false,
                        true, 1, 10, "fire", "V, S", "Damage", false, false, "", null, "V, S", null, "Instantaneous",
                        null, null, null)),
                List.of(new Item("longsword-item", "Longsword", 1, "15 gp", "", true, false, false)),
                new Coins(0, 0, 0, 45, 0),
                List.of(new FeatureTrait(
                        "extraAttack", "Extra Attack", "CLASS_FEATURE", "Fighter",
                        "Attack twice, instead of once, whenever you take the Attack action.",
                        "Attack twice, instead of once, whenever you take the Attack action.", null, 0, null)),
                new Background(
                        "Soldier", "Military Rank", "Soldiers loyal to your former military organization still recognize your authority.",
                        "Lawful Good", "I face problems head-on.", "I fight for those who cannot fight for themselves.",
                        "My honor is my life.", "I have little respect for anyone who is not a proven warrior.",
                        "Weathered and scarred.", "The King's Army", "Sergeant Kova", "The Crimson Blades",
                        "Grew up in a border town.", "", "", "", "", "", "", "", "", "", "", ""),
                List.of(new Extra("warhorseExtra", "Warhorse", "MOUNT", 11, 19, 12, 0, 60,
                        new ExtraStatBlock(
                                "Large", "Beast", "Unaligned", 1, "3d10 + 3", null, List.of(), List.of(),
                                "Passive Perception 11", "--", "1/2 (XP 100; PB +2)", List.of(), List.of()))),
                new HitDice(10, 5, 2), List.of(new SpellSlotLevel(1, 3, 2)),
                List.of(new SpecialSense("DARKVISION", 60, "Darkvision 60 ft.")), List.of(),
                new Encumbrance(false, 0, 0, false, List.of()));
        when(characterSheetService.getVitals(any(Jwt.class), eq(characterId))).thenReturn(vitals);

        mockMvc.perform(get("/api/characters/" + characterId + "/sheet").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.armorClass.value").value(12))
                .andExpect(jsonPath("$.armorClass.contributions[0].source").value("Base (unarmored)"))
                .andExpect(jsonPath("$.hitPoints.value").value(44))
                .andExpect(jsonPath("$.abilityModifiers.dexterity.value").value(2))
                .andExpect(jsonPath("$.abilityScores.dexterity").value(14))
                .andExpect(jsonPath("$.speed").value(30))
                .andExpect(jsonPath("$.savingThrows.strength.value").value(6))
                .andExpect(jsonPath("$.savingThrowProficiencies.strength").value("FULL"))
                .andExpect(jsonPath("$.senses.passivePerception.value").value(11))
                .andExpect(jsonPath("$.specialSenses[0].label").value("Darkvision 60 ft."))
                .andExpect(jsonPath("$.armorProficiencies[0]").value("Light"))
                .andExpect(jsonPath("$.skills.athletics.value").value(6))
                .andExpect(jsonPath("$.skillProficiencies.athletics").value("FULL"))
                .andExpect(jsonPath("$.skillGoverningAbilities.athletics").value("strength"))
                .andExpect(jsonPath("$.damageResistances[0]").value("Poison"))
                .andExpect(jsonPath("$.activeConditions[0]").value("prone"))
                .andExpect(jsonPath("$.currentHitPoints").value(30))
                .andExpect(jsonPath("$.temporaryHitPoints").value(5))
                .andExpect(jsonPath("$.heroicInspiration").value(true))
                .andExpect(jsonPath("$.attacks.longsword.toHit.value").value(6))
                .andExpect(jsonPath("$.attacks.longsword.damageDiceSides").value(8))
                .andExpect(jsonPath("$.attacks.longsword.category").value("Melee Weapon"))
                .andExpect(jsonPath("$.attacks.longsword.actionType").value("ACTION"))
                .andExpect(jsonPath("$.featureActions[0].name").value("Second Wind"))
                .andExpect(jsonPath("$.featureActions[0].maxUses").value(1))
                .andExpect(jsonPath("$.featureActions[0].rechargeTrigger").value("SHORT_OR_LONG_REST"))
                .andExpect(jsonPath("$.spellcasting[0].className").value("Fighter"))
                .andExpect(jsonPath("$.spellcasting[0].spellSaveDc.value").value(11))
                .andExpect(jsonPath("$.spells[0].name").value("Fire Bolt"))
                .andExpect(jsonPath("$.spells[0].level").value(0))
                .andExpect(jsonPath("$.spells[0].attackRoll").value(true))
                .andExpect(jsonPath("$.spells[0].damageDiceCount").value(1))
                .andExpect(jsonPath("$.spells[0].damageDiceSides").value(10))
                .andExpect(jsonPath("$.spells[0].damageType").value("fire"))
                .andExpect(jsonPath("$.items[0].name").value("Longsword"))
                .andExpect(jsonPath("$.items[0].equipped").value(true))
                .andExpect(jsonPath("$.coins.gold").value(45))
                .andExpect(jsonPath("$.featureTraits[0].name").value("Extra Attack"))
                .andExpect(jsonPath("$.featureTraits[0].source").value("Fighter"))
                .andExpect(jsonPath("$.background.name").value("Soldier"))
                .andExpect(jsonPath("$.background.featureName").value("Military Rank"))
                .andExpect(jsonPath("$.background.alignment").value("Lawful Good"))
                .andExpect(jsonPath("$.extras[0].name").value("Warhorse"))
                .andExpect(jsonPath("$.extras[0].category").value("MOUNT"))
                .andExpect(jsonPath("$.extras[0].currentHitPoints").value(12))
                .andExpect(jsonPath("$.hitDice.dieSize").value(10))
                .andExpect(jsonPath("$.hitDice.max").value(5))
                .andExpect(jsonPath("$.hitDice.used").value(2));
    }

    @Test
    void appliesDamageAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(27, 0, false, Set.of());
        when(characterSheetService.applyDamage(any(Jwt.class), eq(characterId), eq(8), eq(false))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-points/damage")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentHitPoints").value(27))
                .andExpect(jsonPath("$.deathSaves.dying").value(false));
    }

    @Test
    void passesACriticalHitToTheDamage() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.applyDamage(any(Jwt.class), eq(characterId), eq(8), eq(true)))
                .thenReturn(vitalsWithSessionState(0, 0, false, Set.of()));

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-points/damage")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":8,\"critical\":true}"))
                .andExpect(status().isOk());
    }

    @Test
    void passesACustomizationToTheSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.customize(any(Jwt.class), eq(characterId), eq("abilities"), eq("strength"), eq("{\"otherModifier\":2}")))
                .thenReturn(vitalsWithSessionState(10, 0, false, Set.of()));

        mockMvc.perform(put("/api/characters/" + characterId + "/customizations/abilities/strength")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"otherModifier\":2}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsACustomizationTheSystemDoesNotOffer() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.customize(any(Jwt.class), eq(characterId), eq("luck"), eq("strength"), any()))
                .thenThrow(new InvalidCustomizationException("Unknown customization group: \"luck\""));

        mockMvc.perform(put("/api/characters/" + characterId + "/customizations/luck/strength")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAnExperienceChangeThatIsNotExactlyOneOfAddRemoveOrSet() throws Exception {
        mockMvc.perform(post("/api/characters/" + UUID.randomUUID() + "/experience")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\":100,\"set\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addsExperiencePoints() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.changeExperience(any(Jwt.class), eq(characterId), any()))
                .thenReturn(vitalsWithSessionState(10, 0, false, Set.of()));

        mockMvc.perform(post("/api/characters/" + characterId + "/experience")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"add\":300}"))
                .andExpect(status().isOk());
    }

    @Test
    void setsTheDeathSavesByHand() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.setDeathSaves(any(Jwt.class), eq(characterId), eq(2), eq(1)))
                .thenReturn(vitalsWithSessionState(0, 0, false, Set.of()));

        mockMvc.perform(put("/api/characters/" + characterId + "/death-saves")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successes\":2,\"failures\":1}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsMoreThanThreeDeathSaves() throws Exception {
        mockMvc.perform(put("/api/characters/" + UUID.randomUUID() + "/death-saves")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"successes\":4,\"failures\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refusesADeathSaveForACharacterWhoIsNotDying() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.rollDeathSave(any(Jwt.class), eq(characterId))).thenThrow(new NotDyingException());

        mockMvc.perform(post("/api/characters/" + characterId + "/death-saves/roll").with(jwt()))
                .andExpect(status().isConflict());
    }

    @Test
    void rejectsAHitPointAdjustmentWithoutAPositiveAmount() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-points/damage")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void appliesHealingAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(35, 5, true, Set.of("prone"));
        when(characterSheetService.applyHealing(any(Jwt.class), eq(characterId), eq(5))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-points/heal")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentHitPoints").value(35));
    }

    @Test
    void setsTemporaryHitPointsAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(30, 8, false, Set.of());
        when(characterSheetService.setTemporaryHitPoints(any(Jwt.class), eq(characterId), eq(8))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-points/temporary")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":8}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.temporaryHitPoints").value(8));
    }

    @Test
    void customizingMaxHitPointsReturnsTheCalculatedMaximumBesideTheCustomizedOne() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithCalculatedMaxHitPoints(46, 44);
        when(characterSheetService.customize(any(Jwt.class), eq(characterId), eq("hitPoints"), eq("maxModifier"), any(String.class)))
                .thenReturn(vitals);

        mockMvc.perform(put("/api/characters/" + characterId + "/customizations/hitPoints/maxModifier")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"value\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.calculatedMaxHitPoints").value(44))
                .andExpect(jsonPath("$.hitPoints.value").value(46));
    }

    @Test
    void togglesInspirationAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(30, 0, true, Set.of());
        when(characterSheetService.toggleInspiration(any(Jwt.class), eq(characterId))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/inspiration/toggle").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.heroicInspiration").value(true));
    }

    @Test
    void togglesAConditionAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(30, 0, false, Set.of("poisoned"));
        when(characterSheetService.toggleCondition(any(Jwt.class), eq(characterId), eq("poisoned"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/conditions/poisoned/toggle").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeConditions[0]").value("poisoned"));
    }

    @Test
    void returnsBadRequestForAnUnknownCondition() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.toggleCondition(any(Jwt.class), eq(characterId), eq("not-a-condition")))
                .thenThrow(new InvalidConditionException("not-a-condition"));

        mockMvc.perform(post("/api/characters/" + characterId + "/conditions/not-a-condition/toggle").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addsAProficiencyThroughTheCustomizationsAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithProficiencies(List.of("Shields"), List.of(), List.of(), List.of());
        when(characterSheetService.customize(any(Jwt.class), eq(characterId), eq("proficiencies"), eq("new"), any(String.class)))
                .thenReturn(vitals);

        mockMvc.perform(put("/api/characters/" + characterId + "/customizations/proficiencies/new")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"ARMOR\",\"name\":\"Shields\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.armorProficiencies[0]").value("Shields"));
    }

    @Test
    void addsAnItemAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(
                List.of(new Item("item-1", "Rope, hempen", 1, "1 gp", "50 feet", false, false, false)), new Coins(0, 0, 0, 0, 0));
        when(characterSheetService.addItem(
                        any(Jwt.class), eq(characterId), eq("Rope, hempen"), eq(1), eq("1 gp"), eq("50 feet"), eq(false),
                        isNull()))
                .thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/items")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Rope, hempen\",\"quantity\":1,\"cost\":\"1 gp\",\"notes\":\"50 feet\","
                                + "\"requiresAttunement\":false}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Rope, hempen"));
    }

    @Test
    void rejectsAddingAnItemWithABlankName() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/items")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\" \",\"quantity\":1,\"cost\":\"\",\"notes\":\"\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addsACatalogueItemAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        UUID catalogueEntryId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(
                List.of(new Item("item-1", "Warhammer", 1, "15 gp", "", false, false, false)), new Coins(0, 0, 0, 0, 0));
        when(characterSheetService.addCatalogueItem(any(Jwt.class), eq(characterId), eq(catalogueEntryId)))
                .thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/items/from-catalogue")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"catalogueEntryId\":\"" + catalogueEntryId + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Warhammer"));
    }

    @Test
    void removesAnItemAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(List.of(), new Coins(0, 0, 0, 0, 0));
        when(characterSheetService.removeItem(any(Jwt.class), eq(characterId), eq("item-1"))).thenReturn(vitals);

        mockMvc.perform(delete("/api/characters/" + characterId + "/items/item-1").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void addsACustomActionAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(List.of(), new Coins(0, 0, 0, 0, 0));
        AddCustomActionRequest expected = new AddCustomActionRequest(
                "GENERAL", "Custom Action 1", "", "", null, null, null, null, null, null, null, null, null, null,
                null, null, "ACTION", null, false, false, false, null, null, false, false);
        when(characterSheetService.addCustomAction(any(Jwt.class), eq(characterId), eq(expected))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/custom-actions")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"template\":\"GENERAL\",\"name\":\"Custom Action 1\",\"snippet\":\"\","
                                + "\"description\":\"\",\"activationType\":\"ACTION\",\"affectedByMartialArts\":false,"
                                + "\"proficient\":false,\"displayAsAttack\":false,\"dualWield\":false,\"silvered\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsAddingACustomActionWithABlankName() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/custom-actions")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"template\":\"GENERAL\",\"name\":\" \",\"snippet\":\"\",\"description\":\"\","
                                + "\"activationType\":\"ACTION\",\"affectedByMartialArts\":false,"
                                + "\"proficient\":false,\"displayAsAttack\":false,\"dualWield\":false,\"silvered\":false}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updatesACustomActionAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(List.of(), new Coins(0, 0, 0, 0, 0));
        AddCustomActionRequest expected = new AddCustomActionRequest(
                "GENERAL", "Tail Swipe", "", "", null, null, null, null, null, null, null, null, null, null,
                null, null, "BONUS_ACTION", null, false, false, false, null, null, false, false);
        when(characterSheetService.updateCustomAction(any(Jwt.class), eq(characterId), eq("action-1"), eq(expected)))
                .thenReturn(vitals);

        mockMvc.perform(put("/api/characters/" + characterId + "/custom-actions/action-1")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"template\":\"GENERAL\",\"name\":\"Tail Swipe\",\"snippet\":\"\","
                                + "\"description\":\"\",\"activationType\":\"BONUS_ACTION\",\"affectedByMartialArts\":false,"
                                + "\"proficient\":false,\"displayAsAttack\":false,\"dualWield\":false,\"silvered\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void removesACustomActionAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(List.of(), new Coins(0, 0, 0, 0, 0));
        when(characterSheetService.removeCustomAction(any(Jwt.class), eq(characterId), eq("action-1"))).thenReturn(vitals);

        mockMvc.perform(delete("/api/characters/" + characterId + "/custom-actions/action-1").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void togglesItemEquippedAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(
                List.of(new Item("item-1", "Shield", 1, "10 gp", "", true, false, false)), new Coins(0, 0, 0, 0, 0));
        when(characterSheetService.toggleItemEquipped(any(Jwt.class), eq(characterId), eq("item-1"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/items/item-1/equip/toggle").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].equipped").value(true));
    }

    @Test
    void togglesItemAttunedAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(
                List.of(new Item("item-1", "Cloak of Protection", 1, "", "", true, true, true)), new Coins(0, 0, 0, 0, 0));
        when(characterSheetService.toggleItemAttuned(any(Jwt.class), eq(characterId), eq("item-1"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/items/item-1/attune/toggle").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].attuned").value(true));
    }

    @Test
    void returnsBadRequestWhenAttunementLimitIsExceeded() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.toggleItemAttuned(any(Jwt.class), eq(characterId), eq("item-4")))
                .thenThrow(new AttunementLimitExceededException(3));

        mockMvc.perform(post("/api/characters/" + characterId + "/items/item-4/attune/toggle").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestWhenAttuningAnItemThatDoesNotRequireIt() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.toggleItemAttuned(any(Jwt.class), eq(characterId), eq("item-5")))
                .thenThrow(new AttunementNotAllowedException("item-5"));

        mockMvc.perform(post("/api/characters/" + characterId + "/items/item-5/attune/toggle").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addsCoinsAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(List.of(), new Coins(0, 0, 0, 10, 0));
        when(characterSheetService.addCoins(any(Jwt.class), eq(characterId), eq("gold"), eq(10))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/coins/gold")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins.gold").value(10));
    }

    @Test
    void removesCoinsAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithItems(List.of(), new Coins(0, 0, 0, 0, 0));
        when(characterSheetService.removeCoins(any(Jwt.class), eq(characterId), eq("gold"), eq(20))).thenReturn(vitals);

        mockMvc.perform(delete("/api/characters/" + characterId + "/coins/gold").param("amount", "20").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coins.gold").value(0));
    }

    @Test
    void returnsBadRequestForAnUnknownCoinDenomination() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.addCoins(any(Jwt.class), eq(characterId), eq("not-a-denomination"), eq(5)))
                .thenThrow(new InvalidCoinDenominationException("not-a-denomination"));

        mockMvc.perform(post("/api/characters/" + characterId + "/coins/not-a-denomination")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":5}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void usesAFeatureActionAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithFeatures(
                List.of(new FeatureAction("secondWind", "Second Wind", "BONUS_ACTION", "Regain hit points.", 1, 1, "SHORT_OR_LONG_REST")),
                List.of());
        when(characterSheetService.useFeatureAction(any(Jwt.class), eq(characterId), eq("secondWind"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/feature-actions/secondWind/use").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureActions[0].usedCount").value(1));
    }

    @Test
    void restoresAFeatureActionAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithFeatures(
                List.of(new FeatureAction("secondWind", "Second Wind", "BONUS_ACTION", "Regain hit points.", 1, 0, "SHORT_OR_LONG_REST")),
                List.of());
        when(characterSheetService.restoreFeatureAction(any(Jwt.class), eq(characterId), eq("secondWind"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/feature-actions/secondWind/restore").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureActions[0].usedCount").value(0));
    }

    @Test
    void usesAFeatureTraitUseAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithFeatures(
                List.of(),
                List.of(new FeatureTrait("secondWindTrait", "Second Wind", "CLASS_FEATURE", "Fighter", "Regain hit points.", "Regain hit points.", 1, 1, "SHORT_OR_LONG_REST")));
        when(characterSheetService.useFeatureTraitUse(any(Jwt.class), eq(characterId), eq("secondWindTrait"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/feature-traits/secondWindTrait/use").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureTraits[0].usedCount").value(1));
    }

    @Test
    void restoresAFeatureTraitUseAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithFeatures(
                List.of(),
                List.of(new FeatureTrait("secondWindTrait", "Second Wind", "CLASS_FEATURE", "Fighter", "Regain hit points.", "Regain hit points.", 1, 0, "SHORT_OR_LONG_REST")));
        when(characterSheetService.restoreFeatureTraitUse(any(Jwt.class), eq(characterId), eq("secondWindTrait"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/feature-traits/secondWindTrait/restore").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.featureTraits[0].usedCount").value(0));
    }

    @Test
    void consumesASpellSlotAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSpellSlots(List.of(new SpellSlotLevel(1, 3, 3)));
        when(characterSheetService.consumeSpellSlot(any(Jwt.class), eq(characterId), eq(1), eq(false))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/spell-slots/1/consume").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spellSlots[0].usedSlots").value(3));
    }

    @Test
    void consumesAPactMagicSlotAndReturnsThePoolWithItsClass() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSpellSlots(List.of(new SpellSlotLevel(2, 2, 1, true, "Warlock")));
        when(characterSheetService.consumeSpellSlot(any(Jwt.class), eq(characterId), eq(2), eq(true))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/spell-slots/2/consume").param("pact", "true").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spellSlots[0].pact").value(true))
                .andExpect(jsonPath("$.spellSlots[0].className").value("Warlock"));
    }

    @Test
    void castsASpellAtTheChosenSlotLevel() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSpellSlots(List.of(new SpellSlotLevel(2, 3, 1)));
        when(characterSheetService.castSpell(any(Jwt.class), eq(characterId), eq("mage-armor"), eq(2), eq(false), eq(true))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/spells/mage-armor/cast").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotLevel\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spellSlots[0].usedSlots").value(1))
                .andExpect(jsonPath("$.activeEffects").isArray());
    }

    @Test
    void castsASpellOnAnAlly() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSpellSlots(List.of(new SpellSlotLevel(1, 3, 1)));
        when(characterSheetService.castSpell(any(Jwt.class), eq(characterId), eq("mage-armor"), eq(1), eq(false), eq(false))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/spells/mage-armor/cast").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotLevel\":1,\"onSelf\":false}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsACastAboveNinthLevel() throws Exception {
        mockMvc.perform(post("/api/characters/" + UUID.randomUUID() + "/spells/mage-armor/cast").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotLevel\":10}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void endsAnActiveEffect() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSpellSlots(List.of());
        when(characterSheetService.endActiveEffect(any(Jwt.class), eq(characterId), eq("mage-armor"))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/active-effects/mage-armor/end").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activeEffects").isEmpty());
    }

    @Test
    void restoresASpellSlotAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSpellSlots(List.of(new SpellSlotLevel(1, 3, 0)));
        when(characterSheetService.restoreSpellSlot(any(Jwt.class), eq(characterId), eq(1), eq(false))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/spell-slots/1/restore").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.spellSlots[0].usedSlots").value(0));
    }

    @Test
    void appliesAShortRestAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithFeatures(
                List.of(new FeatureAction("secondWind", "Second Wind", "BONUS_ACTION", "Regain hit points.", 1, 0, "SHORT_OR_LONG_REST")),
                List.of());
        when(characterSheetService.applyShortRest(any(Jwt.class), eq(characterId), eq(0), isNull()))
                .thenReturn(new ShortRestResult(vitals, List.of()));

        mockMvc.perform(post("/api/characters/" + characterId + "/rest/short")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hitDiceSpent\":0}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheet.featureActions[0].usedCount").value(0))
                .andExpect(jsonPath("$.roll").doesNotExist());
    }

    @Test
    void appliesAShortRestSpendingHitDiceAndReturnsTheRoll() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(38, 0, false, Set.of());
        Roll roll = Roll.create(characterId, "2d10+4", "Hit Die: heal", new int[] {6, 8}, 18);
        when(characterSheetService.applyShortRest(any(Jwt.class), eq(characterId), eq(2), isNull()))
                .thenReturn(new ShortRestResult(vitals, List.of(roll)));

        mockMvc.perform(post("/api/characters/" + characterId + "/rest/short")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hitDiceSpent\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheet.currentHitPoints").value(38))
                .andExpect(jsonPath("$.roll.expression").value("2d10+4"))
                .andExpect(jsonPath("$.roll.total").value(18))
                .andExpect(jsonPath("$.rolls[0].total").value(18));
    }

    @Test
    void appliesAShortRestSpendingHitDiceOfSeveralSizes() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(38, 0, false, Set.of());
        Roll d8 = Roll.create(characterId, "2d8+1", "Hit Die (d8): heal", new int[] {3, 5}, 10);
        Roll d6 = Roll.create(characterId, "1d6+1", "Hit Die (d6): heal", new int[] {4}, 5);
        when(characterSheetService.applyShortRest(any(Jwt.class), eq(characterId), eq(0), eq(Map.of(8, 2, 6, 1))))
                .thenReturn(new ShortRestResult(vitals, List.of(d8, d6)));

        mockMvc.perform(post("/api/characters/" + characterId + "/rest/short")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hitDiceSpent\":0,\"hitDiceBySize\":{\"8\":2,\"6\":1}}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rolls.length()").value(2))
                .andExpect(jsonPath("$.roll.expression").value("1d6+1"));
    }

    @Test
    void appliesALongRestAndReturnsTheUpdatedSheet() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(44, 0, false, Set.of());
        when(characterSheetService.applyLongRest(any(Jwt.class), eq(characterId), isNull())).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/rest/long").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currentHitPoints").value(44));
    }

    @Test
    void appliesALongRestRecoveringTheChosenHitDice() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(44, 0, false, Set.of());
        when(characterSheetService.applyLongRest(any(Jwt.class), eq(characterId), eq(Map.of(6, 2)))).thenReturn(vitals);

        mockMvc.perform(post("/api/characters/" + characterId + "/rest/long")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hitDiceRecovered\":{\"6\":2}}"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsALongRestRecoveringTooManyHitDice() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.applyLongRest(any(Jwt.class), eq(characterId), eq(Map.of(8, 5))))
                .thenThrow(new InvalidHitDiceRecoveryException("A long rest recovers at most 3 hit dice, not 5"));

        mockMvc.perform(post("/api/characters/" + characterId + "/rest/long")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"hitDiceRecovered\":{\"8\":5}}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void spendsHitDiceAndReturnsTheUpdatedSheetAndRoll() throws Exception {
        UUID characterId = UUID.randomUUID();
        VitalsZone vitals = vitalsWithSessionState(38, 0, false, Set.of());
        Roll roll = Roll.create(characterId, "2d10+4", "Hit Die: heal", new int[] {6, 8}, 18);
        when(characterSheetService.spendHitDice(any(Jwt.class), eq(characterId), eq(2), isNull()))
                .thenReturn(new HitDiceSpendResult(vitals, roll));

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-dice/spend")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sheet.currentHitPoints").value(38))
                .andExpect(jsonPath("$.roll.expression").value("2d10+4"))
                .andExpect(jsonPath("$.roll.total").value(18));
    }

    @Test
    void returnsBadRequestWhenSpendingMoreHitDiceThanAvailable() throws Exception {
        UUID characterId = UUID.randomUUID();
        when(characterSheetService.spendHitDice(any(Jwt.class), eq(characterId), eq(6), isNull()))
                .thenThrow(new InsufficientHitDiceException(6, 5));

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-dice/spend")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":6}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsSpendingHitDiceWithoutAPositiveCount() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/hit-dice/spend")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":0}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deletesACharacter() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(delete("/api/characters/" + characterId).with(jwt()))
                .andExpect(status().isNoContent());
    }

    @Test
    void rejectsRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/characters")).andExpect(status().isUnauthorized());
    }

    /** A minimal, otherwise-empty vitals fixture for asserting a mutation's session-state fields. */
    private VitalsZone vitalsWithSessionState(
            int currentHitPoints, int temporaryHitPoints, boolean heroicInspiration, Set<String> activeConditions) {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                activeConditions, 0, currentHitPoints, temporaryHitPoints, 0, heroicInspiration, Map.of(), List.of(),
                List.of(), List.of(), List.of(), new Coins(0, 0, 0, 0, 0), List.of(), emptyBackground(), List.of(),
                new HitDice(10, 5, 0), List.of(), List.of(), List.of(), new Encumbrance(false, 0, 0, false, List.of()));
    }

    /** A minimal, otherwise-empty vitals fixture for asserting a max hit points adjustment's result. */
    private VitalsZone vitalsWithCalculatedMaxHitPoints(int hitPoints, int calculatedMaxHitPoints) {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        CalculatedValue hitPointsValue = new CalculatedValue(hitPoints, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, hitPointsValue, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of(), 0, hitPoints, 0, calculatedMaxHitPoints, false, Map.of(), List.of(), List.of(), List.of(),
                List.of(), new Coins(0, 0, 0, 0, 0), List.of(), emptyBackground(), List.of(),
                new HitDice(10, 5, 0), List.of(), List.of(), List.of(), new Encumbrance(false, 0, 0, false, List.of()));
    }

    /** A minimal, otherwise-empty vitals fixture for asserting a proficiency edit's result. */
    private VitalsZone vitalsWithProficiencies(
            List<String> armorProficiencies, List<String> weaponProficiencies, List<String> toolProficiencies,
            List<String> languages) {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), armorProficiencies, weaponProficiencies, toolProficiencies, languages,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of(), 0, 30, 0, 0, false, Map.of(), List.of(), List.of(), List.of(), List.of(), new Coins(0, 0, 0, 0, 0),
                List.of(), emptyBackground(), List.of(), new HitDice(10, 5, 0), List.of(), List.of(), List.of(), new Encumbrance(false, 0, 0, false, List.of()));
    }

    /** A minimal, otherwise-empty vitals fixture for asserting an item/coin edit's result. */
    private VitalsZone vitalsWithItems(List<Item> items, Coins coins) {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of(), 0, 30, 0, 0, false, Map.of(), List.of(), List.of(), List.of(), items, coins, List.of(),
                emptyBackground(), List.of(), new HitDice(10, 5, 0), List.of(), List.of(), List.of(), new Encumbrance(false, 0, 0, false, List.of()));
    }

    /** A minimal, otherwise-empty vitals fixture for asserting a feature use/restore result. */
    private VitalsZone vitalsWithFeatures(List<FeatureAction> featureActions, List<FeatureTrait> featureTraits) {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of(), 0, 30, 0, 0, false, Map.of(), featureActions, List.of(), List.of(), List.of(), new Coins(0, 0, 0, 0, 0),
                featureTraits, emptyBackground(), List.of(), new HitDice(10, 5, 0), List.of(), List.of(), List.of(), new Encumbrance(false, 0, 0, false, List.of()));
    }

    private VitalsZone vitalsWithSpellSlots(List<SpellSlotLevel> spellSlots) {
        CalculatedValue zero = new CalculatedValue(0, List.of());
        return new VitalsZone(
                Map.of(), Map.of(), zero, zero, zero, zero, 30, 5,
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Map.of(), Map.of(), Map.of(), List.of(), List.of(), List.of(), List.of(),
                Set.of(), 0, 30, 0, 0, false, Map.of(), List.of(), List.of(), List.of(), List.of(), new Coins(0, 0, 0, 0, 0),
                List.of(), emptyBackground(), List.of(), new HitDice(10, 5, 0), spellSlots, List.of(), List.of(),
                new Encumbrance(false, 0, 0, false, List.of()));
    }

    private Background emptyBackground() {
        return new Background("", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "", "");
    }
}

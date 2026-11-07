package dev.omnisheetvault.api.ruleset.daggerheart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.dice.Roll;
import dev.omnisheetvault.api.dice.RollService;
import dev.omnisheetvault.api.shared.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(DaggerheartRollController.class)
@Import(SecurityConfig.class)
class DaggerheartRollControllerTest {

    private static final UUID ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CharacterService characterService;

    @MockitoBean
    private RollService rollService;

    /** The valid Ranger: Agility +2, Experiences Tracker and Lone Wolf at +2, Hope 2, a Shortbow d6+3. */
    @BeforeEach
    void aRanger() {
        Character character = mock(Character.class);
        when(character.systemId()).thenReturn(DaggerheartGameSystem.SYSTEM_ID);
        when(character.sheet()).thenReturn(new DaggerheartCharacterCreationFlow(DaggerheartTestCatalogue.MAPPER)
                .materialize(null, DaggerheartTestCatalogue.VALID, new DaggerheartTestCatalogue()).sheetJson());
        when(characterService.getMineActive(any(Jwt.class), eq(ID))).thenReturn(character);
        when(rollService.recordReadRoll(eq(ID), anyString(), anyString(), any(int[].class), anyInt()))
                .thenAnswer(call -> Roll.create(ID, call.getArgument(1), call.getArgument(2), call.getArgument(3), call.getArgument(4)));
    }

    private String duality(String body) throws Exception {
        return mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/duality").with(jwt()).contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    void anActionRollWithHopeAddsTheTraitAndExperienceAndGainsAHope() throws Exception {
        when(rollService.rollDice(2, 12)).thenReturn(new int[] {9, 4});

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/duality").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trait\":\"agility\",\"experiences\":[0],\"difficulty\":12}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roll.total").value(17))
                .andExpect(jsonPath("$.roll.outcome").value("Success with Hope"))
                .andExpect(jsonPath("$.roll.label").value("Agility + Tracker"))
                .andExpect(jsonPath("$.sheet.trackers.hope").value(2));

        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), argThat(json -> json.contains("\"hope\":2")));
    }

    @Test
    void aCriticalClearsAStressButAReactionRollGainsNothing() throws Exception {
        when(rollService.rollDice(2, 12)).thenReturn(new int[] {6, 6});

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/duality").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trait\":\"instinct\",\"reaction\":true}"))
                .andExpect(jsonPath("$.roll.kind").value("reaction"))
                .andExpect(jsonPath("$.roll.critical").value(true))
                .andExpect(jsonPath("$.sheet.trackers.hope").value(2));

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/duality").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trait\":\"instinct\"}"))
                .andExpect(jsonPath("$.roll.outcome").value("Critical Success"))
                .andExpect(jsonPath("$.sheet.trackers.hope").value(3));
    }

    @Test
    void experiencesCostAHopeEachAndCantBeUsedWithoutIt() throws Exception {
        when(rollService.rollDice(2, 12)).thenReturn(new int[] {3, 8});

        duality("{\"experiences\":[0,1]}");
        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), argThat(json -> json.contains("\"hope\":0")));

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/duality").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"experiences\":[0,1,0]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void advantageRollsTheD6() throws Exception {
        when(rollService.rollDice(2, 12)).thenReturn(new int[] {5, 7});
        when(rollService.rollDice(1, 6)).thenReturn(new int[] {4});

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/duality").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"trait\":\"strength\",\"advantage\":\"advantage\",\"modifier\":1}"))
                .andExpect(jsonPath("$.roll.advantageDie").value(4))
                .andExpect(jsonPath("$.roll.total").value(18))
                .andExpect(jsonPath("$.roll.outcome").value("18 with Fear"));
    }

    @Test
    void aWeaponDamageRollUsesProficiencyDiceAndItsBonus() throws Exception {
        when(rollService.rollDice(1, 6)).thenReturn(new int[] {5});

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/damage").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"weapon\":\"primary\",\"critical\":true}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("damage"))
                .andExpect(jsonPath("$.label").value("Shortbow damage (critical)"))
                .andExpect(jsonPath("$.total").value(14));
        verify(characterService, never()).replaceMySheet(any(Jwt.class), any(UUID.class), anyString());
    }

    @Test
    void aCustomRollMixesDiceAndAModifier() throws Exception {
        when(rollService.rollDice(2, 6)).thenReturn(new int[] {3, 5});
        when(rollService.rollDice(1, 20)).thenReturn(new int[] {17});

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/custom").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dice\":[{\"count\":2,\"sides\":6},{\"count\":1,\"sides\":20}],\"bonus\":-1}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("custom"))
                .andExpect(jsonPath("$.dice.length()").value(3))
                .andExpect(jsonPath("$.total").value(24));
        verify(rollService).recordReadRoll(eq(ID), eq("dice 2d6 1d20 -1"), eq("Custom roll"), any(int[].class), eq(24));
    }

    @Test
    void rejectsDiceTheGameDoesntUse() throws Exception {
        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/rolls/damage").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"count\":2,\"sides\":7}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void theHistoryReadsEachKindBack() throws Exception {
        when(rollService.history(any(Jwt.class), eq(ID))).thenReturn(List.of(
                Roll.create(ID, "duality action +2 dc 12", "Agility", new int[] {9, 4}, 15),
                Roll.create(ID, "damage 1d6+3", "Shortbow damage", new int[] {5}, 8),
                Roll.create(ID, "rest 1d4+1", "Tend to Wounds (short rest)", new int[] {3}, 4)));

        mockMvc.perform(get("/api/daggerheart/characters/" + ID + "/rolls").with(jwt()))
                .andExpect(jsonPath("$[0].outcome").value("Success with Hope"))
                .andExpect(jsonPath("$[1].total").value(8))
                .andExpect(jsonPath("$[2].kind").value("rest"));
    }
}

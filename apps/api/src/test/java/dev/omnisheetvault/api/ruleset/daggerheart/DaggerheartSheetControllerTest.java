package dev.omnisheetvault.api.ruleset.daggerheart;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.dice.RollService;
import dev.omnisheetvault.api.shared.SecurityConfig;
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

@WebMvcTest(DaggerheartSheetController.class)
@Import(SecurityConfig.class)
class DaggerheartSheetControllerTest {

    private static final UUID ID = UUID.randomUUID();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CharacterService characterService;

    @MockitoBean
    private RollService rollService;

    private Character character;

    @BeforeEach
    void aRanger() {
        character = mock(Character.class);
        when(character.systemId()).thenReturn(DaggerheartGameSystem.SYSTEM_ID);
        when(character.sheet()).thenReturn(new DaggerheartCharacterCreationFlow(DaggerheartTestCatalogue.MAPPER)
                .materialize(null, DaggerheartTestCatalogue.VALID, new DaggerheartTestCatalogue()).sheetJson());
        when(characterService.getMineActive(any(Jwt.class), eq(ID))).thenReturn(character);
    }

    @Test
    void servesTheSheetWithItsDerivedValues() throws Exception {
        mockMvc.perform(get("/api/daggerheart/characters/" + ID + "/sheet").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.identity.class.name").value("Ranger"))
                .andExpect(jsonPath("$.derived.evasion.value").value(13))
                .andExpect(jsonPath("$.derived.severeThreshold.value").value(14))
                .andExpect(jsonPath("$.derived.attacks[0].damage").value("1d6+3"));
    }

    @Test
    void appliesAnActionStoresTheSheetAndReturnsIt() throws Exception {
        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/actions").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"damage\",\"amount\":9}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trackers.hitPointsMarked").value(2))
                .andExpect(jsonPath("$.derived.hitPointSlots.value").value(6));

        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), argThat(json -> json.contains("\"hitPointsMarked\":2")));
    }

    @Test
    void logsTheDiceAShortRestRolled() throws Exception {
        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/actions").with(jwt()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"rest\",\"kind\":\"short\",\"moves\":[{\"move\":\"clearStress\"},{\"move\":\"prepare\"}]}"))
                .andExpect(status().isOk());

        verify(rollService).recordReadRoll(eq(ID), eq("rest 1d4+1"), eq("Clear Stress (short rest)"), any(int[].class), anyInt());
    }

    @Test
    void rejectsAnUnknownAction() throws Exception {
        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/actions").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"teleport\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void refusesAnotherSystemsCharacter() throws Exception {
        when(character.systemId()).thenReturn("dnd-5e");

        mockMvc.perform(get("/api/daggerheart/characters/" + ID + "/sheet").with(jwt())).andExpect(status().isBadRequest());
    }
}

package dev.omnisheetvault.api.dice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.ruleset.RollKind;
import dev.omnisheetvault.api.shared.SecurityConfig;
import java.util.List;
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

@WebMvcTest(RollController.class)
@Import(SecurityConfig.class)
class RollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private RollService rollService;

    @Test
    void rollsAndReturnsTheResult() throws Exception {
        UUID characterId = UUID.randomUUID();
        Roll roll = Roll.create(characterId, "1d20+3", "Strength: check", new int[] {14}, 17);
        when(rollService.roll(any(Jwt.class), eq(characterId), any(RollRequest.class))).thenReturn(roll);

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RollRequest(RollKind.ABILITY_CHECK, "strength", false, false, null))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value("1d20+3"))
                .andExpect(jsonPath("$.context").value("Strength: check"))
                .andExpect(jsonPath("$.results[0]").value(14))
                .andExpect(jsonPath("$.total").value(17));
    }

    @Test
    void rejectsARollRequestWithNoKind() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"strength\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rollsManualDiceAndReturnsTheResult() throws Exception {
        UUID characterId = UUID.randomUUID();
        Roll roll = Roll.create(characterId, "2d20 + 3d6", "Custom: roll", new int[] {14, 9, 1, 4, 6}, 34);
        when(rollService.manualRoll(any(Jwt.class), eq(characterId), any(ManualRollRequest.class))).thenReturn(roll);

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls/manual")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dice\":[{\"type\":\"D20\",\"count\":2},{\"type\":\"D6\",\"count\":3}]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value("2d20 + 3d6"))
                .andExpect(jsonPath("$.context").value("Custom: roll"))
                .andExpect(jsonPath("$.total").value(34));
    }

    @Test
    void rejectsAManualRollWithNoDice() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls/manual")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dice\":[]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAManualRollWithACountAboveTheCap() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls/manual")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dice\":[{\"type\":\"D6\",\"count\":21}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rollsCreationDiceAndMarksTheDroppedOnes() throws Exception {
        UUID characterId = UUID.randomUUID();
        Roll roll = Roll.create(characterId, "4d6kh3", "Strength (4d6 drop lowest)", new int[] {5, 2, 6, 3}, 14);
        when(rollService.creationRoll(any(Jwt.class), eq(characterId), any(CreationRollRequest.class))).thenReturn(roll);

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls/creation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"die\":\"D6\",\"count\":4,\"keepHighest\":3,\"context\":\"Strength (4d6 drop lowest)\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.expression").value("4d6kh3"))
                .andExpect(jsonPath("$.dropped[0]").value(1))
                .andExpect(jsonPath("$.total").value(14));
    }

    @Test
    void rejectsACreationRollKeepingMoreDiceThanItRolls() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls/creation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"die\":\"D6\",\"count\":2,\"keepHighest\":3,\"context\":\"Strength\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsACreationRollWithoutALabel() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls/creation")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"die\":\"D8\",\"count\":1,\"context\":\" \"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsAManualRollWithAnUnknownDieType() throws Exception {
        UUID characterId = UUID.randomUUID();

        mockMvc.perform(post("/api/characters/" + characterId + "/rolls/manual")
                        .with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dice\":[{\"type\":\"D3\",\"count\":1}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsRollHistoryNewestFirst() throws Exception {
        UUID characterId = UUID.randomUUID();
        Roll first = Roll.create(characterId, "1d20+3", "Strength: check", new int[] {14}, 17);
        Roll second = Roll.create(characterId, "1d20+1", "Initiative: roll", new int[] {9}, 10);
        when(rollService.history(any(Jwt.class), eq(characterId))).thenReturn(List.of(second, first));

        mockMvc.perform(get("/api/characters/" + characterId + "/rolls").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].context").value("Initiative: roll"))
                .andExpect(jsonPath("$[1].context").value("Strength: check"));
    }
}

package dev.omnisheetvault.api.ruleset.vtm5;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
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

@WebMvcTest({Vtm5SheetController.class, Vtm5RollController.class})
@Import({SecurityConfig.class, Vtm5Content.class, Vtm5SheetActions.class})
class Vtm5ControllersTest {

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
    void aVampire() {
        character = mock(Character.class);
        when(character.systemId()).thenReturn(Vtm5GameSystem.SYSTEM_ID);
        when(character.sheet()).thenReturn(Vtm5TestBuilds.json(Vtm5TestBuilds.validSheet()));
        when(characterService.getMineActive(any(Jwt.class), eq(ID))).thenReturn(character);
    }

    @Test
    void servesTheContent() throws Exception {
        mockMvc.perform(get("/api/vtm5/content").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clans[4].name").value("Toreador"));
    }

    @Test
    void servesTheSheetWithItsDerivedValues() throws Exception {
        mockMvc.perform(get("/api/vtm5/characters/" + ID + "/sheet").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.derived.healthMax").value(5))
                .andExpect(jsonPath("$.derived.willpowerMax").value(3))
                .andExpect(jsonPath("$.derived.xpAvailable").value(15))
                .andExpect(jsonPath("$.derived.clan.name").value("Toreador"))
                .andExpect(jsonPath("$.derived.nextCost.attribute.Composure").value(10));
    }

    @Test
    void refusesAnotherSystemsCharacter() throws Exception {
        when(character.systemId()).thenReturn("dnd-5e");

        mockMvc.perform(get("/api/vtm5/characters/" + ID + "/sheet").with(jwt())).andExpect(status().isBadRequest());
    }

    @Test
    void appliesAnActionAndStoresTheSheet() throws Exception {
        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/actions").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"hunger\",\"value\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hunger").value(3));

        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), anyString());
    }

    @Test
    void aRuleBreakingActionIsABadRequest() throws Exception {
        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/actions").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"type\":\"identity\",\"field\":\"clan\",\"value\":\"x\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rollsAPoolWithHungerDice() throws Exception {
        when(rollService.rollDice(5, 10)).thenReturn(new int[] {10, 10, 3, 2, 7});
        when(rollService.recordReadRoll(eq(ID), eq("5d10 hunger 1 difficulty 4"), anyString(), any(int[].class), anyInt()))
                .thenAnswer(call -> Roll.create(ID, call.getArgument(1), call.getArgument(2), call.getArgument(3), call.getArgument(4)));

        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"traits\":[\"Dexterity\",\"Larceny\"],\"bonus\":0,\"difficulty\":4}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("pool"))
                .andExpect(jsonPath("$.label").value("Dexterity + Larceny"))
                .andExpect(jsonPath("$.hunger").value(1))
                .andExpect(jsonPath("$.successes").value(5))
                .andExpect(jsonPath("$.outcome").value("Critical"))
                .andExpect(jsonPath("$.difficulty").value(4))
                .andExpect(jsonPath("$.margin").value(1))
                .andExpect(jsonPath("$.win").value(true));
    }

    @Test
    void anUnknownTraitIsABadRequest() throws Exception {
        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"traits\":[\"Juggling\"]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void aFailedRouseCheckRaisesHunger() throws Exception {
        when(rollService.rollDice(1, 10)).thenReturn(new int[] {3});
        when(rollService.recordReadRoll(eq(ID), eq("1d10 rouse"), anyString(), any(int[].class), anyInt()))
                .thenAnswer(call -> Roll.create(ID, call.getArgument(1), call.getArgument(2), call.getArgument(3), call.getArgument(4)));

        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls/rouse").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"reason\":\"Mend\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("rouse"))
                .andExpect(jsonPath("$.successes").value(0))
                .andExpect(jsonPath("$.label").value("Rouse Check · Mend — Hunger 2"));

        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), anyString());
    }

    @Test
    void reRollsFailedRegularDiceOnceForOneWillpower() throws Exception {
        Roll original = Roll.create(ID, "4d10 hunger 1", "Wits + Awareness", new int[] {2, 3, 8, 1}, 1);
        when(rollService.history(any(Jwt.class), eq(ID))).thenReturn(List.of(original));
        when(rollService.rollDice(1, 10)).thenReturn(new int[] {9});
        when(rollService.recordReadRoll(eq(ID), eq("4d10 hunger 1"), anyString(), any(int[].class), anyInt()))
                .thenAnswer(call -> Roll.create(ID, call.getArgument(1), call.getArgument(2), call.getArgument(3), call.getArgument(4)));

        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls/" + original.id() + "/reroll").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("reroll"))
                .andExpect(jsonPath("$.label").value("Wits + Awareness"))
                .andExpect(jsonPath("$.successes").value(3));

        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), anyString());
    }

    @Test
    void listsTheRollLogMarkingReRolledRolls() throws Exception {
        Roll original = Roll.create(ID, "4d10 hunger 1", "Wits + Awareness", new int[] {2, 3, 8, 1}, 1);
        Roll again = Roll.create(ID, "4d10 hunger 1", "Re-roll of " + original.id() + ": Wits + Awareness", new int[] {9, 9, 8, 1}, 3);
        Roll rouse = Roll.create(ID, "1d10 rouse", "Rouse Check — Passed", new int[] {7}, 1);
        when(rollService.history(any(Jwt.class), eq(ID))).thenReturn(List.of(rouse, again, original));

        mockMvc.perform(get("/api/vtm5/characters/" + ID + "/rolls").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kind").value("rouse"))
                .andExpect(jsonPath("$[1].kind").value("reroll"))
                .andExpect(jsonPath("$[2].rerolled").value(true));

        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls/" + original.id() + "/reroll").with(jwt()))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls/" + rouse.id() + "/reroll").with(jwt()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resistsFrenzyWithWillpowerAndAThirdOfHumanity() throws Exception {
        when(rollService.rollDice(5, 10)).thenReturn(new int[] {6, 7, 8, 1, 2});
        when(rollService.recordReadRoll(eq(ID), eq("5d10 hunger 0 difficulty 3"), anyString(), any(int[].class), anyInt()))
                .thenAnswer(call -> Roll.create(ID, call.getArgument(1), call.getArgument(2), call.getArgument(3), call.getArgument(4)));

        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls/frenzy").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"difficulty\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("frenzy"))
                .andExpect(jsonPath("$.label").value("Frenzy check — Resisted"))
                .andExpect(jsonPath("$.win").value(true));
    }

    @Test
    void remorseNeedsStainsAndCanCostHumanity() throws Exception {
        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls/remorse").with(jwt())).andExpect(status().isBadRequest());

        tools.jackson.databind.node.ObjectNode stained = Vtm5TestBuilds.validSheet();
        stained.put("stains", 2);
        when(character.sheet()).thenReturn(Vtm5TestBuilds.json(stained));
        when(rollService.rollDice(1, 10)).thenReturn(new int[] {3});
        when(rollService.recordReadRoll(eq(ID), eq("1d10 hunger 0"), anyString(), any(int[].class), anyInt()))
                .thenAnswer(call -> Roll.create(ID, call.getArgument(1), call.getArgument(2), call.getArgument(3), call.getArgument(4)));

        mockMvc.perform(post("/api/vtm5/characters/" + ID + "/rolls/remorse").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("remorse"))
                .andExpect(jsonPath("$.label").value("Remorse — Humanity 8 → 7"));
        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), anyString());
    }
}

package dev.omnisheetvault.api.ruleset.daggerheart;

import static org.mockito.ArgumentMatchers.any;
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

import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import dev.omnisheetvault.api.shared.SecurityConfig;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.node.ObjectNode;

@WebMvcTest(DaggerheartLevelUpController.class)
@Import({SecurityConfig.class, DaggerheartCharacterCreationFlow.class})
class DaggerheartLevelUpControllerTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String LEVEL_2 = """
            {"experience":"Archer","domainCard":"sage-two","advancements":[
             {"option":"traits","tier":2,"traits":["agility","strength"]},{"option":"hitPoint","tier":2}]}""";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CharacterService characterService;

    @MockitoBean
    private CatalogueService catalogueService;

    private Character character;

    @BeforeEach
    void aLevelOneRanger() {
        character = mock(Character.class);
        when(character.systemId()).thenReturn(DaggerheartGameSystem.SYSTEM_ID);
        when(characterService.getMineActive(any(Jwt.class), eq(ID))).thenReturn(character);
        when(catalogueService.lookup(DaggerheartGameSystem.SYSTEM_ID)).thenReturn(new DaggerheartTestCatalogue());
        withDraft(null);
    }

    private void withDraft(String draft) {
        String sheet = new DaggerheartCharacterCreationFlow(DaggerheartTestCatalogue.MAPPER)
                .materialize(null, DaggerheartTestCatalogue.VALID, new DaggerheartTestCatalogue()).sheetJson();
        ObjectNode node = (ObjectNode) DaggerheartTestCatalogue.MAPPER.readTree(sheet);
        ((ObjectNode) node.get("trackers")).put("hope", 5);
        if (draft != null) {
            node.set("levelUpDraft", DaggerheartTestCatalogue.MAPPER.readTree(draft));
        }
        when(character.sheet()).thenReturn(DaggerheartTestCatalogue.json(node));
    }

    @Test
    void showsTheNextLevelWithItsGridAndWhatIsMissing() throws Exception {
        mockMvc.perform(get("/api/daggerheart/characters/" + ID + "/level-up").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextLevel").value(2))
                .andExpect(jsonPath("$.achievement.experience").value(true))
                .andExpect(jsonPath("$.grid[0].options[0].slots").value(3))
                .andExpect(jsonPath("$.grid[1].open").value(false))
                .andExpect(jsonPath("$.missing.length()").value(3))
                .andExpect(jsonPath("$.after.majorThreshold").value(8))
                .andExpect(jsonPath("$.cardOptions[?(@.slug == 'sage-two')]").exists());
    }

    @Test
    void finishingAppendsTheLevelAndKeepsThePlayState() throws Exception {
        withDraft(LEVEL_2);

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/level-up/finish").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.level").value(2))
                .andExpect(jsonPath("$.trackers.hope").value(5))
                .andExpect(jsonPath("$.derived.hitPointSlots.value").value(7))
                .andExpect(jsonPath("$.levelUpDraft").doesNotExist());

        verify(characterService).replaceMySheet(any(Jwt.class), eq(ID), argThat(json -> json.contains("\"levels\":[{\"experience\":\"Archer\"")));
    }

    @Test
    void refusesToFinishAnIncompleteLevel() throws Exception {
        withDraft("{\"experience\":\"Archer\"}");

        mockMvc.perform(post("/api/daggerheart/characters/" + ID + "/level-up/finish").with(jwt())).andExpect(status().isBadRequest());
        verify(characterService, never()).replaceMySheet(any(Jwt.class), any(UUID.class), anyString());
    }
}

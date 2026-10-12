package dev.omnisheetvault.api.identity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.shared.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TutorialController.class)
@Import(SecurityConfig.class)
class TutorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private PlayerService playerService;

    @Test
    void listsTheSeenTutorials() throws Exception {
        Player player = Player.create("11111111-1111-1111-1111-111111111111", "lydia");
        when(playerService.currentPlayer(any(Jwt.class))).thenReturn(player);

        mockMvc.perform(get("/api/me/tutorials").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.seen").isArray())
                .andExpect(jsonPath("$.seen").isEmpty());
    }

    @Test
    void marksATutorialSeen() throws Exception {
        mockMvc.perform(put("/api/me/tutorials/builder:dnd-5e").with(jwt()).with(csrf()))
                .andExpect(status().isNoContent());

        verify(playerService).markTutorialSeen(any(Jwt.class), eq("builder:dnd-5e"));
    }

    @Test
    void rejectsAMalformedTutorialId() throws Exception {
        doThrow(new InvalidTutorialIdException("BAD")).when(playerService).markTutorialSeen(any(Jwt.class), eq("BAD"));

        mockMvc.perform(put("/api/me/tutorials/BAD").with(jwt()).with(csrf()))
                .andExpect(status().isBadRequest());
    }
}

package dev.omnisheetvault.api.identity;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.shared.SecurityConfig;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrentUserController.class)
@Import(SecurityConfig.class)
class CurrentUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void returnsClaimsFromTheAccessToken() throws Exception {
        mockMvc.perform(get("/api/me").with(jwt().jwt(token -> token
                        .subject("11111111-1111-1111-1111-111111111111")
                        .claim("preferred_username", "lydia")
                        .claim("email", "lydia@example.com")
                        .claim("realm_access", Map.of("roles", List.of("player"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("11111111-1111-1111-1111-111111111111"))
                .andExpect(jsonPath("$.username").value("lydia"))
                .andExpect(jsonPath("$.email").value("lydia@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("player"));
    }

    @Test
    void rejectsRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }
}

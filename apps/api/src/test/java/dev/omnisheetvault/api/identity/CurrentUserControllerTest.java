package dev.omnisheetvault.api.identity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.shared.SecurityConfig;
import dev.omnisheetvault.api.storage.PortraitStorage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CurrentUserController.class)
@Import({SecurityConfig.class, ProfileService.class})
class CurrentUserControllerTest {

    private static final String SUBJECT = "11111111-1111-1111-1111-111111111111";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private PlayerService playerService;

    @MockitoBean
    private PortraitStorage storage;

    @Test
    void returnsThePersistedPlayerAndLiveTokenClaims() throws Exception {
        Player player = Player.create(SUBJECT, "lydia");
        when(playerService.currentPlayer(any(Jwt.class))).thenReturn(player);

        mockMvc.perform(get("/api/me").with(jwt().jwt(token -> token
                        .subject(SUBJECT)
                        .claim("email", "lydia@example.com")
                        .claim("realm_access", Map.of("roles", List.of("player"))))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(player.id().toString()))
                .andExpect(jsonPath("$.subject").value(SUBJECT))
                .andExpect(jsonPath("$.displayName").value("lydia"))
                .andExpect(jsonPath("$.username").value("lydia"))
                .andExpect(jsonPath("$.email").value("lydia@example.com"))
                .andExpect(jsonPath("$.roles[0]").value("player"))
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());
    }

    @Test
    void showsTheChosenNameAndKeepsTheUsername() throws Exception {
        Player player = Player.create(SUBJECT, "lydia");
        player.rename("Lady Lydia");
        when(playerService.rename(any(Jwt.class), eq("Lady Lydia"))).thenReturn(player);

        mockMvc.perform(put("/api/me/profile").with(jwt().jwt(token -> token.subject(SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"Lady Lydia\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.displayName").value("Lady Lydia"))
                .andExpect(jsonPath("$.username").value("lydia"));
    }

    @Test
    void rejectsANameLongerThanFortyCharacters() throws Exception {
        mockMvc.perform(put("/api/me/profile").with(jwt().jwt(token -> token.subject(SUBJECT)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"displayName\":\"" + "x".repeat(41) + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void storesAnUploadedPictureAndLinksIt() throws Exception {
        Player player = Player.create(SUBJECT, "lydia");
        when(playerService.currentPlayer(any(Jwt.class))).thenReturn(player);
        when(playerService.changeAvatar(eq(player), anyString())).thenAnswer(call -> {
            player.changeAvatar(call.getArgument(1));
            return player;
        });
        when(storage.temporaryUrl(anyString())).thenReturn(URI.create("https://storage.test/avatar.png"));

        mockMvc.perform(multipart("/api/me/avatar")
                        .file(new MockMultipartFile("file", "me.png", "image/png", png()))
                        .with(jwt().jwt(token -> token.subject(SUBJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").value("https://storage.test/avatar.png"));

        verify(storage).store(startsWith("avatars/" + player.id() + "/"), any(), eq("image/png"));
        verify(storage, never()).delete(anyString());
    }

    @Test
    void removingThePictureDeletesTheStoredObject() throws Exception {
        Player player = Player.create(SUBJECT, "lydia");
        player.changeAvatar("avatars/old.png");
        when(playerService.currentPlayer(any(Jwt.class))).thenReturn(player);
        when(playerService.changeAvatar(player, null)).thenAnswer(call -> {
            player.changeAvatar(null);
            return player;
        });

        mockMvc.perform(delete("/api/me/avatar").with(jwt().jwt(token -> token.subject(SUBJECT))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").doesNotExist());

        verify(storage).delete("avatars/old.png");
    }

    @Test
    void rejectsRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    private static byte[] png() throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB), "png", bytes);
        return bytes.toByteArray();
    }
}

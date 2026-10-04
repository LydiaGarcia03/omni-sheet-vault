package dev.omnisheetvault.api.character;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.shared.SecurityConfig;
import java.net.URI;
import java.util.UUID;
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

@WebMvcTest(CharacterPortraitController.class)
@Import(SecurityConfig.class)
class CharacterPortraitControllerTest {

    private static final UUID ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CharacterPortraitService portraitService;

    @Test
    void choosesAPreset() throws Exception {
        when(portraitService.choosePreset(any(Jwt.class), eq(ID), eq("10064"))).thenReturn(PortraitResponse.preset("10064"));

        mockMvc.perform(put("/api/characters/" + ID + "/portrait").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"presetId\":\"10064\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("PRESET"))
                .andExpect(jsonPath("$.presetId").value("10064"));
    }

    @Test
    void rejectsAPresetIdThatIsNotAnId() throws Exception {
        mockMvc.perform(put("/api/characters/" + ID + "/portrait").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"presetId\":\"../../etc\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void uploadsAFile() throws Exception {
        byte[] content = {1, 2, 3};
        when(portraitService.upload(any(Jwt.class), eq(ID), eq(content)))
                .thenReturn(PortraitResponse.upload(URI.create("http://localhost:9000/portraits/x.png?X-Amz-Signature=s")));

        mockMvc.perform(multipart("/api/characters/" + ID + "/portrait").file(new MockMultipartFile("file", "me.png", "image/png", content))
                        .with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value("UPLOAD"))
                .andExpect(jsonPath("$.url").value("http://localhost:9000/portraits/x.png?X-Amz-Signature=s"));
    }

    @Test
    void anUnreadableUploadIsABadRequest() throws Exception {
        when(portraitService.upload(any(Jwt.class), eq(ID), any())).thenThrow(new InvalidPortraitException("The portrait must be a PNG or JPEG image."));

        mockMvc.perform(multipart("/api/characters/" + ID + "/portrait").file(new MockMultipartFile("file", "me.gif", "image/gif", new byte[] {1}))
                        .with(jwt()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("The portrait must be a PNG or JPEG image."));
    }

    @Test
    void removesThePortrait() throws Exception {
        mockMvc.perform(delete("/api/characters/" + ID + "/portrait").with(jwt()))
                .andExpect(status().isNoContent());

        verify(portraitService).remove(any(Jwt.class), eq(ID));
    }

    @Test
    void requiresASignedInPlayer() throws Exception {
        mockMvc.perform(delete("/api/characters/" + ID + "/portrait").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}

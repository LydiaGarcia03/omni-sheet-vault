package dev.omnisheetvault.api.catalogue;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import dev.omnisheetvault.api.shared.SecurityConfig;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

@WebMvcTest(CatalogueController.class)
@Import(SecurityConfig.class)
class CatalogueControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @MockitoBean
    private CatalogueService catalogueService;

    @Test
    void listsEntriesForASystem() throws Exception {
        ObjectNode data = objectMapper.createObjectNode().put("level", 1);
        CatalogueEntryResponse entry = new CatalogueEntryResponse(
                UUID.randomUUID().toString(), "dnd-5e", CatalogueEntryKind.SPELL, "sparkling-bolt", "Sparkling Bolt",
                "Test Fixtures", 1, List.of("evocation"), new RedactableText("A fictional test spell.", false), data);
        when(catalogueService.list(eq("dnd-5e"), isNull())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/catalogue?systemId=dnd-5e").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Sparkling Bolt"))
                .andExpect(jsonPath("$[0].description.value").value("A fictional test spell."))
                .andExpect(jsonPath("$[0].description.redacted").value(false))
                .andExpect(jsonPath("$[0].data.level").value(1));
    }

    @Test
    void filtersByKind() throws Exception {
        when(catalogueService.list(eq("dnd-5e"), eq(CatalogueEntryKind.ITEM))).thenReturn(List.of());

        mockMvc.perform(get("/api/catalogue?systemId=dnd-5e&kind=ITEM").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void returnsARedactedEntry() throws Exception {
        ObjectNode data = objectMapper.createObjectNode();
        CatalogueEntryResponse entry = new CatalogueEntryResponse(
                UUID.randomUUID().toString(), "dnd-5e", CatalogueEntryKind.SPELL, "sparkling-bolt", "Sparkling Bolt",
                null, null, List.of(), new RedactableText(null, true), data);
        when(catalogueService.list(eq("dnd-5e"), isNull())).thenReturn(List.of(entry));

        mockMvc.perform(get("/api/catalogue?systemId=dnd-5e").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description.redacted").value(true))
                .andExpect(jsonPath("$[0].description.value").doesNotExist());
    }

    @Test
    void returnsNotFoundForAMissingEntry() throws Exception {
        UUID missingId = UUID.randomUUID();
        when(catalogueService.get(eq(missingId))).thenThrow(new CatalogueEntryNotFoundException(missingId));

        mockMvc.perform(get("/api/catalogue/" + missingId).with(jwt()))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsRequestsWithoutAToken() throws Exception {
        mockMvc.perform(get("/api/catalogue?systemId=dnd-5e")).andExpect(status().isUnauthorized());
    }

    @Test
    void requiresASystemId() throws Exception {
        mockMvc.perform(get("/api/catalogue").with(jwt())).andExpect(status().isBadRequest());
    }
}

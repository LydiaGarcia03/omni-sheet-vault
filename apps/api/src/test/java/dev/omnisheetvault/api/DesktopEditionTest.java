package dev.omnisheetvault.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import dev.omnisheetvault.api.storage.PortraitStorage;
import jakarta.servlet.http.Cookie;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

/** The desktop edition end to end: embedded PostgreSQL, the local player, portraits on disk and the catalogue import. */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("desktop")
@DirtiesContext
class DesktopEditionTest {

    /** Shared by every test JVM run: the embedded-postgres library remembers where it unpacked the binaries. */
    static final Path EMBEDDED_POSTGRES_RUNTIME = Path.of("build/embedded-postgres");

    private static final List<String> CONDITIONS = List.of("blinded.json", "charmed.json", "deafened.json");

    @TempDir
    static Path dataDirectory;

    @TempDir
    static Path contentDirectory;

    @TempDir
    static Path webDirectory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PortraitStorage portraitStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    static void bundleContentAndAWebApp() throws IOException {
        bundleThreeConditions();
        bundleAWebApp();
    }

    private static void bundleThreeConditions() throws IOException {
        Path source = Path.of("../../content/dnd-5e/conditions");
        Path target = Files.createDirectories(contentDirectory.resolve("dnd-5e/conditions"));
        for (String file : CONDITIONS) {
            Files.copy(source.resolve(file), target.resolve(file));
        }
    }

    private static void bundleAWebApp() throws IOException {
        Files.writeString(webDirectory.resolve("index.html"), "<html>the vault</html>");
        Files.writeString(Files.createDirectories(webDirectory.resolve("assets")).resolve("app-1a2b.js"), "console.log('vault')");
    }

    @DynamicPropertySource
    static void desktopFolders(DynamicPropertyRegistry registry) {
        registry.add("app.desktop.data-directory", () -> dataDirectory.toString());
        registry.add("app.desktop.content-directory", () -> contentDirectory.toString());
        registry.add("app.desktop.web-directory", () -> webDirectory.toString());
        registry.add("app.desktop.runtime-directory", () -> EMBEDDED_POSTGRES_RUNTIME.toString());
    }

    @Test
    void theDatabaseLivesInTheDataFolder() {
        assertThat(dataDirectory.resolve("db/postgresql.conf")).isRegularFile();
    }

    @Test
    void everyRequestIsTheMachinesLocalPlayer() throws Exception {
        mockMvc.perform(get("/api/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("local-player"))
                .andExpect(jsonPath("$.displayName").value(System.getProperty("user.name")));
    }

    @Test
    void aRequestAddressedToAnotherHostIsRefused() throws Exception {
        mockMvc.perform(get("/api/me").with(request -> {
                    request.setServerName("evil.example");
                    return request;
                }))
                .andExpect(status().isForbidden());
    }

    @Test
    void writesStillNeedTheCsrfToken() throws Exception {
        mockMvc.perform(post("/api/characters").contentType("application/json").content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void theCsrfTokenStaysValidAcrossRequestsSoTheWebAppCanWrite() throws Exception {
        Cookie token = mockMvc.perform(get("/api/me")).andReturn().getResponse().getCookie("XSRF-TOKEN");
        assertThat(token).isNotNull();

        mockMvc.perform(get("/api/me").cookie(token))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist("XSRF-TOKEN"));

        String created = mockMvc.perform(post("/api/characters")
                        .cookie(token)
                        .header("X-XSRF-TOKEN", token.getValue())
                        .contentType("application/json")
                        .content("{\"name\":\"Mez\",\"systemId\":\"dnd-5e\"}"))
                .andExpect(status().isCreated())
                .andExpect(cookie().doesNotExist("XSRF-TOKEN"))
                .andReturn().getResponse().getContentAsString();

        String id = JsonPath.read(created, "$.id");
        mockMvc.perform(delete("/api/characters/" + id).cookie(token).header("X-XSRF-TOKEN", token.getValue()))
                .andExpect(status().isNoContent());
    }

    @Test
    void theAppServesTheWebAppOnItsOwnOrigin() throws Exception {
        mockMvc.perform(get("/")).andExpect(forwardedUrl("/index.html"));
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string("<html>the vault</html>"))
                .andExpect(header().string("Cache-Control", containsString("no-cache")));
        mockMvc.perform(get("/assets/app-1a2b.js"))
                .andExpect(status().isOk())
                .andExpect(content().string("console.log('vault')"))
                .andExpect(header().string("Cache-Control", containsString("max-age=31536000")));
    }

    @Test
    void anyPathTheRouterOwnsOpensTheWebApp() throws Exception {
        mockMvc.perform(get("/characters/" + UUID.randomUUID() + "/build/species"))
                .andExpect(status().isOk())
                .andExpect(content().string("<html>the vault</html>"));
    }

    @Test
    void anUnknownApiPathStaysA404() throws Exception {
        mockMvc.perform(get("/api/does-not-exist")).andExpect(status().isNotFound());
    }

    @Test
    void theBundledCatalogueIsImportedOnTheFirstStart() {
        Integer entries = jdbcTemplate.queryForObject("select count(*) from catalogue_entries", Integer.class);

        assertThat(entries).isEqualTo(CONDITIONS.size());
        assertThat(dataDirectory.resolve("catalogue.fingerprint")).isRegularFile();
    }

    @Test
    void uploadedPortraitsAreFilesServedBackByTheApp() throws Exception {
        String key = "uploads/" + UUID.randomUUID() + "/" + UUID.randomUUID() + ".png";
        byte[] image = {(byte) 0x89, 'P', 'N', 'G'};

        portraitStorage.store(key, image, "image/png");

        assertThat(dataDirectory.resolve("portraits").resolve(key)).hasBinaryContent(image);
        mockMvc.perform(get(portraitStorage.temporaryUrl(key).toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/png"))
                .andExpect(content().bytes(image));
    }

    @Test
    void onlyPortraitFileNamesAreServed() throws Exception {
        mockMvc.perform(get("/api/portrait-files/uploads/" + UUID.randomUUID() + "/notes.txt"))
                .andExpect(status().isNotFound());
    }
}

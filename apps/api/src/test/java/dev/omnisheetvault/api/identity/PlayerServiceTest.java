package dev.omnisheetvault.api.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@Transactional
class PlayerServiceTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void createsAPlayerOnFirstSightOfASubject() {
        Jwt jwt = jwtFor("33333333-3333-3333-3333-333333333333", "new-player");

        Player player = playerService.currentPlayer(jwt);

        assertThat(player.subject()).isEqualTo("33333333-3333-3333-3333-333333333333");
        assertThat(player.displayName()).isEqualTo("new-player");
        /*
         * Compares by id, not whole-object equality: the row is created in its own
         * transaction (see PlayerService's own doc comment on createOrFindPlayer), a
         * separate JPA persistence context from this test's, so the re-read below is
         * a genuinely different Java object for the same row — Player has no
         * equals()/hashCode() override, same precedent as reusesTheExistingRowForAKnownSubject.
         */
        assertThat(playerRepository.findBySubject("33333333-3333-3333-3333-333333333333"))
                .hasValueSatisfying(stored -> assertThat(stored.id()).isEqualTo(player.id()));
    }

    @Test
    void reusesTheExistingRowForAKnownSubject() {
        Jwt jwt = jwtFor("44444444-4444-4444-4444-444444444444", "returning-player");

        Player first = playerService.currentPlayer(jwt);
        Player second = playerService.currentPlayer(jwt);

        assertThat(second.id()).isEqualTo(first.id());
        assertThat(playerRepository.findBySubject("44444444-4444-4444-4444-444444444444")).hasValueSatisfying(
                stored -> assertThat(stored.id()).isEqualTo(first.id()));
    }

    private Jwt jwtFor(String subject, String preferredUsername) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("preferred_username", preferredUsername)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}

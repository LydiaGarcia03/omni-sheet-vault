package dev.omnisheetvault.api.dice;

import static org.assertj.core.api.Assertions.assertThat;

import dev.omnisheetvault.api.character.Character;
import dev.omnisheetvault.api.character.CharacterService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * The first {@code int[]} column in this codebase — worth a real round trip against
 * Postgres, not just a mock. A valid {@code character_id} comes from
 * {@link CharacterService}; the sheet payload itself is irrelevant here since only the
 * {@code rolls} table is under test.
 */
@SpringBootTest
@Testcontainers
@Transactional
class RollRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private RollRepository rollRepository;

    @Autowired
    private CharacterService characterService;

    @Test
    void savesAndReadsBackTheResultsArray() {
        Character character = characterService.create(jwtFor("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa"), "Aria", "dnd-5e");

        rollRepository.save(Roll.create(character.id(), "1d20+3", "Strength: check", new int[] {14}, 17));

        List<Roll> history = rollRepository.findByCharacterIdOrderByRolledAtDesc(character.id());
        assertThat(history).hasSize(1);
        assertThat(history.get(0).results()).containsExactly(14);
        assertThat(history.get(0).total()).isEqualTo(17);
        assertThat(history.get(0).context()).isEqualTo("Strength: check");
    }

    @Test
    void ordersHistoryNewestFirst() throws InterruptedException {
        Character character = characterService.create(jwtFor("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb"), "Borin", "dnd-5e");

        rollRepository.save(Roll.create(character.id(), "1d20+3", "Strength: check", new int[] {5}, 8));
        Thread.sleep(5);
        rollRepository.save(Roll.create(character.id(), "1d20+1", "Initiative: roll", new int[] {9}, 10));

        List<Roll> history = rollRepository.findByCharacterIdOrderByRolledAtDesc(character.id());
        assertThat(history).extracting(Roll::context).containsExactly("Initiative: roll", "Strength: check");
    }

    private Jwt jwtFor(String subject) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .subject(subject)
                .claim("preferred_username", "player-" + subject)
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(60))
                .build();
    }
}

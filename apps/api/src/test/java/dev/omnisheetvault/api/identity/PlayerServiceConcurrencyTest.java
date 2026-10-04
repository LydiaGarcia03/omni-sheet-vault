package dev.omnisheetvault.api.identity;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.oauth2.jwt.Jwt;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Not {@code @Transactional}, unlike PlayerServiceTest — a shared test transaction
 * would serialize what this test needs to genuinely race: several real, concurrent
 * database connections all racing to create the first row for the same subject. See
 * PlayerService's own doc comment for the bug this reproduces and the fix.
 */
@SpringBootTest
@Testcontainers
class PlayerServiceConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private PlayerService playerService;

    @Autowired
    private PlayerRepository playerRepository;

    @Test
    void concurrentFirstSightOfTheSameSubjectResolvesToExactlyOnePlayer() throws Exception {
        String subject = "55555555-5555-5555-5555-555555555555";
        Jwt jwt = jwtFor(subject, "racing-player");
        int concurrentRequests = 8;
        ExecutorService executor = Executors.newFixedThreadPool(concurrentRequests);
        CountDownLatch ready = new CountDownLatch(concurrentRequests);
        CountDownLatch start = new CountDownLatch(1);

        try {
            List<Future<Player>> futures = IntStream.range(0, concurrentRequests)
                    .mapToObj(i -> executor.submit(() -> {
                        ready.countDown();
                        start.await();
                        return playerService.currentPlayer(jwt);
                    }))
                    .toList();

            ready.await();
            start.countDown();

            List<UUID> playerIds = new ArrayList<>();
            for (Future<Player> future : futures) {
                playerIds.add(future.get(10, TimeUnit.SECONDS).id());
            }

            assertThat(playerIds).containsOnly(playerIds.get(0));
            assertThat(playerRepository.findBySubject(subject)).hasValueSatisfying(
                    stored -> assertThat(stored.id()).isEqualTo(playerIds.get(0)));
        } finally {
            executor.shutdownNow();
        }
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

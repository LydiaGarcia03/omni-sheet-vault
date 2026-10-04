package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.identity.Player;
import dev.omnisheetvault.api.identity.PlayerService;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * Applies every build file in a directory to one local player, then exits:
 * {@code bootRun --args='--apply-builds=<dir> --player=<username>'}. Dev profile only.
 */
@Profile("dev")
@Component
class CharacterBuildRunner implements CommandLineRunner {

    private static final String BUILDS_ARG = "--apply-builds=";
    private static final String PLAYER_ARG = "--player=";
    private static final Logger LOG = LoggerFactory.getLogger(CharacterBuildRunner.class);

    private final CharacterBuildApplier applier;
    private final PlayerService playerService;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;

    CharacterBuildRunner(CharacterBuildApplier applier, PlayerService playerService, ObjectMapper objectMapper,
            ApplicationContext applicationContext) {
        this.applier = applier;
        this.playerService = playerService;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        Optional<String> directory = argument(args, BUILDS_ARG);
        if (directory.isEmpty()) {
            return;
        }
        int exitCode = applyAll(Path.of(directory.get()), argument(args, PLAYER_ARG).orElse(null));
        System.exit(SpringApplication.exit(applicationContext, () -> exitCode));
    }

    private int applyAll(Path directory, String playerName) {
        if (playerName == null) {
            LOG.error("--apply-builds needs --player=<username>");
            return 1;
        }
        List<Player> players = playerService.playersNamed(playerName);
        if (players.size() != 1) {
            LOG.error("Expected exactly one player named {}, found {}; log in once so the player exists", playerName, players.size());
            return 1;
        }
        int failures = 0;
        for (Path file : buildFiles(directory)) {
            try {
                CharacterBuildFile build = objectMapper.readValue(Files.readString(file), CharacterBuildFile.class);
                CharacterBuildApplier.Outcome outcome = applier.apply(players.getFirst().id(), build);
                LOG.info("{} {} from {}", outcome, build.characterName(), file.getFileName());
            } catch (IOException | RuntimeException e) {
                failures++;
                LOG.error("Could not apply {}: {}", file.getFileName(), e.getMessage());
            }
        }
        return failures == 0 ? 0 : 1;
    }

    private static List<Path> buildFiles(Path directory) {
        try (DirectoryStream<Path> files = Files.newDirectoryStream(directory, "*.json")) {
            return StreamSupport.stream(files.spliterator(), false).sorted().toList();
        } catch (IOException e) {
            throw new IllegalStateException("Could not read build directory " + directory, e);
        }
    }

    private static Optional<String> argument(String[] args, String prefix) {
        return Arrays.stream(args).filter(arg -> arg.startsWith(prefix)).map(arg -> arg.substring(prefix.length())).findFirst();
    }
}

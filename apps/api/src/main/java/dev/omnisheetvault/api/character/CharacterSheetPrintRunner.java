package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.identity.Player;
import dev.omnisheetvault.api.identity.PlayerService;
import dev.omnisheetvault.api.ruleset.registry.SheetCalculatorRegistry;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;

/**
 * Writes one local player's characters to a directory, then exits: {@code <name>.sheet.json} is the calculated
 * sheet exactly as the API returns it, {@code <name>.stored.json} the stored sheet it came from. For audits:
 * {@code bootRun --args='--print-sheets=<dir> --player=<username> --server.port=0'}. Dev profile only.
 */
@Profile("dev")
@Component
class CharacterSheetPrintRunner implements CommandLineRunner {

    private static final String DIRECTORY_ARG = "--print-sheets=";
    private static final String PLAYER_ARG = "--player=";
    private static final Logger LOG = LoggerFactory.getLogger(CharacterSheetPrintRunner.class);

    private final CharacterRepository characterRepository;
    private final SheetCalculatorRegistry sheetCalculatorRegistry;
    private final PlayerService playerService;
    private final ObjectMapper objectMapper;
    private final ApplicationContext applicationContext;

    CharacterSheetPrintRunner(CharacterRepository characterRepository, SheetCalculatorRegistry sheetCalculatorRegistry,
            PlayerService playerService, ObjectMapper objectMapper, ApplicationContext applicationContext) {
        this.characterRepository = characterRepository;
        this.sheetCalculatorRegistry = sheetCalculatorRegistry;
        this.playerService = playerService;
        this.objectMapper = objectMapper;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        Optional<String> directory = argument(args, DIRECTORY_ARG);
        if (directory.isEmpty()) {
            return;
        }
        int exitCode = printAll(Path.of(directory.get()), argument(args, PLAYER_ARG).orElse(null));
        System.exit(SpringApplication.exit(applicationContext, () -> exitCode));
    }

    private int printAll(Path directory, String playerName) {
        List<Player> players = playerName == null ? List.of() : playerService.playersNamed(playerName);
        if (players.size() != 1) {
            LOG.error("--print-sheets needs --player=<username> naming exactly one player");
            return 1;
        }
        try {
            Files.createDirectories(directory);
            for (Character character : characterRepository.findByPlayerIdAndDeletedAtIsNull(players.getFirst().id())) {
                if (character.isDraft()) {
                    continue;
                }
                String fileName = character.name().toLowerCase().replaceAll("[^a-z0-9]+", "-");
                CharacterSheetResponse sheet = CharacterSheetResponse.from(
                        sheetCalculatorRegistry.forSystem(character.systemId()).calculateVitals(character.sheet()));
                Files.writeString(directory.resolve(fileName + ".sheet.json"),
                        objectMapper.writer().with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(sheet));
                Files.writeString(directory.resolve(fileName + ".stored.json"), objectMapper.writer()
                        .with(SerializationFeature.INDENT_OUTPUT).writeValueAsString(objectMapper.readTree(character.sheet())));
                LOG.info("Printed {}", character.name());
            }
            return 0;
        } catch (IOException e) {
            LOG.error("Could not write sheets to {}: {}", directory, e.getMessage());
            return 1;
        }
    }

    private static Optional<String> argument(String[] args, String prefix) {
        return Arrays.stream(args).filter(arg -> arg.startsWith(prefix)).map(arg -> arg.substring(prefix.length())).findFirst();
    }
}

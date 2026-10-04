package dev.omnisheetvault.api.character;

import dev.omnisheetvault.api.catalogue.CatalogueService;
import dev.omnisheetvault.api.identity.Player;
import dev.omnisheetvault.api.identity.PlayerService;
import dev.omnisheetvault.api.ruleset.CharacterCreationFlow;
import dev.omnisheetvault.api.ruleset.MaterializedSheet;
import dev.omnisheetvault.api.ruleset.registry.CharacterCreationFlowRegistry;
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
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * Re-applies each of a player's characters' own stored build, keeping its play state, then exits:
 * {@code bootRun --args='--rematerialize --player=<username>'}. Dev profile only.
 */
@Profile("dev")
@Component
class CharacterRematerializeRunner implements CommandLineRunner {

    private static final String FLAG = "--rematerialize";
    private static final String PLAYER_ARG = "--player=";
    private static final Logger LOG = LoggerFactory.getLogger(CharacterRematerializeRunner.class);

    private final CharacterRepository characterRepository;
    private final CharacterCreationFlowRegistry creationFlows;
    private final CatalogueService catalogueService;
    private final PlayerService playerService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactions;
    private final ApplicationContext applicationContext;

    CharacterRematerializeRunner(CharacterRepository characterRepository, CharacterCreationFlowRegistry creationFlows,
            CatalogueService catalogueService, PlayerService playerService, ObjectMapper objectMapper,
            TransactionTemplate transactions, ApplicationContext applicationContext) {
        this.characterRepository = characterRepository;
        this.creationFlows = creationFlows;
        this.catalogueService = catalogueService;
        this.playerService = playerService;
        this.objectMapper = objectMapper;
        this.transactions = transactions;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        if (Arrays.stream(args).noneMatch(FLAG::equals)) {
            return;
        }
        Optional<String> playerName = Arrays.stream(args).filter(arg -> arg.startsWith(PLAYER_ARG))
                .map(arg -> arg.substring(PLAYER_ARG.length())).findFirst();
        int exitCode = playerName.map(this::rematerializeAll).orElseGet(() -> {
            LOG.error("--rematerialize needs --player=<username>");
            return 1;
        });
        System.exit(SpringApplication.exit(applicationContext, () -> exitCode));
    }

    private int rematerializeAll(String playerName) {
        List<Player> players = playerService.playersNamed(playerName);
        if (players.size() != 1) {
            LOG.error("Expected exactly one player named {}, found {}", playerName, players.size());
            return 1;
        }
        int failures = 0;
        for (Character character : characterRepository.findByPlayerIdAndDeletedAtIsNull(players.getFirst().id())) {
            try {
                LOG.info("{}: {}", character.name(), transactions.execute(status -> rematerialize(character)));
            } catch (RuntimeException e) {
                failures++;
                LOG.error("Could not re-apply {}: {}", character.name(), e.getMessage());
            }
        }
        return failures == 0 ? 0 : 1;
    }

    private String rematerialize(Character character) {
        if (character.isDraft()) {
            return "skipped (draft)";
        }
        CharacterCreationFlow flow = creationFlows.forSystem(character.systemId());
        Optional<String> build = flow.buildOf(character.sheet());
        if (build.isEmpty()) {
            return "skipped (no stored build)";
        }
        MaterializedSheet materialized = flow.materialize(character.sheet(), build.get(), catalogueService.lookup(character.systemId()));
        if (!materialized.isMaterialized()) {
            return "skipped (build has pending choices or problems)";
        }
        character.replaceSheet(materialized.sheetJson(), objectMapper.readTree(materialized.sheetJson()).path("schemaVersion").asInt());
        characterRepository.save(character);
        return "re-applied";
    }
}

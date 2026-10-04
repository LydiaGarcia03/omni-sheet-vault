package dev.omnisheetvault.api.catalogue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Runs the catalogue import only when explicitly invoked with
 * {@code --import-catalogue=<directory>} — never at ordinary startup, per
 * roadmap.md phase 7 ("An import pipeline, run as a command rather than at
 * application startup"). Exits the process afterward instead of starting the web
 * server: {@code ./gradlew :apps:api:bootRun --args='--import-catalogue=path/to/content'}.
 */
@Component
class CatalogueImportRunner implements CommandLineRunner {

    private static final String ARG_PREFIX = "--import-catalogue=";
    private static final Logger LOG = LoggerFactory.getLogger(CatalogueImportRunner.class);

    private final CatalogueImportService catalogueImportService;
    private final ApplicationContext applicationContext;

    CatalogueImportRunner(CatalogueImportService catalogueImportService, ApplicationContext applicationContext) {
        this.catalogueImportService = catalogueImportService;
        this.applicationContext = applicationContext;
    }

    @Override
    public void run(String... args) {
        Optional<String> directoryArg = Arrays.stream(args)
                .filter(arg -> arg.startsWith(ARG_PREFIX))
                .map(arg -> arg.substring(ARG_PREFIX.length()))
                .findFirst();

        if (directoryArg.isEmpty()) {
            return;
        }

        int imported = catalogueImportService.importFrom(Path.of(directoryArg.get()));
        LOG.info("Imported {} catalogue entries from {}", imported, directoryArg.get());
        System.exit(SpringApplication.exit(applicationContext, () -> 0));
    }
}

package dev.omnisheetvault.api.catalogue;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import tools.jackson.databind.ObjectMapper;

/**
 * Entry point for the 5etools ingestion pipeline — systems/dnd-5e/features/5etools-ingestion.md's
 * "Tool shape": a dependency-free {@code JavaExec} (no Spring context), run via
 * {@code ./gradlew :apps:api:ingest5etools}. Thin CLI wrapper only — see
 * {@link Ingest5eToolsRunner} for the actual, independently-testable orchestration.
 *
 * <p>Options (all optional): {@code --data=<dir>} (default {@code tools/5etools-data/data},
 * the owner's local gitignored copy — see tech-stack.md; a 5etools-src release zip
 * extracts its own {@code data/} subdirectory one level down, so the default points
 * there rather than at the repo checkout's own root), {@code --kind=<name>} (default:
 * every registered converter).
 */
public final class Ingest5eToolsMain {

    private static final String DEFAULT_DATA_DIRECTORY = "tools/5etools-data/data";
    private static final String CONTENT_ROOT = "content/dnd-5e";

    private Ingest5eToolsMain() {
    }

    public static void main(String[] args) {
        Map<String, String> options = parseArgs(args);
        ObjectMapper objectMapper = new ObjectMapper();
        FiveEToolsDataSource dataSource =
                new FiveEToolsDataSource(Path.of(options.getOrDefault("data", DEFAULT_DATA_DIRECTORY)), objectMapper);
        FiveEToolsSourceClassifier classifier = FiveEToolsSourceClassifier.forRules2014(FiveEToolsSourceNames.load(dataSource));
        Ingest5eToolsRunner runner = new Ingest5eToolsRunner(objectMapper);
        String kindFilter = options.get("kind");

        FiveEToolsMechanicsOverlay overlay = FiveEToolsMechanicsOverlay.load(Path.of(CONTENT_ROOT, "mechanics"), objectMapper);
        for (FiveEToolsConverter converter : registeredConverters(objectMapper, overlay)) {
            if (kindFilter != null && !converter.kind().name().equalsIgnoreCase(kindFilter)) {
                continue;
            }
            Path outputDirectory = CatalogueContentLayout.directory(Path.of(CONTENT_ROOT), converter.kind());
            int count = runner.run(converter, dataSource, classifier, outputDirectory);
            System.out.printf("Converted %d %s entries into %s%n", count, converter.kind(), outputDirectory);
        }
    }

    private static List<FiveEToolsConverter> registeredConverters(ObjectMapper objectMapper, FiveEToolsMechanicsOverlay overlay) {
        return List.of(
                new SpellConverter(objectMapper, overlay),
                new ItemConverter(objectMapper),
                new FeatConverter(objectMapper, overlay),
                new ClassConverter(objectMapper, overlay),
                new SubclassConverter(objectMapper, overlay),
                new OptionalFeatureConverter(objectMapper, overlay),
                new SpeciesConverter(objectMapper, overlay),
                new BackgroundConverter(objectMapper),
                new LanguageConverter(objectMapper),
                new ConditionConverter(objectMapper, overlay));
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (String arg : args) {
            int equals = arg.startsWith("--") ? arg.indexOf('=') : -1;
            if (equals > 0) {
                options.put(arg.substring(2, equals), arg.substring(equals + 1));
            }
        }
        return options;
    }
}

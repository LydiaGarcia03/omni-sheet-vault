package dev.omnisheetvault.api.desktop;

import com.zaxxer.hikari.HikariDataSource;
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * The desktop edition's database: real PostgreSQL binaries started by the app, listening on a free local port, with
 * the data kept in the user's data folder across restarts and updates. Flyway migrates it like any other database.
 */
@Configuration(proxyBeanMethods = false)
@Profile("desktop")
@EnableConfigurationProperties(DesktopProperties.class)
class EmbeddedDatabaseConfig {

    static final String DATABASE = "postgres";
    static final String USER = "postgres";
    private static final Duration LEFTOVER_STOP_TIMEOUT = Duration.ofSeconds(60);
    private static final Logger LOG = LoggerFactory.getLogger(EmbeddedDatabaseConfig.class);

    @Bean(destroyMethod = "close")
    EmbeddedPostgres embeddedPostgres(DesktopProperties properties) {
        return start(properties);
    }

    @Bean(destroyMethod = "close")
    DataSource dataSource(EmbeddedPostgres postgres) {
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(postgres.getJdbcUrl(USER, DATABASE));
        dataSource.setUsername(USER);
        dataSource.setMaximumPoolSize(5);
        return dataSource;
    }

    static EmbeddedPostgres start(DesktopProperties properties) {
        try {
            Files.createDirectories(properties.dataPath());
            Files.createDirectories(properties.runtimePath());
            stopLeftoverServer(properties);
            return EmbeddedPostgres.builder()
                    .setDataDirectory(properties.databasePath())
                    .setCleanDataDirectory(false)
                    .setOverrideWorkingDirectory(properties.runtimePath().toFile())
                    .start();
        } catch (IOException e) {
            throw new UncheckedIOException("Could not start the local database in " + properties.databasePath(), e);
        }
    }

    /**
     * An app that was killed (rather than closed) leaves its PostgreSQL running on the data folder, which would keep
     * the next start from opening it. That server is stopped first; with none running, {@code pg_ctl} just says so.
     */
    private static void stopLeftoverServer(DesktopProperties properties) throws IOException {
        if (!Files.exists(properties.databasePath().resolve("postmaster.pid"))) {
            return;
        }
        Optional<Path> pgCtl = findPgCtl(properties.runtimePath());
        if (pgCtl.isEmpty()) {
            return;
        }
        LOG.info("Stopping a local database left running by an earlier session");
        Process stop = new ProcessBuilder(pgCtl.get().toString(), "stop", "-D", properties.databasePath().toString(), "-m", "fast", "-w")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
        try {
            if (!stop.waitFor(LEFTOVER_STOP_TIMEOUT.toSeconds(), TimeUnit.SECONDS)) {
                stop.destroyForcibly();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static Optional<Path> findPgCtl(Path runtime) throws IOException {
        try (Stream<Path> files = Files.walk(runtime)) {
            return files.filter(file -> file.getFileName().toString().matches("pg_ctl(\\.exe)?")).findFirst();
        }
    }
}

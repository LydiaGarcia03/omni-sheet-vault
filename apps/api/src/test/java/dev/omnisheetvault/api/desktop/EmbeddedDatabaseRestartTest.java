package dev.omnisheetvault.api.desktop;

import static org.assertj.core.api.Assertions.assertThat;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class EmbeddedDatabaseRestartTest {

    @TempDir
    Path dataDirectory;

    private DesktopProperties properties() {
        return new DesktopProperties(
                dataDirectory.toString(), dataDirectory.resolve("content").toString(), dataDirectory.resolve("web").toString(),
                "build/embedded-postgres");
    }

    @Test
    void aDatabaseLeftRunningByAKilledAppIsStoppedBeforeTheNextStart() throws Exception {
        DesktopProperties properties = properties();
        EmbeddedDatabaseConfig.start(properties).close();
        startAnOrphanServer(properties);

        try (EmbeddedPostgres next = EmbeddedDatabaseConfig.start(properties);
                Connection connection = next.getPostgresDatabase().getConnection();
                Statement statement = connection.createStatement();
                ResultSet one = statement.executeQuery("select 1")) {
            assertThat(one.next()).isTrue();
        }
    }

    /** A server with no Java process around it, as a killed app leaves behind. */
    private static void startAnOrphanServer(DesktopProperties properties) throws Exception {
        Path pgCtl;
        try (Stream<Path> files = Files.walk(properties.runtimePath())) {
            pgCtl = files.filter(file -> file.getFileName().toString().matches("pg_ctl(\\.exe)?")).findFirst().orElseThrow();
        }
        Process start = new ProcessBuilder(pgCtl.toString(), "start", "-D", properties.databasePath().toString(), "-o", "-p 55432", "-w")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .start();
        assertThat(start.waitFor(60, TimeUnit.SECONDS)).isTrue();
        assertThat(start.exitValue()).isZero();
    }

    @Test
    void whatWasSavedIsStillThereAfterTheAppRestarts() throws Exception {
        DesktopProperties properties = properties();

        try (EmbeddedPostgres first = EmbeddedDatabaseConfig.start(properties);
                Connection connection = first.getPostgresDatabase().getConnection();
                Statement statement = connection.createStatement()) {
            statement.execute("create table heroes (name text)");
            statement.execute("insert into heroes values ('Mez')");
        }

        try (EmbeddedPostgres second = EmbeddedDatabaseConfig.start(properties);
                Connection connection = second.getPostgresDatabase().getConnection();
                Statement statement = connection.createStatement();
                ResultSet heroes = statement.executeQuery("select name from heroes")) {
            assertThat(heroes.next()).isTrue();
            assertThat(heroes.getString("name")).isEqualTo("Mez");
        }
    }
}

package dev.omnisheetvault.desktop;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SingleInstanceTest {

    @TempDir
    Path dataDirectory;

    @Test
    void aSecondCopyFindsTheFirstOneAndItsPort() throws Exception {
        try (SingleInstance first = SingleInstance.acquire(dataDirectory).orElseThrow()) {
            first.recordPort(8095);

            Optional<SingleInstance> second = SingleInstance.acquire(dataDirectory);

            assertThat(second).isEmpty();
            assertThat(SingleInstance.runningPort(dataDirectory)).contains(8095);
        }
    }

    @Test
    void theLockIsFreeAgainOnceTheFirstCopyQuits() throws Exception {
        SingleInstance.acquire(dataDirectory).orElseThrow().close();

        try (SingleInstance next = SingleInstance.acquire(dataDirectory).orElseThrow()) {
            assertThat(SingleInstance.runningPort(dataDirectory)).isEmpty();
        }
    }
}

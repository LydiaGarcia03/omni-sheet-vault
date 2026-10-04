package dev.omnisheetvault.desktop;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.InetAddress;
import java.net.ServerSocket;
import org.junit.jupiter.api.Test;

class PortChooserTest {

    @Test
    void keepsThePreferredPortWhenItIsFree() throws Exception {
        int free;
        try (ServerSocket probe = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            free = probe.getLocalPort();
        }

        assertThat(PortChooser.choose(free)).isEqualTo(free);
    }

    @Test
    void picksAnotherPortWhenThePreferredOneIsTaken() throws Exception {
        try (ServerSocket taken = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            int chosen = PortChooser.choose(taken.getLocalPort());

            assertThat(chosen).isPositive().isNotEqualTo(taken.getLocalPort());
        }
    }
}

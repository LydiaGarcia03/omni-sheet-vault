package dev.omnisheetvault.desktop;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/** The vault's usual local port when it is free, so the address stays the same between runs; any free port otherwise. */
final class PortChooser {

    static final int PREFERRED_PORT = 8095;

    private PortChooser() {
    }

    static int choose() throws IOException {
        return choose(PREFERRED_PORT);
    }

    static int choose(int preferred) throws IOException {
        if (isFree(preferred)) {
            return preferred;
        }
        try (ServerSocket any = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
            return any.getLocalPort();
        }
    }

    private static boolean isFree(int port) {
        try (ServerSocket probe = new ServerSocket(port, 0, InetAddress.getLoopbackAddress())) {
            return true;
        } catch (IOException taken) {
            return false;
        }
    }
}

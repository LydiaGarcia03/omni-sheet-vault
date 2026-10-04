package dev.omnisheetvault.desktop;

import dev.omnisheetvault.api.OmniSheetVaultApplication;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * The desktop edition's entry point: shows the launcher window, starts the vault in the {@code desktop} profile on
 * this machine only, and opens it in the default browser. Quitting closes the app, and with it the database.
 */
public final class DesktopLauncher {

    private final AppFolders folders;
    private final SingleInstance instance;
    private final LauncherWindow window;
    private final AtomicReference<ConfigurableApplicationContext> vault = new AtomicReference<>();
    private volatile String address;

    private DesktopLauncher(AppFolders folders, SingleInstance instance) {
        this.folders = folders;
        this.instance = instance;
        this.window = new LauncherWindow(this::openInBrowser, () -> new Thread(this::quit, "vault-quit").start());
    }

    public static void main(String[] args) throws IOException {
        System.setProperty("java.awt.headless", "false");
        AppFolders folders = AppFolders.detect();
        Optional<SingleInstance> instance = SingleInstance.acquire(folders.dataDirectory());
        if (instance.isEmpty()) {
            SingleInstance.runningPort(folders.dataDirectory()).ifPresent(port -> browse("http://localhost:" + port + "/"));
            return;
        }
        new DesktopLauncher(folders, instance.get()).start(args);
    }

    private void start(String[] args) {
        window.showStarting();
        try {
            int port = PortChooser.choose();
            vault.set(new SpringApplicationBuilder(OmniSheetVaultApplication.class)
                    .profiles("desktop")
                    .headless(false)
                    .run(vaultArguments(folders, port, args)));
            instance.recordPort(port);
            address = "http://localhost:" + port + "/";
            window.showRunning(address);
            openInBrowser();
        } catch (RuntimeException | IOException failure) {
            window.showFailure(failure, folders.logFile());
        }
    }

    /**
     * The folders and port as command-line arguments: those outrank {@code application-desktop.yml}, whose relative
     * defaults would otherwise resolve against whatever folder Windows started the program in.
     */
    static String[] vaultArguments(AppFolders folders, int port, String[] args) {
        List<String> arguments = new ArrayList<>(List.of(
                "--server.port=" + port,
                "--app.desktop.data-directory=" + folders.dataDirectory(),
                "--app.desktop.web-directory=" + folders.webDirectory(),
                "--app.desktop.content-directory=" + folders.contentDirectory()));
        arguments.addAll(List.of(args));
        return arguments.toArray(String[]::new);
    }

    private void openInBrowser() {
        if (address != null) {
            browse(address);
        }
    }

    private void quit() {
        window.showStopping();
        ConfigurableApplicationContext running = vault.get();
        if (running != null) {
            SpringApplication.exit(running);
        }
        try {
            instance.close();
        } catch (IOException ignored) {
            // The lock goes away with the process anyway.
        }
        System.exit(0);
    }

    private static void browse(String address) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(address));
            }
        } catch (IOException couldNotOpen) {
            // The window shows the address, so the player can open it by hand.
        }
    }
}

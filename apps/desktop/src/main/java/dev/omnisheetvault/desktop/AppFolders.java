package dev.omnisheetvault.desktop;

import java.net.URISyntaxException;
import java.nio.file.Path;

/**
 * Where the desktop edition finds its files: the bundled web app and catalogue content sit next to the launcher's
 * jar (the app image's {@code app} folder); the player's data lives in {@code %LOCALAPPDATA%\OmniSheetVault}.
 */
record AppFolders(Path appDirectory, Path dataDirectory) {

    static final String DATA_FOLDER_NAME = "OmniSheetVault";

    static AppFolders detect() {
        return new AppFolders(directoryOf(launcherJar()), userDataDirectory(System.getenv("LOCALAPPDATA"), System.getProperty("user.home")));
    }

    Path webDirectory() {
        return appDirectory.resolve("web");
    }

    Path contentDirectory() {
        return appDirectory.resolve("content");
    }

    Path logFile() {
        return dataDirectory.resolve("logs").resolve("omni-sheet-vault.log");
    }

    static Path userDataDirectory(String localAppData, String userHome) {
        Path base = localAppData == null || localAppData.isBlank() ? Path.of(userHome) : Path.of(localAppData);
        return base.resolve(DATA_FOLDER_NAME);
    }

    static Path directoryOf(Path jar) {
        return jar.toAbsolutePath().getParent();
    }

    private static Path launcherJar() {
        try {
            return Path.of(DesktopLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Cannot locate the launcher's own jar", e);
        }
    }
}

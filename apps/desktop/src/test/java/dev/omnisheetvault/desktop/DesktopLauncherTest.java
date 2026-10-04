package dev.omnisheetvault.desktop;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class DesktopLauncherTest {

    @Test
    void theBundledFoldersAndThePortAreCommandLineArgumentsSoTheyWinOverTheProfileDefaults() {
        AppFolders folders = new AppFolders(Path.of("C:/Games/OmniSheetVault/app"), Path.of("C:/Users/nicole/AppData/Local/OmniSheetVault"));

        String[] arguments = DesktopLauncher.vaultArguments(folders, 8123, new String[] {"--debug"});

        assertThat(arguments).containsExactly(
                "--server.port=8123",
                "--app.desktop.data-directory=" + Path.of("C:/Users/nicole/AppData/Local/OmniSheetVault"),
                "--app.desktop.web-directory=" + Path.of("C:/Games/OmniSheetVault/app/web"),
                "--app.desktop.content-directory=" + Path.of("C:/Games/OmniSheetVault/app/content"),
                "--debug");
    }
}

package dev.omnisheetvault.desktop;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AppFoldersTest {

    @Test
    void theBundledFilesSitNextToTheLauncherJar() {
        Path appDirectory = AppFolders.directoryOf(Path.of("C:/Games/OmniSheetVault/app/omni-sheet-vault-desktop.jar"));
        AppFolders folders = new AppFolders(appDirectory, Path.of("C:/Users/nicole/AppData/Local/OmniSheetVault"));

        assertThat(folders.webDirectory()).isEqualTo(Path.of("C:/Games/OmniSheetVault/app/web"));
        assertThat(folders.contentDirectory()).isEqualTo(Path.of("C:/Games/OmniSheetVault/app/content"));
    }

    @Test
    void thePlayersDataGoesToLocalAppDataOrTheHomeFolder() {
        assertThat(AppFolders.userDataDirectory("C:/Users/nicole/AppData/Local", "C:/Users/nicole"))
                .isEqualTo(Path.of("C:/Users/nicole/AppData/Local/OmniSheetVault"));
        assertThat(AppFolders.userDataDirectory(null, "/home/nicole")).isEqualTo(Path.of("/home/nicole/OmniSheetVault"));
    }
}

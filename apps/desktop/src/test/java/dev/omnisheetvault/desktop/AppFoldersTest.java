package dev.omnisheetvault.desktop;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AppFoldersTest {

    @Test
    void theBundledFilesSitNextToTheLauncherJar(@TempDir Path install) {
        Path app = install.resolve("OmniSheetVault").resolve("app");
        Path appDirectory = AppFolders.directoryOf(app.resolve("omni-sheet-vault-desktop.jar"));
        AppFolders folders = new AppFolders(appDirectory, install.resolve("data"));

        assertThat(folders.webDirectory()).isEqualTo(app.resolve("web"));
        assertThat(folders.contentDirectory()).isEqualTo(app.resolve("content"));
    }

    @Test
    void thePlayersDataGoesToLocalAppDataOrTheHomeFolder() {
        assertThat(AppFolders.userDataDirectory("C:/Users/nicole/AppData/Local", "C:/Users/nicole"))
                .isEqualTo(Path.of("C:/Users/nicole/AppData/Local/OmniSheetVault"));
        assertThat(AppFolders.userDataDirectory(null, "/home/nicole")).isEqualTo(Path.of("/home/nicole/OmniSheetVault"));
    }
}

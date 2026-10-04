package dev.omnisheetvault.desktop;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/** The small window the player sees: what the vault is doing, "Open the vault" and "Quit". Closing it quits too. */
final class LauncherWindow {

    private final JFrame frame = new JFrame("Omni Sheet Vault");
    private final JLabel status = new JLabel();
    private final JButton open = new JButton("Open the vault");
    private final JButton quit = new JButton("Quit");

    LauncherWindow(Runnable onOpen, Runnable onQuit) {
        open.setEnabled(false);
        open.addActionListener(event -> onOpen.run());
        quit.addActionListener(event -> onQuit.run());

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(open);
        buttons.add(quit);

        JPanel content = new JPanel(new BorderLayout(0, 12));
        content.setBorder(BorderFactory.createEmptyBorder(16, 18, 12, 18));
        content.add(status, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);

        frame.setContentPane(content);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent event) {
                onQuit.run();
            }
        });
        frame.setSize(460, 190);
        frame.setLocationRelativeTo(null);
    }

    void showStarting() {
        update("<html><b>Starting the vault…</b><br>The first start prepares the database and the books, "
                + "which can take a minute. Later starts take a few seconds.</html>", false);
        SwingUtilities.invokeLater(() -> frame.setVisible(true));
    }

    void showRunning(String address) {
        update("<html><b>The vault is running.</b><br>It opened in your browser at " + address
                + ".<br>Closing this window stops the vault.</html>", true);
    }

    void showStopping() {
        update("<html><b>Stopping the vault…</b><br>Saving everything and closing the database.</html>", false);
        SwingUtilities.invokeLater(() -> quit.setEnabled(false));
    }

    void showFailure(Throwable failure, Path logFile) {
        update("<html><b>The vault could not start.</b><br>" + failure.getClass().getSimpleName() + ": " + failure.getMessage()
                + "<br>Details are in " + logFile + "</html>", false);
    }

    private void update(String html, boolean canOpen) {
        SwingUtilities.invokeLater(() -> {
            status.setText(html);
            open.setEnabled(canOpen);
        });
    }
}

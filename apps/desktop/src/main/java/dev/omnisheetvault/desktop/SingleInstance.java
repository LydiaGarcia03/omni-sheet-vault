package dev.omnisheetvault.desktop;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;

/**
 * Only one copy of the vault may run per data folder: a second one would start a second database on the same files.
 * The first copy holds a lock and writes its port, so a second launch can just open the running one instead.
 */
final class SingleInstance implements AutoCloseable {

    private static final String LOCK_FILE = "app.lock";
    private static final String PORT_FILE = "app.port";

    private final Path dataDirectory;
    private final FileChannel channel;
    private final FileLock lock;

    private SingleInstance(Path dataDirectory, FileChannel channel, FileLock lock) {
        this.dataDirectory = dataDirectory;
        this.channel = channel;
        this.lock = lock;
    }

    /** The lock for this data folder, or empty when another copy of the vault already holds it. */
    static Optional<SingleInstance> acquire(Path dataDirectory) throws IOException {
        Files.createDirectories(dataDirectory);
        FileChannel channel = FileChannel.open(dataDirectory.resolve(LOCK_FILE), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        FileLock lock = null;
        try {
            lock = channel.tryLock();
        } catch (OverlappingFileLockException alreadyHeldInThisProcess) {
            // Same outcome as a lock held by another process.
        } finally {
            if (lock == null) {
                channel.close();
            }
        }
        return lock == null ? Optional.empty() : Optional.of(new SingleInstance(dataDirectory, channel, lock));
    }

    /** The port the running copy recorded, if it recorded one. */
    static Optional<Integer> runningPort(Path dataDirectory) {
        try {
            return Optional.of(Integer.parseInt(Files.readString(dataDirectory.resolve(PORT_FILE)).trim()));
        } catch (IOException | NumberFormatException unknown) {
            return Optional.empty();
        }
    }

    void recordPort(int port) throws IOException {
        Files.writeString(dataDirectory.resolve(PORT_FILE), Integer.toString(port));
    }

    @Override
    public void close() throws IOException {
        Files.deleteIfExists(dataDirectory.resolve(PORT_FILE));
        lock.release();
        channel.close();
    }
}

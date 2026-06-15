package home.thienph.managers;

import home.thienph.Main;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

@Slf4j
public class AppLockManager {
    private static FileLock lock;
    private static FileChannel channel;

    public static boolean isAppAlreadyRunning() {
        try {
            File lockFile = new File(Main.TEMP_DIR, "jcef.app.lock");
            if (!lockFile.getParentFile().exists()) {
                lockFile.getParentFile().mkdirs();
            }
            channel = new RandomAccessFile(lockFile, "rw").getChannel();
            lock = channel.tryLock();
            if (lock == null) {
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Không thể kiểm tra trạng thái FileLock", e);
            return false;
        }
    }

    public static void releaseLock() {
        try {
            if (lock != null) lock.release();
            if (channel != null) channel.close();
        } catch (Exception ignored) {
        }
    }
}
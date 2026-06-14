package home.thienph.managers;

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
            // Tạo một file ẩn để làm ổ khóa trong thư mục chạy jcef
            File lockFile = new File("jcef", ".app.lock");
            if (!lockFile.getParentFile().exists()) {
                lockFile.getParentFile().mkdirs();
            }

            // Mở file dưới dạng Đọc/Ghi
            channel = new RandomAccessFile(lockFile, "rw").getChannel();

            // Cố gắng chiếm quyền khóa file (tryLock)
            lock = channel.tryLock();

            if (lock == null) {
                // Nếu không khóa được (lock == null), nghĩa là app khác đang giữ khóa
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Không thể kiểm tra trạng thái FileLock", e);
            return false; // Trả về false để app chạy tiếp nếu lỗi phân quyền ổ đĩa
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
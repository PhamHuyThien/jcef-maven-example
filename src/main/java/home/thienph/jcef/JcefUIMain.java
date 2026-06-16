package home.thienph.jcef;

import home.thienph.jcef.dispatchers.CefMessageDispatcher;
import home.thienph.jcef.managers.AppLockManager;
import home.thienph.jcef.managers.JcefManager;
import home.thienph.jcef.servers.LocalServer;
import home.thienph.jcef.utils.ClassUtils;
import home.thienph.jcef.utils.ThrUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.Set;

@Slf4j
public class JcefUIMain {

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String APP_DIR = System.getProperty("user.dir");

    @Getter
    private static final Set<Class<?>> allClasses = ClassUtils.findClasses(JcefUIMain.class.getPackageName());
    @Getter
    private static final LocalServer server = new LocalServer();
    @Getter
    private static final Thread shutdownHook = ThrUtils.newNamedThread("shutdown", JcefUIMain::shutdownHook);


    public static void main(String[] args) {
        start();
    }

    @SneakyThrows
    public static void start() {
        Runtime.getRuntime().addShutdownHook(shutdownHook);

        if (AppLockManager.isAppAlreadyRunning()) {
            JOptionPane.showMessageDialog(null,
                    "Ứng dụng hiện đang chạy một phiên bản khác trên hệ thống rồi!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
            return;
        }
        server.start();
        CefMessageDispatcher.init();
        JcefManager.init();
    }

    private static void shutdownHook() {
        long currentTime = System.currentTimeMillis();
        log.info("Shutting down ...");
        server.stop();
        JcefManager.closeJcefFrames();
        AppLockManager.releaseLock();
        log.info("Shutting down completed - total {} ms", System.currentTimeMillis() - currentTime);
        Runtime.getRuntime().halt(0);
    }

}
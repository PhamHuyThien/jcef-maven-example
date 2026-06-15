package home.thienph;

import home.thienph.dispatchers.CefMessageDispatcher;
import home.thienph.jcefs.JcefWindow;
import home.thienph.managers.AppLockManager;
import home.thienph.managers.JcefManager;
import home.thienph.servers.LocalServer;
import home.thienph.utils.ClassUtils;
import home.thienph.utils.ThrUtils;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.Set;

@Slf4j
public class Main {

    public static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    public static final String APP_DIR = System.getProperty("user.dir");

    @Getter
    private static final Set<Class<?>> allClasses = ClassUtils.findClasses(Main.class.getPackageName());
    @Getter
    private static final LocalServer server = new LocalServer();
    @Getter
    private static final Thread shutdownHook = ThrUtils.newNamedThread("shutdown", Main::shutdownHook);


    @SneakyThrows
    public static void main(String[] args) {
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
        log.info("Shutting down localserver at port {} ...", server.getPort());
        server.stop();
        for (JcefWindow jcefWindow : JcefManager.getJcefWindows()) {
            log.info("Shutting down jcef window id {} ...", jcefWindow.getId());
            jcefWindow.stop();
        }
        AppLockManager.releaseLock();
        log.info("Shutting down completed - total {} ms", System.currentTimeMillis() - currentTime);
        Runtime.getRuntime().halt(0);
    }

}
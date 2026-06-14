package home.thienph;

import home.thienph.managers.AppLockManager;
import home.thienph.managers.AppManager;
import home.thienph.utils.ThrUtils;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class Main {
    private static final Thread SHUTDOWN_HOOK
            = ThrUtils.newNamedThread("shutdown", Main::shutdownHook);

    public static void main(String[] args) throws Exception {
        Runtime.getRuntime().addShutdownHook(SHUTDOWN_HOOK);

        if (AppLockManager.isAppAlreadyRunning()) {
            JOptionPane.showMessageDialog(null,
                    "Ứng dụng hiện đang chạy một phiên bản khác trên hệ thống rồi!",
                    "Cảnh báo",
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
            return;
        }

        AppManager.init();
    }

    private static void shutdownHook() {
        AppManager.destroy();
        AppLockManager.releaseLock();
        log.info("Shutting down completed");
        Runtime.getRuntime().halt(0);
    }
}
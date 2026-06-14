package home.thienph.managers;

import home.thienph.handlers.MessageRouterHandler;
import home.thienph.jcefs.JcefWindow;
import home.thienph.servers.LocalServer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class AppManager {
    @Getter
    private static final List<JcefWindow> jcefWindows = new ArrayList<>();
    public static LocalServer server = new LocalServer();

    @SneakyThrows
    public static void init() {
        server.start();
        initJcefWindows();
    }

    public static void initJcefWindows() {
        JcefWindow jcefWindow = new JcefWindow(server.getUrl());
        jcefWindow.setOpenDevTools(true);
        jcefWindow.setResizable(false);
        jcefWindow.setMain(true);
        jcefWindow.getMessageRouterHandlers().add(MessageRouterHandler.getInstance());
        jcefWindow.init();
        jcefWindow.show();
        jcefWindows.add(jcefWindow);

        JcefWindow jcefWindow2 = new JcefWindow(server.getUrl("/b.html"));
        jcefWindow2.setOpenDevTools(true);
        jcefWindow2.setResizable(false);
        jcefWindow2.getMessageRouterHandlers().add(MessageRouterHandler.getInstance());
        jcefWindow2.init();
        jcefWindow2.show();
        jcefWindows.add(jcefWindow2);
    }

    public static void destroy() {
        if (server != null) {
            log.info("Shutting down local server...");
            server.stop();
        }
        for (JcefWindow jcefWindow : jcefWindows) {
            log.info("Shutting down jcef id {} ...", jcefWindow.getId());
            jcefWindow.stop();
        }
    }
}

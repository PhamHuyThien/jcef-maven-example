package home.thienph.managers;

import home.thienph.Main;
import home.thienph.data.cefs.CefEvent;
import home.thienph.handlers.MessageRouterHandler;
import home.thienph.jcefs.JcefWindow;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cef.browser.CefBrowser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
public class JcefManager {
    @Getter
    public static List<JcefWindow> jcefWindows = new ArrayList<>();
    private static final Map<CefBrowser, JcefWindow> jcefWindowsMap = new ConcurrentHashMap<>();
    private static final List<Consumer<CefEvent>> jcefEventConsumers = new CopyOnWriteArrayList<>();

    public static void init() {
        JcefWindow jcefWindow = new JcefWindow(Main.getServer().getUrl());
        jcefWindow.setOpenDevTools(true);
        jcefWindow.setResizable(false);
        jcefWindow.setMain(true);
        jcefWindow.getMessageRouterHandlers().add(MessageRouterHandler.getInstance());
        jcefWindow.init();
        jcefWindow.show();
        jcefWindows.add(jcefWindow);

        JcefWindow jcefWindow2 = new JcefWindow(Main.getServer().getUrlByKey("b"));
        jcefWindow2.setOpenDevTools(true);
        jcefWindow2.setResizable(false);
        jcefWindow2.getMessageRouterHandlers().add(MessageRouterHandler.getInstance());
        jcefWindow2.init();
        jcefWindow2.show();
        jcefWindows.add(jcefWindow2);
    }

    public static JcefWindow getJcefWindowById(String id) {
        return jcefWindows.stream().filter(jcefWindow -> jcefWindow.getId().equals(id)).findFirst().orElse(null);
    }

    public static List<JcefWindow> getJcefWindowByTitle(String title) {
        return jcefWindows.stream().filter(jcefWindow -> jcefWindow.getTitle().equals(title)).toList();
    }

    public static void registerBrowser(CefBrowser browser, JcefWindow window) {
        if (browser != null && window != null) {
            jcefWindowsMap.put(browser, window);
        }
    }

    public static void unregisterBrowser(CefBrowser browser) {
        if (browser != null) {
            jcefWindowsMap.remove(browser);
        }
    }

    public static JcefWindow getWindow(CefBrowser browser) {
        return browser != null ? jcefWindowsMap.get(browser) : null;
    }

    public static void registerEvent(Consumer<CefEvent> subscriber) {
        if (!jcefEventConsumers.contains(subscriber)) {
            jcefEventConsumers.add(subscriber);
        }
    }

    public static void unregisterEvent(Consumer<CefEvent> subscriber) {
        jcefEventConsumers.remove(subscriber);
    }

    public static void postEvent(CefEvent event) {
        for (Consumer<CefEvent> subscriber : jcefEventConsumers) {
            try {
                subscriber.accept(event);
            } catch (Exception e) {
                log.error("post event error: {}", e.getMessage(), e);
            }
        }
    }
}

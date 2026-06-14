package home.thienph.managers;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import home.thienph.jcefs.JcefWindow;
import org.cef.browser.CefBrowser;

public class CefWindowManager {
    private static final Map<CefBrowser, JcefWindow> windowMap = new ConcurrentHashMap<>();

    public static void register(CefBrowser browser, JcefWindow window) {
        if (browser != null && window != null) {
            windowMap.put(browser, window);
        }
    }

    public static void unregister(CefBrowser browser) {
        if (browser != null) {
            windowMap.remove(browser);
        }
    }

    public static JcefWindow getWindow(CefBrowser browser) {
        return browser != null ? windowMap.get(browser) : null;
    }
}
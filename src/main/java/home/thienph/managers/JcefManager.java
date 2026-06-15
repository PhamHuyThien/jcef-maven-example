package home.thienph.managers;

import home.thienph.Main;
import home.thienph.data.cefs.CefEvent;
import home.thienph.handlers.MessageRouterHandler;
import home.thienph.jcefs.JcefFrame;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.browser.CefBrowser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@Slf4j
public class JcefManager {
    private static final String JCEF_BUNDLE_PATH = "jcef";

    @Getter
    private static final CefAppBuilder builder;
    @Getter
    private static final CefApp app;

    @Getter
    public static List<JcefFrame> jcefFrames = new ArrayList<>();
    private static final Map<CefBrowser, JcefFrame> JcefFramesMap = new ConcurrentHashMap<>();
    private static final List<BiConsumer<JcefFrame, CefEvent>> jcefEventConsumers = new CopyOnWriteArrayList<>();

    static {
        builder = new CefAppBuilder();
        builder.setInstallDir(new File(JCEF_BUNDLE_PATH));
        builder.getCefSettings().windowless_rendering_enabled = false;
        builder.addJcefArgs("--disable-gpu", "--disable-webgl", "--no-sandbox");
        try {
            if (CefApp.getState() == CefApp.CefAppState.INITIALIZED)
                app = CefApp.getInstance();
            else app = builder.build();
        } catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void init() {
        JcefManager.createJcefFrame("Main", Main.getServer().getUrl(), true);
        JcefManager.createJcefFrame("Secondary", Main.getServer().getUrlByKey("index2"), 400, 300, false, true);
    }

    public static JcefFrame createJcefFrame(String title, String source) {
        return createJcefFrame(title, source, false);
    }

    public static JcefFrame createJcefFrame(String title, String source, boolean devtools) {
        return createJcefFrame(title, source, JcefFrame.DEFAULT_WIDTH, JcefFrame.DEFAULT_HEIGHT, true, devtools);
    }

    public static JcefFrame createJcefFrame(String title, String source, int w, int h, boolean resize, boolean devtools) {
        JcefFrame jcefFrame = new JcefFrame(source);
        jcefFrame.setTitle(title);
        jcefFrame.setSize(w, h);
        jcefFrame.setResizable(resize);
        jcefFrame.setOpenDevTools(devtools);
        jcefFrame.setLocationRelativeTo(null);
        jcefFrame.setMain(jcefFrames.stream().noneMatch(JcefFrame::isMain));
        jcefFrame.getMessageRouterHandlers().add(MessageRouterHandler.getInstance());
        jcefFrame.init();
        jcefFrame.setVisible(true);
        jcefFrames.add(jcefFrame);
        return jcefFrame;
    }

    public static void closeJcefFrames() {
        jcefFrames.forEach(JcefFrame::stop);
        jcefFrames.clear();
        app.dispose();
    }

    public static void closeJcefFrames(JcefFrame jcefFrame) {
        jcefFrame.stop();
        jcefFrames.remove(jcefFrame);
        if (jcefFrames.isEmpty())
            app.dispose();
    }

    public static JcefFrame getJcefFrameById(String id) {
        return jcefFrames.stream().filter(JcefFrame -> JcefFrame.getId().equals(id)).findFirst().orElse(null);
    }

    public static List<JcefFrame> getJcefFrameByTitle(String title) {
        return jcefFrames.stream().filter(JcefFrame -> JcefFrame.getTitle().equals(title)).toList();
    }

    public static void registerBrowser(CefBrowser browser, JcefFrame window) {
        if (browser != null && window != null) {
            JcefFramesMap.put(browser, window);
        }
    }

    public static void unregisterBrowser(CefBrowser browser) {
        if (browser != null) {
            JcefFramesMap.remove(browser);
        }
    }

    public static JcefFrame getJcefFrame(CefBrowser browser) {
        return browser != null ? JcefFramesMap.get(browser) : null;
    }

    public static void registerEvent(BiConsumer<JcefFrame, CefEvent> subscriber) {
        if (!jcefEventConsumers.contains(subscriber)) {
            jcefEventConsumers.add(subscriber);
        }
    }

    public static void unregisterEvent(BiConsumer<JcefFrame, CefEvent> subscriber) {
        jcefEventConsumers.remove(subscriber);
    }

    public static void postEvent(JcefFrame jcefFrame, CefEvent event) {
        for (BiConsumer<JcefFrame, CefEvent> subscriber : jcefEventConsumers) {
            try {
                subscriber.accept(jcefFrame, event);
            } catch (Exception e) {
                log.error("post event error: {}", e.getMessage(), e);
            }
        }
    }
}

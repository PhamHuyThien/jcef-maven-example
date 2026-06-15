package home.thienph.jcefs;

import home.thienph.data.cefs.CefEvent;
import home.thienph.managers.JcefManager;
import home.thienph.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.friwi.jcefmaven.CefAppBuilder;
import me.friwi.jcefmaven.CefInitializationException;
import me.friwi.jcefmaven.UnsupportedPlatformException;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@Getter
public class JcefWindow {
    private static final String QUERY_FUNC_NAME = "cefQuery";
    private static final String QUERY_CANCEL_FUNC_NAME = "cefQueryCancel";
    private static final String JCEF_BUNDLE_PATH = "jcef";

    @Getter
    private static final CefAppBuilder builder;
    @Getter
    private static final CefApp app;

    static {
        builder = new CefAppBuilder();
        builder.setInstallDir(new File(JCEF_BUNDLE_PATH));
        builder.getCefSettings().windowless_rendering_enabled = false;
        builder.addJcefArgs(
                "--disable-gpu",
                "--disable-webgl",
                "--no-sandbox"
        );

        try {
            if (CefApp.getState() == CefApp.CefAppState.INITIALIZED) {
                app = CefApp.getInstance();
            } else {
                app = builder.build();
            }
        } catch (IOException | UnsupportedPlatformException | InterruptedException | CefInitializationException e) {
            throw new RuntimeException(e);
        }
    }

    String id;
    String url;
    @Setter
    boolean openDevTools = false;

    CefClient client;
    CefBrowser browser;
    Component component;

    @Setter
    String title;
    @Setter
    int width = 800;
    @Setter
    int height = 600;
    @Setter
    boolean resizable = true;
    @Setter
    boolean isMain;
    JFrame frame;

    CefMessageRouter messageRouter;
    @Setter
    CefMessageRouter.CefMessageRouterConfig messageRouterConfig;
    List<CefMessageRouterHandlerAdapter> messageRouterHandlers;
    List<CefLoadHandlerAdapter> loadHandlerAdapters;

    private Consumer<CefEvent> eventBusListener;

    public JcefWindow(String url) {
        this.url = url;
        id = UUID.randomUUID().toString();
        title = id;
        messageRouterConfig = new CefMessageRouter.CefMessageRouterConfig(QUERY_FUNC_NAME, QUERY_CANCEL_FUNC_NAME);
        messageRouterHandlers = new ArrayList<>();
        loadHandlerAdapters = new ArrayList<>();
    }

    @SneakyThrows
    public synchronized void init() {
        client = app.createClient();

        messageRouter = CefMessageRouter.create(messageRouterConfig);
        if (messageRouterHandlers != null && !messageRouterHandlers.isEmpty()) {
            messageRouterHandlers.forEach(msg -> messageRouter.addHandler(msg, true));
        }
        client.addMessageRouter(messageRouter);

        if (openDevTools) {
            loadHandlerAdapters.add(new OpenDevToolLoadHandler());
        }
        if (loadHandlerAdapters != null && !loadHandlerAdapters.isEmpty()) {
            loadHandlerAdapters.forEach(client::addLoadHandler);
        }

        browser = client.createBrowser(url, false, false);
        component = browser.getUIComponent();

        this.eventBusListener = (event) -> {
            if (!event.getId().equals(this.id)) {
                this.broadcastMessage(event.getTopic(), event.getData());
            }
        };
        JcefManager.registerEvent(this.eventBusListener);
        JcefManager.registerBrowser(browser, this);

        frame = new JFrame(title);
        frame.setSize(width, height);
        if (isMain) frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        else frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(resizable);
        frame.add(component, BorderLayout.CENTER);
    }

    public void show() {
        show(true);
    }

    public void show(boolean show) {
        if (frame != null) {
            frame.setVisible(show);
        }
    }

    public void stop() {
        if (browser == null) return;
        JcefManager.unregisterBrowser(browser);
        if (this.eventBusListener != null) {
            JcefManager.unregisterEvent(this.eventBusListener);
        }
        browser.stopLoad();
        browser.close(true);
        client.dispose();
        if (isMain) {
            app.dispose();
        }
    }

    public void broadcastEvent(String topic, Object data) {
        CefEvent event = new CefEvent(this.id, topic, data);
        JcefManager.postEvent(event);
    }

    public void sendCefMessage(String type, Object data) {
        String res = StringUtils.normalizeToString(data);
        String jsScript = String.format("window.onCefMessage('%s', %s)", type, res);
        browser.executeJavaScript(jsScript, "app://jcef.js", 0);
    }

    public void broadcastMessage(String topic, Object data) {
        String res = StringUtils.normalizeToString(data);
        String jsScript = String.format("window.onBroadcastMessage('%s', %s)", topic, res);
        browser.executeJavaScript(jsScript, "app://jcef.js", 0);
    }

    private static class OpenDevToolLoadHandler extends CefLoadHandlerAdapter {
        @Override
        public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
            super.onLoadEnd(browser, frame, httpStatusCode);
            if (frame.isMain()) {
                SwingUtilities.invokeLater(browser::openDevTools);
            }
        }
    }
}
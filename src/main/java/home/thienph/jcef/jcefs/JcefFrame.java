package home.thienph.jcef.jcefs;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import home.thienph.jcef.data.cefs.CefEvent;
import home.thienph.jcef.managers.JcefManager;
import home.thienph.jcef.utils.JsonUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.handler.CefLoadHandlerAdapter;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

@Slf4j
@Getter
@JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.NONE,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE
)
public class JcefFrame extends JFrame {
    public static final int DEFAULT_WIDTH = 800;
    public static final int DEFAULT_HEIGHT = 600;
    private static final String QUERY_FUNC_NAME = "cefQuery";
    private static final String QUERY_CANCEL_FUNC_NAME = "cefQueryCancel";

    @JsonProperty("id")
    private final String id;
    @JsonProperty("url")
    private final String url;
    @Setter
    private boolean openDevTools;
    @JsonProperty("isMain")
    private boolean isMain;
    @JsonProperty("isInitialized")
    private boolean isInitialized;

    private CefClient client;
    private CefBrowser browser;
    private Component component;
    private CefMessageRouter messageRouter;
    @Setter
    private CefMessageRouter.CefMessageRouterConfig messageRouterConfig;
    private final List<CefMessageRouterHandlerAdapter> messageRouterHandlers;
    private final List<CefLoadHandlerAdapter> loadHandlerAdapters;
    private BiConsumer<JcefFrame, CefEvent> eventBusListener;

    public JcefFrame(String url) {
        super();
        this.url = url;
        setTitle(id = UUID.randomUUID().toString());
        messageRouterConfig = new CefMessageRouter.CefMessageRouterConfig(QUERY_FUNC_NAME, QUERY_CANCEL_FUNC_NAME);
        messageRouterHandlers = new ArrayList<>();
        loadHandlerAdapters = new ArrayList<>();
    }

    @SneakyThrows
    public void init() {
        if (isInitialized) return;
        isInitialized = true;

        client = JcefManager.getApp().createClient();

        messageRouter = CefMessageRouter.create(messageRouterConfig);
        if (messageRouterHandlers != null && !messageRouterHandlers.isEmpty()) {
            messageRouterHandlers.forEach(msg -> messageRouter.addHandler(msg, true));
        }
        client.addMessageRouter(messageRouter);

        if (openDevTools) loadHandlerAdapters.add(new OpenDevToolLoadHandler());
        if (loadHandlerAdapters != null && !loadHandlerAdapters.isEmpty()) {
            loadHandlerAdapters.forEach(client::addLoadHandler);
        }

        browser = client.createBrowser(url, false, false);
        component = browser.getUIComponent();

        this.eventBusListener = (JcefFrame, event) -> {
            if (!event.getId().equals(this.id)) {
                this.broadcastMessage(JcefFrame, event.getTopic(), event.getData());
            }
        };
        JcefManager.registerEvent(this.eventBusListener);
        JcefManager.registerBrowser(browser, this);

        if (isMain) setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        else setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        add(component, BorderLayout.CENTER);
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
    }

    public void setMain(boolean main) {
        if (isInitialized) return;
        isMain = main;
    }

    public void broadcastEvent(String topic, Object data) {
        CefEvent event = new CefEvent(this.id, topic, data);
        JcefManager.postEvent(this, event);
    }

    public void sendCefMessage(String type, Object data) {
        String res = JsonUtils.toJson(data);
        String jsScript = String.format("window.onCefMessage('%s', %s)", type, res);
        browser.executeJavaScript(jsScript, "app://jcef.js", 0);
    }

    public void broadcastMessage(JcefFrame jcefFrame, String topic, Object data) {
        String from = JsonUtils.toJson(jcefFrame);
        String res = JsonUtils.toJson(data);
        String jsScript = String.format("window.onCefBroadcastMessage(%s, '%s', %s)", from, topic, res);
        browser.executeJavaScript(jsScript, "app://jcef.js", 0);
    }

    @JsonProperty
    public int getWidth() {
        return super.getWidth();
    }

    @JsonProperty
    public int getHeight() {
        return super.getHeight();
    }

    @JsonProperty
    public String getTitle() {
        return super.getTitle();
    }

    @JsonProperty
    public int getX() {
        return super.getX();
    }

    @JsonProperty
    public int getY() {
        return super.getY();
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
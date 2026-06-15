package home.thienph.handlers;

import home.thienph.dispatchers.CefMessageDispatcher;
import home.thienph.exceptions.ResponseException;
import home.thienph.jcefs.JcefWindow;
import home.thienph.managers.JcefManager;
import home.thienph.utils.JsonUtils;
import home.thienph.utils.StringUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

@Slf4j
public class MessageRouterHandler extends CefMessageRouterHandlerAdapter {

    @Getter
    private static final MessageRouterHandler instance = new MessageRouterHandler();

    @Override
    public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
        int status = 0;
        String response;
        try {
            JcefWindow window = JcefManager.getWindow(browser);
            Object[] data = JsonUtils.fromJson(request, Object[].class);
            Object result = CefMessageDispatcher.dispatch(window, data);
            response = StringUtils.normalizeToString(result);
        } catch (ResponseException e) {
            status = e.getStatus();
            response = e.getMessage();
            callback.failure(e.getStatus(), e.getMessage());
        } catch (Exception e) {
            status = 500;
            response = e.getMessage();
        }
        if (status == 0) callback.success(response);
        else callback.failure(status, response);
        log.info("onQuery: req => {} | stt => {} | res => {}", request, status, response);
        return true;
    }
}

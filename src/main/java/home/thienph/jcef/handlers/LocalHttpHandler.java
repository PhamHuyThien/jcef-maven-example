package home.thienph.jcef.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.io.OutputStream;

@Slf4j
public class LocalHttpHandler implements HttpHandler {
    public static final String DEFAULT_UI_PATH = "/ui";
    public static final String DEFAULT_FILE_INDEX = "/index.html";
    public static final String DEFAULT_FILE_NOT_FOUND = "/404.html";
    public static final String DEFAULT_SERVER_INTERNAL_ERROR = "/500.html";

    @Getter
    private static final LocalHttpHandler instance = new LocalHttpHandler();

    @Override
    public void handle(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        if (path.equals("/") || path.isEmpty()) path = DEFAULT_FILE_INDEX;
        try {
            InputStream isResponse = getResource(path);
            if (isResponse == null) {
                try (InputStream isFileNotFound = getResource(DEFAULT_FILE_NOT_FOUND)) {
                    sendResponse(path, exchange, 404, isFileNotFound);
                    return;
                }
            }
            sendResponse(path, exchange, 200, isResponse);
        } catch (Throwable t) {
            log.error("server error:", t);
            try {
                try (InputStream isServerInternalError = getResource(DEFAULT_SERVER_INTERNAL_ERROR)) {
                    sendResponse(path, exchange, 500, isServerInternalError);
                }
            } catch (Exception e) {
                log.error("send server internal error failed", e);
            }
        }
    }

    @SneakyThrows
    private void sendResponse(String path, HttpExchange exchange, int statusCode, InputStream response) {
        byte[] bytesResponse;
        try (response) {
            bytesResponse = response.readAllBytes();
        }
        String mimeType = switch (path.substring(path.lastIndexOf('.') + 1)) {
            case "js" -> "application/javascript";
            case "css" -> "text/css";
            case "png" -> "image/png";
            case "jpg", "jpeg" -> "image/jpeg";
            case "svg" -> "image/svg+xml";
            default -> "text/html; charset=UTF-8";
        };
        exchange.getResponseHeaders().set("Content-Type", mimeType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytesResponse.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytesResponse);
        }
    }

    private String resolvePath(String filePath) {
        return DEFAULT_UI_PATH + filePath;
    }

    private InputStream getResource(String filePath) {
        return getClass().getResourceAsStream(resolvePath(filePath));
    }
}

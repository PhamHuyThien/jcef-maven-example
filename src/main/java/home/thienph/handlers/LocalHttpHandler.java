package home.thienph.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import home.thienph.servers.LocalServer;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LocalHttpHandler implements HttpHandler {
    @Getter
    private static final LocalHttpHandler instance = new LocalHttpHandler();

    @Override
    public void handle(HttpExchange exchange) {
        try {
            String path = exchange.getRequestURI().getPath();
            if (path.equals(LocalServer.DEFAULT_PATH) || path.isEmpty()) {
                path = LocalServer.DEFAULT_PATH + LocalServer.DEFAULT_FILE_INDEX;
            }
            InputStream is = getClass().getResourceAsStream(LocalServer.DEFAULT_UI_PATH + path);
            if (is == null) {
                try (InputStream _404 = getClass().getResourceAsStream(LocalServer.DEFAULT_UI_PATH + LocalServer.DEFAULT_FILE_NOT_FOUND)) {
                    sendTextResponse(exchange, 404, _404);
                    return;
                }
            }
            byte[] bytes;
            try (is) {
                bytes = is.readAllBytes();
            }
            String mimeType = switch (path.substring(path.lastIndexOf('.') + 1)) {
                case "js" -> "application/javascript";
                case "css" -> "text/css";
                case "png" -> "image/png";
                case "jpg", "jpeg" -> "image/jpeg";
                case "svg" -> "image/svg+xml";
                default -> "text/html";
            };
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (Throwable t) {
            log.error("server error: {}", t.getMessage(), t);
            try {
                try (InputStream _500 = getClass().getResourceAsStream(LocalServer.DEFAULT_UI_PATH + LocalServer.DEFAULT_SERVER_INTERNAL_ERROR)) {
                    sendTextResponse(exchange, 500, _500);
                }
            } catch (IOException ignored) {
            }
        }
    }

    @SneakyThrows
    private void sendTextResponse(HttpExchange exchange, int statusCode, InputStream data) {
        if (data != null) {
            sendTextResponse(exchange, statusCode, new String(data.readAllBytes(), StandardCharsets.UTF_8));
        }
    }

    private void sendTextResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }
}

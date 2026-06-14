package home.thienph;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

@Slf4j
public class LocalAssetServer {
    private HttpServer server;
    private int port;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress("localhost", 0), 0);
        port = server.getAddress().getPort();

        server.createContext("/", new HttpHandler() {
            @Override
            public void handle(HttpExchange exchange) {
                try {
                    String path = exchange.getRequestURI().getPath();
                    if (path.equals("/") || path.isEmpty()) {
                        path = "/index.html";
                    }
                    InputStream is = getClass().getResourceAsStream("/ui" + path);
                    if (is == null) {
                        sendTextResponse(exchange, 404, "Not Found");
                        return;
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
                        sendTextResponse(exchange, 500, "500 Internal Server Error: " + t.getMessage());
                    } catch (IOException ignored) {
                    }
                }
            }
        });
        server.setExecutor(java.util.concurrent.Executors.newFixedThreadPool(100));
        server.start();
        log.info("Local Web Server dang chay tai port: {}", port);
    }

    private void sendTextResponse(HttpExchange exchange, int statusCode, String responseText) throws IOException {
        byte[] responseBytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(responseBytes);
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    public String getUrl() {
        return "http://localhost:" + port + "/index.html";
    }
}
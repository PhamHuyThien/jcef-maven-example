package home.thienph.servers;

import com.sun.net.httpserver.HttpServer;
import home.thienph.handlers.LocalHttpHandler;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Getter
public class LocalServer {
    public static final String DEFAULT_PATH = "/";
    public static final String DEFAULT_UI_PATH = "/ui";
    public static final String DEFAULT_FILE_INDEX = "index.html";
    public static final String DEFAULT_FILE_NOT_FOUND = "404.html";
    public static final String DEFAULT_SERVER_INTERNAL_ERROR = "500.html";
    public static final String DEFAULT_HOST_NAME = "localhost";

    private HttpServer server;
    @Setter
    private String host = DEFAULT_HOST_NAME;
    @Setter
    private int port = 0;
    private ExecutorService executor;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        port = server.getAddress().getPort();
        server.createContext(DEFAULT_PATH, LocalHttpHandler.getInstance());
        executor = Executors.newFixedThreadPool(100);
        server.setExecutor(executor);
        server.start();
        log.info("Local Web Server dang chay tai port: {}", port);
    }



    public void stop() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public String getUrl() {
        return "http://" + host + ":" + port + "/" + DEFAULT_FILE_INDEX;
    }

    public String getUrl(String path) {
        return "http://" + host + ":" + port + path;
    }
}
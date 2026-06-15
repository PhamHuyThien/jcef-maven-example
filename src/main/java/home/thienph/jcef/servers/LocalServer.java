package home.thienph.jcef.servers;

import com.sun.net.httpserver.HttpServer;
import home.thienph.jcef.handlers.LocalHttpHandler;
import home.thienph.jcef.utils.StringUtils;
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
    private HttpServer server;
    private final String protocol = "http";
    private final String host = "localhost";
    @Setter
    private int port = 0;
    private ExecutorService executor;

    public void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(host, port), 0);
        port = server.getAddress().getPort();
        server.createContext("/", LocalHttpHandler.getInstance());
        executor = Executors.newFixedThreadPool(100);
        server.setExecutor(executor);
        server.start();
        log.info("LocalServer running at port: {}", port);
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public String getLocationOrigin() {
        return protocol + "://" + host + ":" + port;
    }

    public String getUrlByFilePath(String filePath) {
        return getLocationOrigin() + "/" + filePath;
    }

    public String getUrlByKey(String key) {
        String filePath = StringUtils.EMPTY;
        if (key != null && !key.isBlank()) {
            filePath = key.replace(".", "/") + ".html";
        }
        return getUrlByFilePath(filePath);
    }

    public String getUrl() {
        return getUrlByKey(StringUtils.EMPTY);
    }
}
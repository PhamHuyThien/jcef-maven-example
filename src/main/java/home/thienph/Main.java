package home.thienph;

import lombok.extern.slf4j.Slf4j;
import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;

import javax.swing.*;
import java.awt.*;
import java.io.File;

@Slf4j
public class Main {

    public static void main(String[] args) throws Exception {

        // 1. KHỞI CHẠY LOCAL WEB SERVER TRƯỚC
        LocalAssetServer assetServer = new LocalAssetServer();
        assetServer.start();

        CefAppBuilder builder = new CefAppBuilder();
        builder.setInstallDir(new File("jcef"));

        builder.getCefSettings().windowless_rendering_enabled = false;

        builder.addJcefArgs(
                "--disable-gpu",
                "--disable-webgl",
                "--no-sandbox"
        );

        CefApp cefApp = builder.build();

        CefClient client = cefApp.createClient();

        // ====================================================================
        // CHIỀU 1: FRONTEND (JS) GỌI BACKEND (JAVA)
        // Cấu hình router nhận lệnh từ JavaScript thông qua từ khóa "cefQuery"
        // ====================================================================
        CefMessageRouter msgRouter = CefMessageRouter.create(new CefMessageRouter.CefMessageRouterConfig("cefQuery", "cefQueryCancel"));

        msgRouter.addHandler(new CefMessageRouterHandlerAdapter() {
            @Override
            public boolean onQuery(CefBrowser browser, CefFrame frame, long queryId, String request, boolean persistent, CefQueryCallback callback) {
                System.out.println("Java đã nhận yêu cầu từ JS: " + request);

                // Bạn có thể xử lý chuỗi request (ví dụ: parse JSON nếu gửi dữ liệu phức tạp)
                if (request.startsWith("sayHello:")) {
                    String name = request.substring(9);

                    // Trả kết quả thành công về cho JavaScript
                    callback.success("Xin chào " + name + "! Đây là phản hồi từ Java Backend.");
                    return true;
                } else if (request.equals("triggerJavaAction")) {
                    // Ví dụ: Nhấn nút trên Web để mở một thông báo Swing thuần của Java
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(null, "Lời gọi này kích hoạt một UI Java Swing thực sự!");
                    });
                    callback.success("Đã mở dialog Java thành công!");
                    return true;
                } else if (request.equals("open_devtools_panel")) {
//                    browser.openDevTools();
                }

                callback.failure(404, "Không tìm thấy lệnh phù hợp");
                return false;
            }
        }, true);

        client.addMessageRouter(msgRouter);


        client.addLoadHandler(new org.cef.handler.CefLoadHandlerAdapter() {
            @Override
            public void onLoadEnd(CefBrowser browser, org.cef.browser.CefFrame frame, int httpStatusCode) {
                if (frame.isMain()) {
                    // Chạy trên luồng EDT của Swing để đảm bảo an toàn giao diện
                    SwingUtilities.invokeLater(browser::openDevTools);
                }
            }
        });

        CefBrowser browser = client.createBrowser(
                assetServer.getUrl(), // Trả về link dạng http://localhost:xxxx/index.html,
                false,
                false
        );

        Component ui = browser.getUIComponent();



        JFrame frame = new JFrame("JCEF TEST");
        frame.setSize(1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.add(ui, BorderLayout.CENTER);
        frame.setVisible(true);

// ====================================================================
        // CHIỀU 2: BACKEND (JAVA) CHỦ ĐỘNG GỌI FRONTEND (JS)
        // Tạo một nút bấm Swing, khi bấm vào sẽ truyền dữ liệu xuống giao diện Web
        // ====================================================================
        JButton btnCallJS = new JButton("Gửi dữ liệu từ Java xuống Web");
        btnCallJS.setFont(new Font("Arial", Font.BOLD, 14));
        btnCallJS.addActionListener(e -> {
            String dataToSend = "Dữ liệu mật từ hệ thống Java: " + System.currentTimeMillis();
            // Sử dụng executeJavaScript để chạy một hàm JS bất kỳ đang có trên giao diện
            browser.executeJavaScript("receiveFromJava('" + dataToSend + "')", browser.getURL(), 0);
        });

        // Thiết lập bố cục UI Swing
        frame.add(btnCallJS, BorderLayout.NORTH); // Nút Java ở phía trên
        frame.add(ui, BorderLayout.CENTER);       // Giao diện Web ở giữa
        frame.setVisible(true);
    }
}
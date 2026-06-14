package home.thienph.jcefs;

import home.thienph.data.cefs.CefEvent;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Slf4j
public class CefEventBus {

    // Sử dụng Pattern Singleton để mọi cửa sổ đều truy cập vào cùng 1 Bus
    @Getter
    private static final CefEventBus instance = new CefEventBus();

    // Danh sách các hàm callback đang lắng nghe sự kiện
    private final List<Consumer<CefEvent>> subscribers = new CopyOnWriteArrayList<>();

    private CefEventBus() {
    }

    /**
     * Đăng ký lắng nghe sự kiện từ EventBus
     */
    public void register(Consumer<CefEvent> subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    /**
     * Hủy đăng ký (gọi khi cửa sổ bị đóng để tránh rò rỉ bộ nhớ)
     */
    public void unregister(Consumer<CefEvent> subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Phát tán sự kiện từ một cửa sổ tới TẤT CẢ các cửa sổ còn lại
     */
    public void post(CefEvent event) {
        // Gửi sự kiện tới từng subscriber
        for (Consumer<CefEvent> subscriber : subscribers) {
            try {
                subscriber.accept(event);
            } catch (Exception e) {
                log.error("post event error: {}", e.getMessage(), e);
            }
        }
    }
}
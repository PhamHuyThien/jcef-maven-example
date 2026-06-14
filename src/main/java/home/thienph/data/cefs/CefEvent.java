package home.thienph.data.cefs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
@ToString
public class CefEvent {
    private final String id; // ID của cửa sổ gửi (để tránh tự gửi tự nhận)
    private final String topic;    // Tên sự kiện (vídụ: "USER_LOGGED_IN", "REFRESH_DATA")
    private final Object data;     // Dữ liệu đính kèm (Bất kỳ Object nào hoặc chuỗi JSON)
}
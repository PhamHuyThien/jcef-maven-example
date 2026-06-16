package home.thienph.jcef.dispatchers;

import home.thienph.jcef.JcefUIMain;
import home.thienph.jcef.anotations.CefController;
import home.thienph.jcef.anotations.OnCefMessage;
import home.thienph.jcef.exceptions.ResponseException;
import home.thienph.jcef.jcefs.JcefFrame;
import home.thienph.jcef.utils.ClassUtils;
import home.thienph.jcef.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public class CefMessageDispatcher {
    private static final Map<String, HandlerMethod> handlers = new HashMap<>();

    public static void init() {
        try {
            for (Class<?> clazz : JcefUIMain.getAllClasses()) {
                if (!clazz.isAnnotationPresent(CefController.class)) continue;
                Object instance = clazz.getDeclaredConstructor().newInstance();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(OnCefMessage.class)) continue;
                    String key = method.getAnnotation(OnCefMessage.class).value();
                    method.setAccessible(true);
                    handlers.put(key, new HandlerMethod(instance, method));
                }
            }
            log.info("Scan total {} CefController / {} total class!!!", handlers.size(), JcefUIMain.getAllClasses().size());
        } catch (Exception e) {
            throw new RuntimeException("Scan CefController failed", e);
        }
    }

    public static Object dispatch(JcefFrame jcefFrame, Object[] data) throws Exception {
        if (data == null || data.length == 0)
            throw new ResponseException(500, "data request wrong format");

        HandlerMethod handler = handlers.get(String.valueOf(data[0]));
        if (handler == null) {
            throw new ResponseException(400, "type not found");
        }

        Class<?>[] paramTypes = handler.method.getParameterTypes();
        Object[] finalArgs = new Object[paramTypes.length];

        // Kiểm tra xem tham số đầu tiên của hàm Java có phải là JcefFrame hay không
        boolean hasFrameParam = paramTypes.length > 0 && paramTypes[0] == JcefFrame.class;

        if (hasFrameParam) {
            if (jcefFrame == null) throw new ResponseException(500, "jcefFrame not found but required");
            finalArgs[0] = jcefFrame;
        }

        // Vòng lặp duyệt qua tất cả tham số của hàm Java để nạp dữ liệu
        // Nếu có JcefFrame: Điền data từ index 1 của Java. Khớp với index 1 của Frontend.
        // Nếu KHÔNG có JcefFrame: Điền data từ index 0 của Java. Khớp với index 1 của Frontend.
        int javaStartIdx = hasFrameParam ? 1 : 0;

        for (int i = javaStartIdx; i < paramTypes.length; i++) {
            // Tính toán vị trí tương ứng của dữ liệu lấy từ Frontend (data)
            // Vì data[0] là tên hàm, nên tham số truyền lên thực tế bắt đầu từ data[1], data[2]...
            int frontendIdx = i + (hasFrameParam ? 0 : 1);

            // NÂNG CẤP: Nếu vị trí này phía Frontend không cung cấp -> Điền giá trị thiếu (null/default)
            if (frontendIdx >= data.length) {
                finalArgs[i] = ClassUtils.getDefaultValueForPrimitive(paramTypes[i]);
                continue;
            }

            // Nếu có dữ liệu, tiến hành parse qua Jackson
            if (data[frontendIdx] == null) {
                finalArgs[i] = ClassUtils.getDefaultValueForPrimitive(paramTypes[i]);
            } else {
                finalArgs[i] = JsonUtils.fromJson(data[frontendIdx], paramTypes[i]);
            }
        }

        // Tự động loại bỏ dữ liệu thừa: Mọi phần tử frontendIdx >= data.length đều đã được xử lý an toàn
        return handler.method.invoke(handler.instance, finalArgs);
    }

    static class HandlerMethod {
        Object instance;
        Method method;

        HandlerMethod(Object instance, Method method) {
            this.instance = instance;
            this.method = method;
        }
    }
}
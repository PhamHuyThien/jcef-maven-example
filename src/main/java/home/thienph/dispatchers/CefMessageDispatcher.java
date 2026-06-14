package home.thienph.dispatchers;

import home.thienph.Main;
import home.thienph.anotations.CefController;
import home.thienph.anotations.OnCefMessage;
import home.thienph.exceptions.ResponseException;
import home.thienph.jcefs.JcefWindow;
import home.thienph.utils.ClassUtils;
import home.thienph.utils.JsonUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
@Getter
public class CefMessageDispatcher {
    @Getter
    public static final CefMessageDispatcher instance = new CefMessageDispatcher();

    private final Map<String, HandlerMethod> handlers = new HashMap<>();

    public CefMessageDispatcher() {
        this(Main.class.getPackageName());
    }

    public CefMessageDispatcher(String basePackage) {
        try {
            Set<Class<?>> classes = ClassUtils.findClasses(basePackage);
            for (Class<?> clazz : classes) {
                if (!clazz.isAnnotationPresent(CefController.class)) continue;
                Object instance = clazz.getDeclaredConstructor().newInstance();
                for (Method method : clazz.getDeclaredMethods()) {
                    if (!method.isAnnotationPresent(OnCefMessage.class)) continue;
                    String key = method.getAnnotation(OnCefMessage.class).value();
                    method.setAccessible(true);
                    handlers.put(key, new HandlerMethod(instance, method));
                }
            }
            log.info("Scan total {} CefController / {} total class!!!", handlers.size(), classes.size());
        } catch (Exception e) {
            throw new RuntimeException("Scan CefController failed", e);
        }
    }

    public Object dispatch(JcefWindow window, Object[] data) throws Exception {
        if (window == null) throw new ResponseException(500, "window not found");
        if (data == null || data.length == 0)
            throw new ResponseException(500, "data request wrong format");

        HandlerMethod handler = handlers.get(String.valueOf(data[0]));
        if (handler == null) {
            throw new ResponseException(400, "type not found");
        }

        Class<?>[] paramTypes = handler.method.getParameterTypes();

        // Kiểm tra số lượng tham số truyền từ Frontend (data)
        // phải khớp với số lượng tham số của hàm Java (paramTypes)
        // data có data[0] là tên hàm, paramTypes có paramTypes[0] là JcefWindow
        if (data.length != paramTypes.length) {
            throw new ResponseException(400, "parameter length mismatch");
        }

        // Tạo 1 mảng gộp chứa tất cả các đối số thực tế sẽ nạp vào hàm invoke
        Object[] finalArgs = new Object[paramTypes.length];

        // Đối số đầu tiên LUÔN LUÔN là window
        finalArgs[0] = window;

        // Vòng lặp parse các tham số còn lại (từ vị trí số 1 trở đi)
        for (int i = 1; i < paramTypes.length; i++) {
            // data[i] tương ứng với paramTypes[i]
            finalArgs[i] = JsonUtils.fromJson(data[i], paramTypes[i]);
        }

        // Truyền toàn bộ mảng gộp finalArgs vào đối số thứ 2 của hàm invoke
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
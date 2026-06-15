package home.thienph.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JsonUtils {
    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            // 1. Ignore field thừa (JS gửi dư cũng không crash)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            // 2. Không lỗi khi thiếu field (partial object từ JS)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
            // 3. Không fail khi null input
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false)
            // 4. Không serialize null fields (quan trọng)
            .setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
            // 5. Accept single value as array (JS gửi linh hoạt)
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            // 6. Pretty optional (debug thôi, prod nên off)
            // .enable(SerializationFeature.INDENT_OUTPUT);
            // 7. Date/Time config chuẩn Java 8+
            .registerModule(new JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public static String toJson(Object o) {
        try {
            return OBJECT_MAPPER.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize object to JSON", e);
            return null;
        }
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to object", e);
            return null;
        }
    }

    public static <T> T fromJson(Object data, Class<T> clazz) {
        try {
            return OBJECT_MAPPER.convertValue(data, clazz);
        } catch (Exception e) {
            log.error("Failed to convert object to JSON", e);
            return null;
        }
    }
}

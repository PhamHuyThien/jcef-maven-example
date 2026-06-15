package home.thienph.jcef.utils;

public class StringUtils {
    public static final String EMPTY = "";

    public static String normalizeToString(Object data) {
        if (data == null) return EMPTY;
        if (data instanceof String
                || data instanceof Number
                || data instanceof Boolean
                || data instanceof Character)
            return String.valueOf(data);
        return JsonUtils.toJson(data);
    }
}

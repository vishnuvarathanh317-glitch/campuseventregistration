import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JsonUtil — Manual JSON builder/parser (no external libraries).
 */
public class JsonUtil {

    // ─── Builders ───────────────────────────────────────────────────────────

    public static String success(String message) {
        return "{\"success\":true,\"message\":" + quote(message) + "}";
    }

    public static String error(String message) {
        return "{\"success\":false,\"error\":" + quote(message) + "}";
    }

    public static String data(String jsonValue) {
        return "{\"success\":true,\"data\":" + jsonValue + "}";
    }

    public static String page(String arrayJson, int total, int page, int pageSize) {
        return "{\"success\":true,\"data\":" + arrayJson +
               ",\"total\":" + total +
               ",\"page\":" + page +
               ",\"pageSize\":" + pageSize + "}";
    }

    public static String mapToJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            sb.append(quote(entry.getKey())).append(":");
            sb.append(valueToJson(entry.getValue()));
            first = false;
        }
        sb.append("}");
        return sb.toString();
    }

    public static String listToJsonArray(List<String> jsonItems) {
        return "[" + String.join(",", jsonItems) + "]";
    }

    public static String valueToJson(Object value) {
        if (value == null)              return "null";
        if (value instanceof Boolean)   return value.toString();
        if (value instanceof Number)    return value.toString();
        if (value instanceof String)    return quote((String) value);
        return quote(value.toString());
    }

    public static String quote(String s) {
        if (s == null) return "null";
        StringBuilder sb = new StringBuilder("\"");
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n");  break;
                case '\r': sb.append("\\r");  break;
                case '\t': sb.append("\\t");  break;
                default:   sb.append(c);
            }
        }
        sb.append("\"");
        return sb.toString();
    }

    // ─── Parser ─────────────────────────────────────────────────────────────

    /**
     * Extracts the value for a given key from a JSON string.
     */
    public static String extractValue(String json, String key) {
        if (json == null || key == null) return null;

        // 1. Match string values: "key"\s*:\s*"([^"]*)"
        Pattern p1 = Pattern.compile("[\"']?" + Pattern.quote(key) + "[\"']?\\s*:\\s*\"([^\"]*)\"");
        Matcher m1 = p1.matcher(json);
        if (m1.find()) {
            return m1.group(1);
        }

        // 2. Match non-quoted values: "key"\s*:\s*([^,}\s]+)
        Pattern p2 = Pattern.compile("[\"']?" + Pattern.quote(key) + "[\"']?\\s*:\\s*([^,}\\s]+)");
        Matcher m2 = p2.matcher(json);
        if (m2.find()) {
            String val = m2.group(1).trim().replace("\"", "").replace("'", "");
            return "null".equalsIgnoreCase(val) ? null : val;
        }

        return null;
    }

    public static int extractInt(String json, String key, int defaultValue) {
        String val = extractValue(json, key);
        if (val == null) return defaultValue;
        try { return Integer.parseInt(val.trim()); }
        catch (NumberFormatException e) { return defaultValue; }
    }
}

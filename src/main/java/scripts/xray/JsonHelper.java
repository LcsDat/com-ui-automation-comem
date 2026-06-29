package scripts.xray;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JSON utilities nhẹ (không dùng thư viện ngoài).
 * Chỉ hỗ trợ đủ dùng cho Jira/Xray API: escape, quote, extract field, split objects.
 */
public final class JsonHelper {

    private JsonHelper() {}

    /** Escape các ký tự đặc biệt JSON: ", \, \n, \r, \t, control chars. */
    public static String escape(String s) {
        if (s == null) return "";
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '"':  sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }

    /** Bọc string trong dấu ngoặc kép JSON: "value" (đã escape bên trong). */
    public static String quoted(String s) {
        return "\"" + escape(s) + "\"";
    }

    /**
     * Trích xuất giá trị string từ JSON bằng regex.
     * Tìm pattern "field":"value". Return null nếu không tìm thấy.
     */
    public static String extractStringField(String json, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return unescape(m.group(1));
        }
        return null;
    }

    /** Trích xuất giá trị boolean (true/false) từ JSON bằng regex. */
    public static String extractBooleanField(String json, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    /**
     * Trích xuất tất cả "message" từ JSON errors array, nối bằng space.
     * Fallback trả về raw JSON nếu không tìm thấy field "message".
     */
    public static String extractErrorMessages(String json) {
        Pattern p = Pattern.compile("\"message\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (!sb.isEmpty()) sb.append(" ");
            sb.append(unescape(m.group(1)));
        }
        return sb.isEmpty() ? json : sb.toString();
    }

    /**
     * Tách JSON array thành từng object top-level.
     * Theo dõi brace depth + inString để không bị nhầm bởi nested objects.
     */
    public static List<String> splitTopLevelObjects(String json) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = -1;
        boolean inString = false;
        boolean escape = false;

        for (int i = 0; i < json.length(); i++) {
            char c = json.charAt(i);

            if (inString) {
                if (escape) {
                    escape = false;
                } else if (c == '\\') {
                    escape = true;
                } else if (c == '"') {
                    inString = false;
                }
                continue;
            }

            if (c == '"') {
                inString = true;
            } else if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    result.add(json.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return result;
    }

    /** Unescape JSON string: \\n → \n, \\\" → ", \\uXXXX → char. */
    public static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(i + 1);
                switch (next) {
                    case '"':  sb.append('"'); i++; break;
                    case '\\': sb.append('\\'); i++; break;
                    case 'n':  sb.append('\n'); i++; break;
                    case 'r':  sb.append('\r'); i++; break;
                    case 't':  sb.append('\t'); i++; break;
                    case 'u':
                        if (i + 5 < s.length()) {
                            String hex = s.substring(i + 2, i + 6);
                            sb.append((char) Integer.parseInt(hex, 16));
                            i += 5;
                        }
                        break;
                    default: sb.append(next); i++;
                }
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}

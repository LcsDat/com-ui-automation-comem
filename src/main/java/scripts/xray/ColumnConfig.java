package scripts.xray;

import java.util.*;

/**
 * Cấu hình mapping CSV columns → Jira/Xray fields.
 *
 * Gồm 2 loại mapping:
 *
 * 1. Core Roles (enum) — có xử lý logic đặc biệt trong code:
 *    SUMMARY, ISSUE_TYPE, ASSIGNEE, LABELS — dùng cho mọi issue type.
 *    ACTIONS, EXPECTED_RESULT, TEST_TYPE, FOLDER — chỉ dùng khi issue type = Test.
 *
 * 2. Dynamic Fields — CSV column map thẳng vào Jira field, không cần sửa code:
 *    "Priority" → "priority"
 *    "Story Points" → "customfield_10016"
 *    "Component" → "components"
 *    Các field này tự động truyền vào payload khi tạo issue.
 *
 * Usage:
 *   ColumnConfig cfg = ColumnConfig.builder()
 *       .map(Role.SUMMARY, "Tên TC")
 *       .map(Role.ISSUE_TYPE, "Loại")
 *       .field("Priority", "priority")                    // dynamic field
 *       .field("Story Points", "customfield_10016")       // custom field
 *       .fieldJson("Component", "components", "array")    // complex field type
 *       .build();
 *
 * CLI:
 *   --column-map "SUMMARY=Tên TC"                → core role
 *   --field "Priority=priority"                  → dynamic field (simple string/name object)
 *   --field-json "Component=components:array"    → dynamic field (JSON array format)
 */
public class ColumnConfig {

    /**
     * Core roles — có logic xử lý đặc biệt trong XrayImporter.
     * Không cần thêm enum mới khi muốn import thêm Jira field.
     */
    public enum Role {
        SUMMARY, ISSUE_TYPE, ASSIGNEE, LABELS,
        ACTIONS, EXPECTED_RESULT, TEST_TYPE, FOLDER,
        COPY_TESTS_FROM
    }

    /**
     * Loại format khi đưa dynamic field vào Jira JSON payload.
     * STRING: "fieldName": "value"
     * NAME_OBJECT: "fieldName": {"name": "value"}
     * ID_OBJECT: "fieldName": {"id": "value"}
     * ARRAY: "fieldName": [{"name": "v1"}, {"name": "v2"}] (split bằng ;)
     * RAW: "fieldName": value (user tự format JSON)
     */
    /**
     * STRING:      "fieldName": "value"
     * NAME_OBJECT: "fieldName": {"name": "value"}
     * KEY_OBJECT:  "fieldName": {"key": "value"}        — dùng cho parent, fixVersion...
     * ID_OBJECT:   "fieldName": {"id": "value"}
     * ARRAY:       "fieldName": [{"name": "v1"}, ...]   — split bằng ;
     * RAW:         "fieldName": value                    — user tự format JSON
     */
    public enum FieldType {
        STRING, NAME_OBJECT, KEY_OBJECT, ID_OBJECT, ARRAY, RAW
    }

    public static class DynamicField {
        public final String csvHeader;
        public final String jiraFieldName;
        public final FieldType type;

        public DynamicField(String csvHeader, String jiraFieldName, FieldType type) {
            this.csvHeader = csvHeader;
            this.jiraFieldName = jiraFieldName;
            this.type = type;
        }
    }

    private final Map<Role, String> roleToHeader;
    private final Map<Role, String> roleToDefault;
    private final List<DynamicField> dynamicFields;
    private final Set<String> ignoredHeaders;

    private ColumnConfig(Map<Role, String> roleToHeader, Map<Role, String> roleToDefault,
                         List<DynamicField> dynamicFields, Set<String> ignoredHeaders) {
        this.roleToHeader = roleToHeader;
        this.roleToDefault = roleToDefault;
        this.dynamicFields = dynamicFields;
        this.ignoredHeaders = ignoredHeaders;
    }

    public static ColumnConfig defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static HeaderMapper fromCsvHeaders(List<String> headers) {
        return new HeaderMapper(headers);
    }

    /** Resolve core role value từ normalized row. */
    public String resolve(Map<String, String> normalizedRow, Role role) {
        String header = roleToHeader.get(role);
        if (header == null) return roleToDefault.getOrDefault(role, "");

        String value = normalizedRow.get(header);
        if (value != null && !value.trim().isEmpty()) return value.trim();
        return roleToDefault.getOrDefault(role, "");
    }

    /**
     * Resolve tất cả dynamic fields từ normalized row.
     * Trả về Map<jiraFieldName, JSON fragment> sẵn sàng đưa vào payload.
     * Chỉ include field có giá trị (bỏ qua empty).
     */
    public Map<String, String> resolveDynamicFields(Map<String, String> normalizedRow) {
        Map<String, String> result = new LinkedHashMap<>();
        for (DynamicField df : dynamicFields) {
            String normalized = normalizeKey(df.csvHeader);
            String value = normalizedRow.get(normalized);
            if (value == null || value.trim().isEmpty()) continue;
            value = value.trim();

            String jsonFragment = formatFieldValue(value, df.type);
            result.put(df.jiraFieldName, jsonFragment);
        }
        return result;
    }

    public List<DynamicField> getDynamicFields() {
        return Collections.unmodifiableList(dynamicFields);
    }

    public Set<String> allMappedHeaders() {
        Set<String> all = new LinkedHashSet<>(roleToHeader.values());
        for (DynamicField df : dynamicFields) {
            all.add(normalizeKey(df.csvHeader));
        }
        return all;
    }

    public boolean isIgnored(String header) {
        return ignoredHeaders.contains(normalizeKey(header));
    }

    public static String normalizeKey(String rawHeader) {
        return rawHeader.trim().toUpperCase().replaceAll("[\\s_]+", "_");
    }

    public Map<String, String> normalizeRow(Map<String, String> rawRow) {
        Map<String, String> norm = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : rawRow.entrySet()) {
            norm.put(normalizeKey(e.getKey()), e.getValue());
        }
        if (!norm.containsKey("SUMMARY") && norm.containsKey("TITLE")) {
            norm.put("SUMMARY", norm.get("TITLE"));
        }
        return norm;
    }

    // =========================================================================
    // Format helpers
    // =========================================================================

    private String formatFieldValue(String value, FieldType type) {
        switch (type) {
            case STRING:
                return JsonHelper.quoted(value);
            case NAME_OBJECT:
                return "{\"name\":" + JsonHelper.quoted(value) + "}";
            case KEY_OBJECT:
                return "{\"key\":" + JsonHelper.quoted(value) + "}";
            case ID_OBJECT:
                return "{\"id\":" + JsonHelper.quoted(value) + "}";
            case ARRAY:
                String[] parts = value.split("[;,]");
                StringBuilder sb = new StringBuilder("[");
                boolean first = true;
                for (String part : parts) {
                    String trimmed = part.trim();
                    if (trimmed.isEmpty()) continue;
                    if (!first) sb.append(",");
                    sb.append("{\"name\":").append(JsonHelper.quoted(trimmed)).append("}");
                    first = false;
                }
                sb.append("]");
                return sb.toString();
            case RAW:
                return value;
            default:
                return JsonHelper.quoted(value);
        }
    }

    // =========================================================================
    // Builder
    // =========================================================================

    public static class Builder {
        private final Map<Role, String> roleToHeader = new LinkedHashMap<>();
        private final Map<Role, String> roleToDefault = new LinkedHashMap<>();
        private final List<DynamicField> dynamicFields = new ArrayList<>();
        private final Set<String> ignored = new LinkedHashSet<>();

        private Builder() {
            roleToHeader.put(Role.SUMMARY, "SUMMARY");
            roleToHeader.put(Role.ISSUE_TYPE, "ISSUE_TYPE");
            roleToHeader.put(Role.ACTIONS, "ACTIONS");
            roleToHeader.put(Role.EXPECTED_RESULT, "EXPECTED_RESULT");
            roleToHeader.put(Role.LABELS, "LABELS");
            roleToHeader.put(Role.TEST_TYPE, "TEST_TYPE");
            roleToHeader.put(Role.FOLDER, "FOLDER");
            roleToHeader.put(Role.ASSIGNEE, "ASSIGNEE");
            roleToHeader.put(Role.COPY_TESTS_FROM, "COPY_TESTS_FROM");

            roleToDefault.put(Role.ISSUE_TYPE, "Test");
            roleToDefault.put(Role.TEST_TYPE, "Manual");
        }

        public Builder map(Role role, String csvHeader) {
            roleToHeader.put(role, normalizeKey(csvHeader));
            return this;
        }

        public Builder defaultValue(Role role, String value) {
            roleToDefault.put(role, value);
            return this;
        }

        public Builder remove(Role role) {
            roleToHeader.remove(role);
            return this;
        }

        /** Map CSV column → Jira field, format NAME_OBJECT (mặc định). Ví dụ: priority → {"name":"High"} */
        public Builder field(String csvHeader, String jiraFieldName) {
            dynamicFields.add(new DynamicField(csvHeader, jiraFieldName, FieldType.NAME_OBJECT));
            return this;
        }

        /** Map CSV column → Jira field với format chỉ định. */
        public Builder field(String csvHeader, String jiraFieldName, FieldType type) {
            dynamicFields.add(new DynamicField(csvHeader, jiraFieldName, type));
            return this;
        }

        public Builder ignore(String csvHeader) {
            ignored.add(normalizeKey(csvHeader));
            return this;
        }

        public ColumnConfig build() {
            return new ColumnConfig(
                    Collections.unmodifiableMap(new LinkedHashMap<>(roleToHeader)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(roleToDefault)),
                    Collections.unmodifiableList(new ArrayList<>(dynamicFields)),
                    Collections.unmodifiableSet(new LinkedHashSet<>(ignored))
            );
        }
    }

    // =========================================================================
    // HeaderMapper — auto-detect từ CSV file
    // =========================================================================

    public static class HeaderMapper {
        private final List<String> availableHeaders;
        private final Map<Role, String> roleToHeader = new LinkedHashMap<>();
        private final Map<Role, String> roleToDefault = new LinkedHashMap<>();
        private final List<DynamicField> dynamicFields = new ArrayList<>();
        private final Set<String> ignored = new LinkedHashSet<>();

        private HeaderMapper(List<String> headers) {
            this.availableHeaders = new ArrayList<>(headers);

            roleToHeader.put(Role.SUMMARY, "SUMMARY");
            roleToHeader.put(Role.ISSUE_TYPE, "ISSUE_TYPE");
            roleToHeader.put(Role.ACTIONS, "ACTIONS");
            roleToHeader.put(Role.EXPECTED_RESULT, "EXPECTED_RESULT");
            roleToHeader.put(Role.LABELS, "LABELS");
            roleToHeader.put(Role.TEST_TYPE, "TEST_TYPE");
            roleToHeader.put(Role.FOLDER, "FOLDER");
            roleToHeader.put(Role.ASSIGNEE, "ASSIGNEE");
            roleToHeader.put(Role.COPY_TESTS_FROM, "COPY_TESTS_FROM");

            roleToDefault.put(Role.ISSUE_TYPE, "Test");
            roleToDefault.put(Role.TEST_TYPE, "Manual");
        }

        public List<String> getAvailableHeaders() {
            return Collections.unmodifiableList(availableHeaders);
        }

        public HeaderMapper map(String csvHeader, Role role) {
            String normalized = normalizeKey(csvHeader);
            if (!containsHeader(normalized)) {
                System.out.println("[Warn] Header '" + csvHeader + "' not found in CSV. Available: " + availableHeaders);
            }
            roleToHeader.put(role, normalized);
            return this;
        }

        /** Map CSV column → Jira field (NAME_OBJECT format mặc định). */
        public HeaderMapper field(String csvHeader, String jiraFieldName) {
            dynamicFields.add(new DynamicField(csvHeader, jiraFieldName, FieldType.NAME_OBJECT));
            return this;
        }

        public HeaderMapper field(String csvHeader, String jiraFieldName, FieldType type) {
            dynamicFields.add(new DynamicField(csvHeader, jiraFieldName, type));
            return this;
        }

        public HeaderMapper ignore(String csvHeader) {
            ignored.add(normalizeKey(csvHeader));
            return this;
        }

        public HeaderMapper defaultValue(Role role, String value) {
            roleToDefault.put(role, value);
            return this;
        }

        public ColumnConfig build() {
            Set<String> mapped = new LinkedHashSet<>(roleToHeader.values());
            for (DynamicField df : dynamicFields) mapped.add(normalizeKey(df.csvHeader));

            for (String header : availableHeaders) {
                String norm = normalizeKey(header);
                if (!mapped.contains(norm) && !ignored.contains(norm)) {
                    System.out.println("[Info] CSV column '" + header + "' is not mapped to any role/field and not ignored.");
                }
            }

            return new ColumnConfig(
                    Collections.unmodifiableMap(new LinkedHashMap<>(roleToHeader)),
                    Collections.unmodifiableMap(new LinkedHashMap<>(roleToDefault)),
                    Collections.unmodifiableList(new ArrayList<>(dynamicFields)),
                    Collections.unmodifiableSet(new LinkedHashSet<>(ignored))
            );
        }

        private boolean containsHeader(String normalized) {
            for (String h : availableHeaders) {
                if (normalizeKey(h).equals(normalized)) return true;
            }
            return false;
        }
    }
}

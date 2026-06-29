package scripts.xray;

import utilities.data.CsvDataReader;

import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpResponse;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Import issues vào Jira (không dùng Xray API).
 * Dùng cho mọi issue type: Task, Bug, Story, Test Execution, Sub-task, ...
 *
 * Programmatic:
 *   new JiraImporter("/path/to/file.csv")
 *       .field("Parent", "parent", ColumnConfig.FieldType.KEY_OBJECT)
 *       .field("Team", "customfield_10001")
 *       .run();
 *
 * CLI:
 *   java scripts.xray.JiraImporter --csv /path/to/file.csv --field "Parent=parent:KEY_OBJECT"
 */
public class JiraImporter {

    private String csvPath;
    private String configPath;
    private final List<String> columnMappings = new ArrayList<>();
    private final List<String> fieldMappings = new ArrayList<>();

    public JiraImporter(String csvPath) {
        this.csvPath = csvPath;
    }

    public JiraImporter configPath(String configPath) { this.configPath = configPath; return this; }
    public JiraImporter columnMap(String role, String csvHeader) { columnMappings.add(role + "=" + csvHeader); return this; }
    public JiraImporter field(String csvHeader, String jiraField) { fieldMappings.add(csvHeader + "=" + jiraField); return this; }
    public JiraImporter field(String csvHeader, String jiraField, ColumnConfig.FieldType type) {
        fieldMappings.add(csvHeader + "=" + jiraField + ":" + type.name());
        return this;
    }

    public void run() throws Exception {
        ImportConfig config = ImportConfig.load(configPath);
        validateJiraConfig(config);

        HttpClient httpClient = buildTrustAllHttpClient();
        CsvDataReader csvReader = new CsvDataReader();

        List<String> headers = csvReader.readHeaders(csvPath);
        System.out.println("CSV headers detected: " + headers);

        ColumnConfig columnConfig = buildColumnConfig(headers, columnMappings, fieldMappings);

        List<Map<String, String>> rawRows = csvReader.readAll(csvPath);
        List<Map<String, String>> rows = normalizeRows(rawRows, columnConfig);
        System.out.println("Loaded " + rows.size() + " issues from: " + csvPath);

        if (rows.isEmpty()) {
            System.out.println("No valid rows to import.");
            return;
        }

        JiraClient jira = new JiraClient(httpClient, config);
        importRows(rows, jira, config, columnConfig);
    }

    // =========================================================================
    // Import logic — chỉ Jira REST, không Xray
    // =========================================================================

    static void importRows(List<Map<String, String>> rows, JiraClient jira,
                           ImportConfig config, ColumnConfig columnConfig) throws Exception {
        Map<String, String> assigneeCache = new LinkedHashMap<>();
        XrayClient xray = null;
        int success = 0, fail = 0;

        for (Map<String, String> row : rows) {
            String summary = row.getOrDefault("SUMMARY", "").trim();
            String issueType = row.getOrDefault("ISSUE_TYPE", "").trim();
            String labelsCell = row.getOrDefault("LABELS", "");
            String assigneeEmail = row.getOrDefault("ASSIGNEE", "").trim();
            String copyTestsFrom = row.getOrDefault("COPY_TESTS_FROM", "").trim();

            if (summary.isEmpty()) continue;
            if (issueType.isEmpty()) issueType = "Task";

            List<String> labels = splitLabels(labelsCell);

            String accountId = null;
            if (!assigneeEmail.isEmpty()) {
                if (!assigneeCache.containsKey(assigneeEmail)) {
                    assigneeCache.put(assigneeEmail, jira.getAccountIdByEmail(assigneeEmail));
                }
                accountId = assigneeCache.get(assigneeEmail);
            }

            Map<String, String> extraFields = columnConfig.resolveDynamicFields(
                    columnConfig.normalizeRow(row));

            HttpResponse<String> response = jira.createIssue(
                    config.getProjectKey(), issueType, summary, labels, accountId, extraFields);

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String issueKey = JsonHelper.extractStringField(response.body(), "key");
                String issueId = JsonHelper.extractStringField(response.body(), "id");
                if (issueKey == null) issueKey = "?";
                System.out.println("  Created: " + issueKey + " [" + issueType + "] — "
                        + summary.substring(0, Math.min(60, summary.length())));

                if (!copyTestsFrom.isEmpty() && issueId != null) {
                    try {
                        if (xray == null) {
                            xray = lazyAuthenticateXray(config);
                        }
                        String sourceIssueId = jira.getIssueId(copyTestsFrom);
                        List<String> testIds = xray.getTestsFromExecution(sourceIssueId);
                        System.out.println("    -> Found " + testIds.size() + " tests in " + copyTestsFrom);

                        if (!testIds.isEmpty()) {
                            xray.addTestsToExecution(issueId, testIds);
                            System.out.println("    -> Copied " + testIds.size() + " tests to " + issueKey);
                        }
                    } catch (Exception e) {
                        System.out.println("    [Warn] Failed to copy tests from " + copyTestsFrom + ": " + e.getMessage());
                    }
                }

                success++;
            } else {
                System.out.println("  FAILED (" + response.statusCode() + "): "
                        + summary.substring(0, Math.min(60, summary.length())));
                System.out.println("    Response: " + response.body().substring(0, Math.min(200, response.body().length())));
                fail++;
            }
        }

        System.out.println("\nDone. " + success + " created, " + fail + " failed.");
    }

    static XrayClient lazyAuthenticateXray(ImportConfig config) throws Exception {
        if (config.getXrayClientId() == null || config.getXrayClientId().isBlank()
                || config.getXrayClientSecret() == null || config.getXrayClientSecret().isBlank()) {
            throw new IllegalStateException(
                    "COPY_TESTS_FROM requires Xray credentials (XRAY_CLIENT_ID + XRAY_CLIENT_SECRET)");
        }
        HttpClient httpClient = buildTrustAllHttpClient();
        XrayClient xray = new XrayClient(httpClient, config);
        System.out.println("    [Xray] Authenticating (lazy)...");
        xray.authenticate(config.getXrayClientId(), config.getXrayClientSecret());
        System.out.println("    [Xray] Authenticated.");
        return xray;
    }

    // =========================================================================
    // Column mapping (reuse logic từ XrayImporter)
    // =========================================================================

    static ColumnConfig buildColumnConfig(List<String> csvHeaders, List<String> mappings, List<String> fieldMappings) {
        ColumnConfig.HeaderMapper mapper = ColumnConfig.fromCsvHeaders(csvHeaders);

        if (mappings != null) {
            for (String mapping : mappings) {
                String[] parts = mapping.split("=", 2);
                if (parts.length != 2) continue;
                String roleName = parts[0].trim().toUpperCase();
                String csvHeader = parts[1].trim();
                try {
                    ColumnConfig.Role role = ColumnConfig.Role.valueOf(roleName);
                    mapper.map(csvHeader, role);
                } catch (IllegalArgumentException ignored) {}
            }
        }

        if (fieldMappings != null) {
            for (String fm : fieldMappings) {
                String[] parts = fm.split("=", 2);
                if (parts.length != 2) continue;
                String csvHeader = parts[0].trim();
                String fieldPart = parts[1].trim();

                String jiraField;
                ColumnConfig.FieldType type = ColumnConfig.FieldType.NAME_OBJECT;

                if (fieldPart.contains(":")) {
                    String[] fp = fieldPart.split(":", 2);
                    jiraField = fp[0].trim();
                    try {
                        type = ColumnConfig.FieldType.valueOf(fp[1].trim().toUpperCase());
                    } catch (IllegalArgumentException e) {
                        type = ColumnConfig.FieldType.NAME_OBJECT;
                    }
                } else {
                    jiraField = fieldPart;
                }

                mapper.field(csvHeader, jiraField, type);
            }
        }
        return mapper.build();
    }

    static List<Map<String, String>> normalizeRows(List<Map<String, String>> rawRows, ColumnConfig columnConfig) {
        List<Map<String, String>> result = new ArrayList<>();
        for (Map<String, String> item : rawRows) {
            Map<String, String> norm = columnConfig.normalizeRow(item);

            String summary = columnConfig.resolve(norm, ColumnConfig.Role.SUMMARY);
            if (summary.isEmpty() || summary.equalsIgnoreCase("nan")) continue;

            Map<String, String> row = new LinkedHashMap<>(norm);
            row.put("SUMMARY", summary);
            for (ColumnConfig.Role role : ColumnConfig.Role.values()) {
                if (role == ColumnConfig.Role.SUMMARY) continue;
                row.put(role.name(), columnConfig.resolve(norm, role));
            }
            result.add(row);
        }
        return result;
    }

    static List<String> splitLabels(String cell) {
        List<String> result = new ArrayList<>();
        if (cell == null || cell.trim().isEmpty()) return result;
        for (String part : cell.split("[;,]")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed.replaceAll("\\s+", "-"));
        }
        return result;
    }

    static void validateJiraConfig(ImportConfig config) {
        StringBuilder missing = new StringBuilder();
        if (config.getJiraBaseUrl() == null || config.getJiraBaseUrl().isBlank())
            missing.append("  - JIRA_BASE_URL\n");
        if (config.getJiraEmail() == null || config.getJiraEmail().isBlank())
            missing.append("  - JIRA_EMAIL\n");
        if (config.getJiraApiToken() == null || config.getJiraApiToken().isBlank())
            missing.append("  - JIRA_API_TOKEN\n");
        if (config.getProjectKey() == null || config.getProjectKey().isBlank())
            missing.append("  - JIRA_PROJECT_KEY\n");

        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required Jira config:\n" + missing);
        }
    }

    static HttpClient buildTrustAllHttpClient() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
        };
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

        return HttpClient.newBuilder()
                .sslContext(sslContext)
                .followRedirects(Redirect.NORMAL)
                .build();
    }

    // =========================================================================
    // CLI entry point
    // =========================================================================

    public static void main(String[] args) throws Exception {
        String csvPath = null;
        String configPath = null;
        boolean listFields = false;
        List<String> columnMappings = new ArrayList<>();
        List<String> fieldMappings = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--csv":        csvPath = args[++i]; break;
                case "--config":     configPath = args[++i]; break;
                case "--column-map": columnMappings.add(args[++i]); break;
                case "--field":      fieldMappings.add(args[++i]); break;
                case "--list-fields": listFields = true; break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
                    System.exit(1);
            }
        }

        if (listFields) {
            ImportConfig config = ImportConfig.load(configPath);
            validateJiraConfig(config);
            new JiraClient(buildTrustAllHttpClient(), config).listFields();
            return;
        }

        if (csvPath == null || csvPath.isBlank()) {
            System.out.println("Usage: java scripts.xray.JiraImporter --csv /path/to/file.csv [--field CsvHeader=jiraField[:type]]");
            System.exit(1);
        }

        JiraImporter importer = new JiraImporter(csvPath);
        importer.configPath = configPath;
        importer.columnMappings.addAll(columnMappings);
        importer.fieldMappings.addAll(fieldMappings);
        importer.run();
    }
}

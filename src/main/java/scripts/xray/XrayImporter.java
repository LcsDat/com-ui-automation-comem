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
 * Entry point: đọc CSV → map columns → import test cases vào Jira/Xray.
 *
 * Usage:
 *   javac *.java
 *   java XrayImporter --csv /path/to/file.csv
 *   java XrayImporter --csv /path/to/file.csv --folder "/PLUK/Sign In"
 *   java XrayImporter --csv /path/to/file.csv --column-map "SUMMARY=Test Name" --column-map "ACTIONS=Steps"
 *   java XrayImporter --list-fields
 *   java XrayImporter --config /path/to/xray-config.properties
 *
 * Credentials: env vars > xray-config.properties > CLI args override
 */
public class XrayImporter {

    private String csvPath;
    private String configPath;
    private String folderOverride;
    private final List<String> columnMappings = new ArrayList<>();
    private final List<String> fieldMappings = new ArrayList<>();

    public XrayImporter(String csvPath) {
        this.csvPath = csvPath;
    }

    public XrayImporter configPath(String configPath) { this.configPath = configPath; return this; }
    public XrayImporter folder(String folder) { this.folderOverride = folder; return this; }
    public XrayImporter columnMap(String role, String csvHeader) { columnMappings.add(role + "=" + csvHeader); return this; }
    public XrayImporter field(String csvHeader, String jiraField) { fieldMappings.add(csvHeader + "=" + jiraField); return this; }
    public XrayImporter field(String csvHeader, String jiraField, ColumnConfig.FieldType type) {
        fieldMappings.add(csvHeader + "=" + jiraField + ":" + type.name());
        return this;
    }

    /**
     * Chạy import programmatic.
     * Ví dụ:
     *   new XrayImporter("/path/to/file.csv")
     *       .configPath("xray-config.properties")
     *       .folder("/PLUK/Sign In")
     *       .columnMap("SUMMARY", "Tên TC")
     *       .field("Priority", "priority")
     *       .run();
     */
    public void run() throws Exception {
        ImportConfig config = ImportConfig.load(configPath)
                .withCliOverrides(null, null, null, null, null, null, null, folderOverride);
        config.validate();

        HttpClient httpClient = buildTrustAllHttpClient();
        CsvDataReader csvReader = new CsvDataReader();

        List<String> headers = csvReader.readHeaders(csvPath);
        System.out.println("CSV headers detected: " + headers);

        ColumnConfig columnConfig = buildColumnConfig(headers, columnMappings, fieldMappings);

        List<Map<String, String>> rawRows = csvReader.readAll(csvPath);
        List<Map<String, String>> rows = normalizeRows(rawRows, columnConfig);
        System.out.println("Loaded " + rows.size() + " test cases from: " + csvPath);

        if (rows.isEmpty()) {
            System.out.println("No valid rows to import.");
            return;
        }

        JiraClient jira = new JiraClient(httpClient, config);
        XrayClient xray = new XrayClient(httpClient, config);

        System.out.println("Authenticating with Xray Cloud API...");
        xray.authenticate(config.getXrayClientId(), config.getXrayClientSecret());
        System.out.println("Xray authentication successful.\n");

        String jiraProjectId = jira.getProjectId(config.getProjectKey());
        System.out.println("Project ID: " + jiraProjectId + "\n");

        String globalFolder = normaliseFolder(config.getFolderPath());

        if (globalFolder != null && !globalFolder.isEmpty()) {
            importRows(rows, globalFolder, jira, xray, jiraProjectId, config, columnConfig);
        } else {
            Map<String, List<Map<String, String>>> folderGroups = groupByFolder(rows);
            System.out.println("Folder groups: " + folderGroups.keySet() + "\n");
            for (Map.Entry<String, List<Map<String, String>>> entry : folderGroups.entrySet()) {
                String folder = entry.getKey();
                System.out.println("--- Uploading " + entry.getValue().size() + " test(s) to folder: '"
                        + (folder.isEmpty() ? "(auto)" : folder) + "' ---");
                importRows(entry.getValue(), folder, jira, xray, jiraProjectId, config, columnConfig);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        String csvPath = null;
        String folderOverride = null;
        String configPath = null;
        String jiraBase = null, jiraEmail = null, jiraToken = null, projectKey = null;
        String xrayBase = null, xrayClientId = null, xrayClientSecret = null;
        boolean listFields = false;
        List<String> columnMappings = new ArrayList<>();
        List<String> fieldMappings = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--csv":                csvPath = args[++i]; break;
                case "--folder":             folderOverride = args[++i]; break;
                case "--config":             configPath = args[++i]; break;
                case "--jira-base":          jiraBase = args[++i]; break;
                case "--jira-email":         jiraEmail = args[++i]; break;
                case "--jira-token":         jiraToken = args[++i]; break;
                case "--project-key":        projectKey = args[++i]; break;
                case "--xray-base":          xrayBase = args[++i]; break;
                case "--xray-client-id":     xrayClientId = args[++i]; break;
                case "--xray-client-secret": xrayClientSecret = args[++i]; break;
                case "--column-map":         columnMappings.add(args[++i]); break;
                case "--field":              fieldMappings.add(args[++i]); break;
                case "--list-fields":        listFields = true; break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
                    System.exit(1);
            }
        }

        // --- Load config: env > properties > CLI override ---
        ImportConfig config = ImportConfig.load(configPath)
                .withCliOverrides(jiraBase, jiraEmail, jiraToken, projectKey,
                        xrayBase, xrayClientId, xrayClientSecret, folderOverride);

        HttpClient httpClient = buildTrustAllHttpClient();

        // --- List fields mode ---
        if (listFields) {
            config.validate();
            new JiraClient(httpClient, config).listFields();
            return;
        }

        // --- Validate CSV path ---
        if (csvPath == null || csvPath.isBlank()) {
            System.out.println("Error: --csv path is required.");
            System.out.println("Usage: java XrayImporter --csv /path/to/file.csv [options]");
            System.exit(1);
        }

        config.validate();

        // --- Read CSV headers + build ColumnConfig ---
        CsvDataReader csvReader = new CsvDataReader();
        List<String> headers = csvReader.readHeaders(csvPath);
        System.out.println("CSV headers detected: " + headers);

        ColumnConfig columnConfig = buildColumnConfig(headers, columnMappings, fieldMappings);

        // --- Load CSV rows ---
        List<Map<String, String>> rawRows = csvReader.readAll(csvPath);
        List<Map<String, String>> rows = normalizeRows(rawRows, columnConfig);
        System.out.println("Loaded " + rows.size() + " test cases from: " + csvPath);

        if (rows.isEmpty()) {
            System.out.println("No valid rows to import.");
            return;
        }

        // --- Import ---
        JiraClient jira = new JiraClient(httpClient, config);
        XrayClient xray = new XrayClient(httpClient, config);

        System.out.println("Authenticating with Xray Cloud API...");
        xray.authenticate(config.getXrayClientId(), config.getXrayClientSecret());
        System.out.println("Xray authentication successful.\n");

        String jiraProjectId = jira.getProjectId(config.getProjectKey());
        System.out.println("Project ID: " + jiraProjectId + "\n");

        String globalFolder = normaliseFolder(config.getFolderPath());

        if (globalFolder != null && !globalFolder.isEmpty()) {
            importRows(rows, globalFolder, jira, xray, jiraProjectId, config, columnConfig);
        } else {
            Map<String, List<Map<String, String>>> folderGroups = groupByFolder(rows);
            System.out.println("Folder groups: " + folderGroups.keySet() + "\n");

            for (Map.Entry<String, List<Map<String, String>>> entry : folderGroups.entrySet()) {
                String folder = entry.getKey();
                System.out.println("--- Uploading " + entry.getValue().size() + " test(s) to folder: '"
                        + (folder.isEmpty() ? "(auto)" : folder) + "' ---");
                importRows(entry.getValue(), folder, jira, xray, jiraProjectId, config, columnConfig);
            }
        }
    }

    // =========================================================================
    // Import logic
    // =========================================================================

    static void importRows(List<Map<String, String>> rows, String folderPath,
                           JiraClient jira, XrayClient xray, String jiraProjectId,
                           ImportConfig config, ColumnConfig columnConfig) throws Exception {
        Map<String, String> assigneeCache = new LinkedHashMap<>();
        Map<String, Boolean> folderCache = new LinkedHashMap<>();
        int success = 0, fail = 0;

        String resolvedFolder = normaliseFolder(folderPath);

        for (Map<String, String> row : rows) {
            String summary = row.getOrDefault("SUMMARY", "").trim();
            String issueType = row.getOrDefault("ISSUE_TYPE", "Test").trim();
            String actionsCell = row.getOrDefault("ACTIONS", "");
            String expectedCell = row.getOrDefault("EXPECTED_RESULT", "");
            String labelsCell = row.getOrDefault("LABELS", "");
            String testType = row.getOrDefault("TEST_TYPE", "Manual").trim();
            String assigneeEmail = row.getOrDefault("ASSIGNEE", "").trim();

            if (summary.isEmpty()) continue;
            if (issueType.isEmpty()) issueType = "Test";
            if (testType.isEmpty()) testType = "Manual";

            List<String> actions = splitBySemicolonOrNewline(actionsCell);
            List<String> expectedResults = splitBySemicolonOrNewline(expectedCell);
            List<String> labels = splitLabels(labelsCell);

            // Determine folder
            String targetFolder;
            List<String> folderSegments;
            if (resolvedFolder != null && !resolvedFolder.isEmpty()) {
                targetFolder = resolvedFolder;
                folderSegments = Arrays.asList(targetFolder.substring(1).split("/"));
            } else {
                List<String> segments = new ArrayList<>();
                for (String s : summary.split("\\|")) segments.add(s.trim());
                folderSegments = segments.size() > 1 ? segments.subList(0, segments.size() - 1) : new ArrayList<>();
                targetFolder = folderSegments.isEmpty() ? "" : "/" + String.join("/", folderSegments);
            }

            // Resolve assignee
            String accountId = null;
            if (!assigneeEmail.isEmpty()) {
                if (!assigneeCache.containsKey(assigneeEmail)) {
                    assigneeCache.put(assigneeEmail, jira.getAccountIdByEmail(assigneeEmail));
                }
                accountId = assigneeCache.get(assigneeEmail);
            }

            // Resolve dynamic fields cho issue này
            Map<String, String> extraFields = columnConfig.resolveDynamicFields(
                    columnConfig.normalizeRow(row));

            // Create Jira issue
            HttpResponse<String> response = jira.createIssue(
                    config.getProjectKey(), issueType, summary, labels, accountId, extraFields);

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String issueKey = JsonHelper.extractStringField(response.body(), "key");
                String issueId = JsonHelper.extractStringField(response.body(), "id");
                if (issueKey == null) issueKey = "?";

                System.out.println("  Created: " + issueKey + " — " + summary.substring(0, Math.min(60, summary.length())));

                if (issueId != null && !issueId.isEmpty() && issueType.equalsIgnoreCase("Test")) {
                    try {
                        xray.updateTestType(issueId, testType);
                        System.out.println("    -> Test Type: " + testType);
                    } catch (Exception e) {
                        System.out.println("    [Warn] Failed to set Test Type: " + e.getMessage());
                    }

                    int stepCount = Math.max(actions.size(), expectedResults.size());
                    for (int i = 0; i < stepCount; i++) {
                        String action = i < actions.size() ? actions.get(i) : "";
                        String expected = i < expectedResults.size() ? expectedResults.get(i) : "";
                        if (action.isEmpty() && expected.isEmpty()) continue;
                        try {
                            xray.addTestStep(issueId, action, expected);
                        } catch (Exception e) {
                            System.out.println("    [Warn] Failed to add step " + (i + 1) + ": " + e.getMessage());
                        }
                    }
                    if (stepCount > 0) System.out.println("    -> Added " + stepCount + " step(s)");

                    if (!targetFolder.isEmpty() && !folderSegments.isEmpty()) {
                        if (!folderCache.containsKey(targetFolder)) {
                            xray.ensureFolderExists(jiraProjectId, targetFolder);
                            folderCache.put(targetFolder, true);
                        }
                        xray.addTestToFolder(jiraProjectId, targetFolder, issueId);
                        System.out.println("    -> Placed in: " + targetFolder);
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

    // =========================================================================
    // Column mapping
    // =========================================================================

    static ColumnConfig buildColumnConfig(List<String> csvHeaders, List<String> mappings, List<String> fieldMappings) {
        ColumnConfig.HeaderMapper mapper = ColumnConfig.fromCsvHeaders(csvHeaders);

        if (mappings != null) {
            for (String mapping : mappings) {
                String[] parts = mapping.split("=", 2);
                if (parts.length != 2) {
                    System.out.println("Invalid --column-map format (expected ROLE=Header): " + mapping);
                    System.exit(1);
                }
                String roleName = parts[0].trim().toUpperCase();
                String csvHeader = parts[1].trim();
                try {
                    ColumnConfig.Role role = ColumnConfig.Role.valueOf(roleName);
                    mapper.map(csvHeader, role);
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown column role: " + roleName
                            + ". Valid roles: " + Arrays.toString(ColumnConfig.Role.values()));
                    System.exit(1);
                }
            }
        }

        if (fieldMappings != null) {
            for (String fm : fieldMappings) {
                // Format: "CsvHeader=jiraField" hoặc "CsvHeader=jiraField:type"
                String[] parts = fm.split("=", 2);
                if (parts.length != 2) {
                    System.out.println("Invalid --field format (expected CsvHeader=jiraField[:type]): " + fm);
                    System.exit(1);
                }
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
                        System.out.println("Unknown field type: " + fp[1]
                                + ". Valid: " + Arrays.toString(ColumnConfig.FieldType.values()));
                        System.exit(1);
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

    // =========================================================================
    // Utilities
    // =========================================================================

    static Map<String, List<Map<String, String>>> groupByFolder(List<Map<String, String>> rows) {
        Map<String, List<Map<String, String>>> groups = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String folder = normaliseFolder(row.get("FOLDER"));
            groups.computeIfAbsent(folder, k -> new ArrayList<>()).add(row);
        }
        return groups;
    }

    static String normaliseFolder(String folder) {
        if (folder == null) return "";
        String f = folder.trim();
        if (f.isEmpty()) return "";
        if (!f.startsWith("/")) f = "/" + f;
        if (f.length() > 1 && f.endsWith("/")) f = f.substring(0, f.length() - 1);
        return f;
    }

    static List<String> splitBySemicolonOrNewline(String cell) {
        List<String> result = new ArrayList<>();
        if (cell == null || cell.trim().isEmpty()) return result;
        for (String part : cell.split("\\r?\\n|;")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) result.add(trimmed);
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

}

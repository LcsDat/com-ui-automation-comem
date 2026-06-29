package scripts.xray;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpClient.Redirect;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Import test cases from a CSV file into Jira/Xray.
 *
 * CSV file format expected (header row, case-insensitive, spaces/underscores
 * in header names are treated the same — e.g. "Expected Result" == "expected_result"):
 *
 *   Summary,Actions,Expected Result,Labels,Test Type,Folder,Assignee
 *   "PLUK | Sign in | Verify login","Open app;Enter creds;Tap login","App opens;Fields show;User is logged in","smoke;regression","Manual","/PLUK/Sign in",
 *   "PLUK | Sign in | Verify OTP","Request OTP;Enter OTP","OTP is sent;User is logged in","",,,"someone@example.com"
 *
 * Columns:
 *   Summary / Title    — Test case title (mapped to Jira SUMMARY field).
 *   Actions            — Xray manual test step actions. Multiple steps in one
 *                         cell are separated by ';' or by a newline. Paired
 *                         with Expected Result by position (step 1 action <->
 *                         step 1 expected result, etc).
 *   Expected Result    — Xray manual test step expected results, same
 *                         separator rules as Actions.
 *   Labels             — Jira labels field. Multiple labels separated by ';'
 *                         or ','. Spaces within a label are converted to '-'
 *                         since Jira labels cannot contain spaces.
 *   Test Type          — Xray Test Type (e.g. Manual, Generic, Cucumber,
 *                         Automated). Defaults to "Manual" if left blank.
 *   Folder             — Xray Test Repository folder path for this test case
 *                         (e.g. "/PLUK/Auto Deduction"). If blank, the folder
 *                         is derived from pipe-separated segments in Summary.
 *   Assignee           — Jira assignee, given as an email address. Looked up
 *                         against Jira to resolve the accountId.
 *
 * Usage:
 *   java CsvToJira.java
 *   java CsvToJira.java --csv /path/to/file.csv
 *   java CsvToJira.java --csv /path/to/file.csv --folder "/PLUK/My Feature"
 *
 * Note: This is a direct conversion of csv_to_jira.py. Credentials are
 * hard-coded below exactly as in the original script, per request. This
 * means the script trusts all SSL certs (mirroring Python's verify=False),
 * which is NOT recommended outside of trusted internal tooling.
 */
public class XrayCsvImporter {

    // =========================================================================
    // Jira Configuration
    // =========================================================================
    static String JIRA_BASE_URL    = "";
    static String JIRA_EMAIL       = "";
    static String JIRA_API_TOKEN   = "";
    static String JIRA_PROJECT_KEY = "";
    // =========================================================================

    // =========================================================================
    // Xray Configuration (Xray Cloud REST API v2)
    // =========================================================================
    static String XRAY_BASE_URL      = "https://xray.cloud.getxray.app";
    static String XRAY_CLIENT_ID     = "";
    static String XRAY_CLIENT_SECRET = "";
    // =========================================================================

    // ----------------------
    // Script defaults — edit for a one-shot run
    // ----------------------
    static String CSV_PATH    = "C:\\PruSer\\Automation\\script\\demoImporter\\TestCaseImportXrayTemplate-copy1.csv";
    static String FOLDER_PATH = "";   // leave blank to use per-row Folder column or auto-derive from Title
    // ----------------------

    static HttpClient httpClient;

    /**
     * Entry point: parse CLI args → build Config + ColumnConfig → load CSV → import theo folder group.
     *
     * Flow:
     *   1. Parse CLI args (--csv, --folder, --column-map, --list-fields, ...) ghi đè lên default.
     *   2. Tạo ColumnConfig từ các --column-map (nếu không có thì dùng default mapping).
     *   3. Nếu --list-fields → in danh sách Jira fields rồi exit.
     *   4. Load CSV → normalize rows qua ColumnConfig → lọc bỏ row trống/nan.
     *   5. Nếu có --folder global → import tất cả rows vào 1 folder duy nhất.
     *   6. Nếu không → group rows theo cột FOLDER của từng row, import từng group riêng.
     */
    public static void main(String[] args) throws Exception {
        httpClient = buildTrustAllHttpClient();

        // --- argparse-equivalent: simple flag parsing -----------------------
        String csvPath = CSV_PATH;
        String folderOverride = FOLDER_PATH;
        String jiraBaseOverride = null;
        String jiraEmailOverride = null;
        String jiraTokenOverride = null;
        String projectKeyOverride = null;
        String xrayBaseOverride = null;
        String xrayClientIdOverride = null;
        String xrayClientSecretOverride = null;
        boolean listFields = false;
        List<String> columnMappings = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--csv":                csvPath = args[++i]; break;
                case "--folder":              folderOverride = args[++i]; break;
                case "--jira-base":           jiraBaseOverride = args[++i]; break;
                case "--jira-email":          jiraEmailOverride = args[++i]; break;
                case "--jira-token":          jiraTokenOverride = args[++i]; break;
                case "--project-key":         projectKeyOverride = args[++i]; break;
                case "--xray-base":           xrayBaseOverride = args[++i]; break;
                case "--xray-client-id":      xrayClientIdOverride = args[++i]; break;
                case "--xray-client-secret":  xrayClientSecretOverride = args[++i]; break;
                case "--list-fields":         listFields = true; break;
                case "--column-map":          columnMappings.add(args[++i]); break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
                    System.exit(1);
            }
        }

        Config cfg = new Config();
        if (jiraBaseOverride != null)          cfg.jiraBaseUrl = jiraBaseOverride;
        if (jiraEmailOverride != null)         cfg.jiraEmail = jiraEmailOverride;
        if (jiraTokenOverride != null)         cfg.jiraApiToken = jiraTokenOverride;
        if (projectKeyOverride != null)        cfg.projectKey = projectKeyOverride;
        if (xrayBaseOverride != null)          cfg.xrayBaseUrl = xrayBaseOverride;
        if (xrayClientIdOverride != null)      cfg.xrayClientId = xrayClientIdOverride;
        if (xrayClientSecretOverride != null)  cfg.xrayClientSecret = xrayClientSecretOverride;

        ColumnConfig columnConfig = buildColumnConfig(columnMappings);

        // --list-fields: print every Jira field (id + name) and exit, so you
        // can look up custom field IDs (e.g. for Test Type) without guessing.
        if (listFields) {
            listJiraFields(cfg.jiraBaseUrl, cfg.jiraEmail, cfg.jiraApiToken);
            return;
        }

        List<Map<String, String>> rows = loadCsvTestCases(csvPath, columnConfig);
        System.out.println("Loaded " + rows.size() + " test cases from: " + Paths.get(csvPath).getFileName());

        String globalFolder = normaliseFolder(folderOverride);
        if (!globalFolder.isEmpty()) {
            System.out.println("Using global folder override: " + globalFolder + "\n");
            cfg.folderPath = globalFolder;
            try {
                importRows(rows, cfg);
            } catch (Exception e) {
                System.out.println("Import failed: " + e.getMessage());
                System.exit(1);
            }
            return;
        }

        // Otherwise group rows by their per-row Folder column and upload per group
        Map<String, List<Map<String, String>>> folderGroups = new LinkedHashMap<>();
        for (Map<String, String> row : rows) {
            String folderKey = normaliseFolder(row.get("FOLDER"));
            folderGroups.computeIfAbsent(folderKey, k -> new ArrayList<>()).add(row);
        }

        Set<String> keysForDisplay = folderGroups.isEmpty()
                ? new LinkedHashSet<>(List.of("(auto-derived from Title)"))
                : folderGroups.keySet();
        System.out.println("Folder groups: " + keysForDisplay + "\n");

        for (Map.Entry<String, List<Map<String, String>>> entry : folderGroups.entrySet()) {
            String folder = entry.getKey();
            List<Map<String, String>> groupRows = entry.getValue();
            System.out.println("--- Uploading " + groupRows.size() + " test(s) to folder: '"
                    + (folder.isEmpty() ? "(auto)" : folder) + "' ---");
            Config groupCfg = cfg.copy();
            if (!folder.isEmpty()) {
                groupCfg.folderPath = folder;
            }
            try {
                importRows(groupRows, groupCfg);
            } catch (Exception e) {
                System.out.println("Import failed for folder '" + folder + "': " + e.getMessage());
                System.exit(1);
            }
        }
    }

    /**
     * Chuyển đổi list các chuỗi "--column-map ROLE=Header" thành ColumnConfig.
     * Nếu không có mapping nào → trả về config mặc định (7 cột chuẩn).
     * Mỗi entry có format "ROLE=CsvHeader", ví dụ "SUMMARY=Test Name".
     * Throw error nếu role không hợp lệ hoặc format sai.
     */
    static ColumnConfig buildColumnConfig(List<String> mappings) {
        if (mappings == null || mappings.isEmpty()) return ColumnConfig.defaults();

        ColumnConfig.Builder builder = ColumnConfig.builder();
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
                builder.map(role, csvHeader);
            } catch (IllegalArgumentException e) {
                System.out.println("Unknown column role: " + roleName
                        + ". Valid roles: " + java.util.Arrays.toString(ColumnConfig.Role.values()));
                System.exit(1);
            }
        }
        return builder.build();
    }

    /**
     * Chứa toàn bộ thông tin kết nối Jira + Xray + folder path.
     * Mặc định lấy từ các hằng số static ở đầu file.
     * copy() tạo bản sao để mỗi folder group có thể có folderPath riêng mà không ảnh hưởng nhau.
     */
    static class Config {
        String jiraBaseUrl       = JIRA_BASE_URL;
        String jiraEmail         = JIRA_EMAIL;
        String jiraApiToken      = JIRA_API_TOKEN;
        String projectKey        = JIRA_PROJECT_KEY;
        String xrayBaseUrl       = XRAY_BASE_URL;
        String xrayClientId      = XRAY_CLIENT_ID;
        String xrayClientSecret  = XRAY_CLIENT_SECRET;
        String folderPath        = null;

        Config copy() {
            Config c = new Config();
            c.jiraBaseUrl = jiraBaseUrl;
            c.jiraEmail = jiraEmail;
            c.jiraApiToken = jiraApiToken;
            c.projectKey = projectKey;
            c.xrayBaseUrl = xrayBaseUrl;
            c.xrayClientId = xrayClientId;
            c.xrayClientSecret = xrayClientSecret;
            c.folderPath = folderPath;
            return c;
        }
    }

    // ---------------------------------------------------------------------------
    // Xray helpers
    // ---------------------------------------------------------------------------

    /**
     * Xác thực với Xray Cloud API bằng client_id + client_secret.
     * Gửi POST tới /api/v2/authenticate, nhận về JWT Bearer token dạng raw string.
     * Token này dùng cho tất cả các lệnh Xray GraphQL sau đó.
     */
    static String getXrayToken(String xrayBaseUrl, String clientId, String clientSecret) throws Exception {
        String url = stripTrailingSlash(xrayBaseUrl) + "/api/v2/authenticate";
        String body = "{\"client_id\":\"" + jsonEscape(clientId) + "\",\"client_secret\":\"" + jsonEscape(clientSecret) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);
        // Xray returns the token as a raw JSON string, e.g. "eyJ0eXAi..."
        String raw = response.body().trim();
        if (raw.startsWith("\"") && raw.endsWith("\"")) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return raw;
    }

    /**
     * Tạo folder trong Xray Test Repository nếu chưa tồn tại.
     * Gọi GraphQL mutation createFolder. Nếu folder đã có → bỏ qua (không throw).
     * Nếu lỗi khác "already exists" → throw RuntimeException.
     */
    static void ensureFolderExists(String xrayBaseUrl, String token, String projectId, String folderPath) throws Exception {
        String mutation = "mutation { createFolder(projectId: \"" + projectId
                + "\", path: \"" + folderPath
                + "\") { folder { name path } warnings } }";

        String body = "{\"query\":" + jsonString(mutation) + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stripTrailingSlash(xrayBaseUrl) + "/api/v2/graphql"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);

        String respBody = response.body();
        if (respBody.contains("\"errors\"")) {
            String msgs = extractErrorMessages(respBody);
            if (!msgs.toLowerCase().contains("already exists")) {
                throw new RuntimeException("Xray createFolder error: " + msgs);
            }
            System.out.println("    [Xray] Folder already exists: " + folderPath);
        } else {
            System.out.println("    [Xray] Folder ready: " + folderPath);
        }
    }

    /**
     * Thêm 1 test step (Action + Expected Result) vào 1 Test issue qua Xray GraphQL.
     * Gọi mutation addTestStep. Mỗi step gọi 1 lần, theo đúng thứ tự.
     * Action và Expected Result ghép cặp theo vị trí (step 1 ↔ expected 1, ...).
     */
    static void addTestStep(String xrayBaseUrl, String token, String issueId, String action, String expectedResult) throws Exception {
        String mutation = "mutation { addTestStep(issueId: \"" + issueId
                + "\", step: { action: " + jsonString(action)
                + ", result: " + jsonString(expectedResult)
                + " }) { id action result } }";

        String body = "{\"query\":" + jsonString(mutation) + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stripTrailingSlash(xrayBaseUrl) + "/api/v2/graphql"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);

        String respBody = response.body();
        if (respBody.contains("\"errors\"")) {
            throw new RuntimeException("Xray addTestStep error: " + extractErrorMessages(respBody));
        }
    }

    /**
     * Cập nhật Test Type cho 1 Test issue trên Xray (Manual / Generic / Cucumber / Automated).
     * Gọi GraphQL mutation updateTestType. Mặc định "Manual" nếu CSV để trống.
     */
    static void updateTestType(String xrayBaseUrl, String token, String issueId, String testTypeName) throws Exception {
        String mutation = "mutation { updateTestType(issueId: \"" + issueId
                + "\", testType: { name: " + jsonString(testTypeName)
                + " }) { issueId testType { name } } }";

        String body = "{\"query\":" + jsonString(mutation) + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(stripTrailingSlash(xrayBaseUrl) + "/api/v2/graphql"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);

        String respBody = response.body();
        if (respBody.contains("\"errors\"")) {
            throw new RuntimeException("Xray updateTestType error: " + extractErrorMessages(respBody));
        }
    }

    /**
     * Gán 1 Test issue vào folder trong Xray Test Repository.
     * Gọi GraphQL mutation addTestsToFolder. Có cơ chế retry vì Jira đôi khi
     * chưa index xong issue mới tạo → lỗi "don't exist on Jira" / "invalid issue ids".
     * Retry tối đa maxRetries lần, mỗi lần chờ retryDelaySeconds giây.
     */
    static void addTestToFolder(String xrayBaseUrl, String token, String projectId, String folderPath,
                                String issueId, int maxRetries, double retryDelaySeconds) throws Exception {
        String mutation = "mutation { addTestsToFolder(projectId: \"" + projectId
                + "\", path: \"" + folderPath
                + "\", testIssueIds: [\"" + issueId
                + "\"]) { folder { name path } warnings } }";

        String body = "{\"query\":" + jsonString(mutation) + "}";
        String url = stripTrailingSlash(xrayBaseUrl) + "/api/v2/graphql";

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            raiseForStatus(response);

            String respBody = response.body();
            if (!respBody.contains("\"errors\"")) {
                return;
            }

            String errMsg = extractErrorMessages(respBody);
            boolean isIndexLag = errMsg.contains("don't exist on Jira") || errMsg.contains("invalid issue ids");
            if (isIndexLag && attempt < maxRetries) {
                System.out.println("    [Xray] Issue not yet indexed, retrying (" + attempt + "/" + (maxRetries - 1) + ")...");
                Thread.sleep((long) (retryDelaySeconds * 1000));
                continue;
            }
            throw new RuntimeException("Xray addTestsToFolder error: " + errMsg);
        }
    }

    // ---------------------------------------------------------------------------
    // Jira helpers
    // ---------------------------------------------------------------------------

    /**
     * Lấy numeric project ID từ Jira REST API dựa trên project key (ví dụ "GTP" → "12345").
     * Gọi GET /rest/api/2/project/{key}, trích xuất field "id" từ JSON response.
     */
    static String getJiraProjectId(String jiraBaseUrl, String projectKey, String email, String apiToken) throws Exception {
        String url = stripTrailingSlash(jiraBaseUrl) + "/rest/api/2/project/" + projectKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader(email, apiToken))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);
        return extractJsonStringField(response.body(), "id");
    }

    /**
     * In ra toàn bộ Jira fields (id + tên hiển thị) để tra cứu custom field ID.
     * Dùng bởi flag --list-fields. Gọi GET /rest/api/2/field.
     */
    static void listJiraFields(String jiraBaseUrl, String email, String apiToken) throws Exception {
        String url = stripTrailingSlash(jiraBaseUrl) + "/rest/api/2/field";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader(email, apiToken))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);

        List<Map<String, String>> fields = parseFieldListResults(response.body());
        System.out.println("Found " + fields.size() + " fields:\n");
        for (Map<String, String> f : fields) {
            System.out.println("  " + f.getOrDefault("id", "?") + "  —  " + f.getOrDefault("name", "?"));
        }
    }

    /**
     * Parse response từ /field API: tách JSON array thành list các {id, name}.
     * Dùng splitTopLevelJsonObjects để tách từng object, rồi extract id + name.
     */
    static List<Map<String, String>> parseFieldListResults(String json) {
        List<Map<String, String>> fields = new ArrayList<>();
        List<String> objects = splitTopLevelJsonObjects(json);
        for (String obj : objects) {
            String id = extractJsonStringField(obj, "id");
            String name = extractJsonStringField(obj, "name");
            if (id == null && name == null) continue;
            Map<String, String> f = new LinkedHashMap<>();
            if (id != null) f.put("id", id);
            if (name != null) f.put("name", name);
            fields.add(f);
        }
        return fields;
    }

    /**
     * Tra cứu Jira accountId từ email address qua /rest/api/2/user/search.
     * Ưu tiên exact match trên emailAddress nếu Jira trả về field đó.
     * Nếu GDPR mode ẩn emailAddress → fallback lấy user ACTIVE đầu tiên từ kết quả query.
     * Return null nếu không tìm thấy.
     */
    static String getAccountIdByEmail(String jiraBaseUrl, String email, String authEmail, String apiToken) throws Exception {
        String url = stripTrailingSlash(jiraBaseUrl) + "/rest/api/2/user/search?query="
                + URLEncoder.encode(email, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader(authEmail, apiToken))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            System.out.println("    [Warn] Could not look up assignee '" + email + "': HTTP " + response.statusCode());
            return null;
        }

        // Parse the JSON array of user objects.
        //
        // IMPORTANT: On many Jira Cloud sites, "emailAddress" is omitted from the
        // /user/search response entirely (GDPR / privacy mode), even though the
        // search itself is performed server-side by email. So we cannot rely on
        // matching emailAddress in the response - we trust the query's own
        // filtering and take the result(s) returned.
        List<Map<String, String>> users = parseUserSearchResults(response.body());

        if (users.isEmpty()) {
            System.out.println("    [Warn] Assignee email not found in Jira: " + email);
            return null;
        }

        // Prefer an exact emailAddress match if the field happens to be present.
        for (Map<String, String> user : users) {
            String userEmail = user.get("emailAddress");
            if (userEmail != null && userEmail.equalsIgnoreCase(email)) {
                return user.get("accountId");
            }
        }

        // Fallback: emailAddress was hidden/omitted (GDPR mode). Since the Jira
        // API already filtered by this query, accept the first ACTIVE result.
        for (Map<String, String> user : users) {
            String active = user.get("active");
            if (active == null || active.equalsIgnoreCase("true")) {
                String displayName = user.getOrDefault("displayName", "?");
                System.out.println("    [Info] emailAddress hidden by Jira; matched '" + email
                        + "' to account " + displayName + " (" + user.get("accountId") + ") by query result.");
                return user.get("accountId");
            }
        }

        System.out.println("    [Warn] Assignee email not found in Jira: " + email);
        return null;
    }

    // ---------------------------------------------------------------------------
    // Core import logic
    // ---------------------------------------------------------------------------

    /**
     * Logic import chính: duyệt từng row đã normalize → tạo Jira Test issue → cấu hình Xray.
     *
     * Flow cho mỗi row:
     *   1. Lấy SUMMARY, ACTIONS, EXPECTED_RESULT, LABELS, TEST_TYPE, ASSIGNEE từ row.
     *   2. Resolve assignee email → accountId (có cache để tránh gọi API lặp).
     *   3. Xác định folder: dùng folderPathInput (global) hoặc tách từ pipe "|" trong Summary.
     *   4. POST tạo Jira issue type "Test" với summary + labels + assignee.
     *   5. Nếu tạo thành công (201):
     *      a. updateTestType → set Manual/Automated/...
     *      b. addTestStep → thêm từng step (action ↔ expected result theo vị trí).
     *      c. ensureFolderExists + addTestToFolder → gán issue vào folder Xray.
     *   6. Đếm success/fail, in kết quả cuối cùng.
     */
    static void importRows(List<Map<String, String>> rows, Config cfg) throws Exception {
        if (rows.isEmpty()) {
            System.out.println("No rows to import.");
            return;
        }

        System.out.println("Authenticating with Xray Cloud API...");
        String xrayToken = getXrayToken(cfg.xrayBaseUrl, cfg.xrayClientId, cfg.xrayClientSecret);
        System.out.println("Xray authentication successful.\n");

        System.out.println("Looking up numeric project ID for '" + cfg.projectKey + "'...");
        String jiraProjectId = getJiraProjectId(cfg.jiraBaseUrl, cfg.projectKey, cfg.jiraEmail, cfg.jiraApiToken);
        System.out.println("  Project ID: " + jiraProjectId + "\n");

        String apiUrl = stripTrailingSlash(cfg.jiraBaseUrl) + "/rest/api/2/issue";

        // Cache email -> accountId lookups to avoid redundant API calls
        Map<String, String> assigneeCache = new LinkedHashMap<>();

        // Normalise the folder path once
        String folderPathInput = null;
        if (cfg.folderPath != null && !cfg.folderPath.trim().isEmpty()) {
            String p = cfg.folderPath.trim();
            if (!p.startsWith("/")) {
                p = "/" + p;
            }
            if (p.length() > 1 && p.endsWith("/")) {
                p = p.substring(0, p.length() - 1);
            }
            folderPathInput = p;
        }

        Map<String, Boolean> folderIdCache = new LinkedHashMap<>();
        int successCount = 0;
        int failCount = 0;

        for (Map<String, String> row : rows) {
            String summary        = row.getOrDefault("SUMMARY", "").trim();
            String actionsCell    = row.getOrDefault("ACTIONS", "");
            String expectedCell   = row.getOrDefault("EXPECTED_RESULT", "");
            String labelsCell     = row.getOrDefault("LABELS", "");
            String testType       = row.getOrDefault("TEST_TYPE", "Manual").trim();
            String assigneeEmail  = row.getOrDefault("ASSIGNEE", "").trim();

            if (summary.isEmpty()) {
                continue;
            }
            if (testType.isEmpty()) {
                testType = "Manual";
            }

            List<String> actions = splitSteps(actionsCell);
            List<String> expectedResults = splitSteps(expectedCell);
            List<String> labels = splitLabels(labelsCell);

            // Determine folder segments
            List<String> folderSegments;
            String folderDisplay;
            if (folderPathInput != null) {
                folderDisplay = folderPathInput;
                folderSegments = new ArrayList<>();
                for (String seg : folderDisplay.split("/")) {
                    if (!seg.isEmpty()) folderSegments.add(seg);
                }
            } else {
                List<String> segments = new ArrayList<>();
                for (String s : summary.split("\\|")) {
                    segments.add(s.trim());
                }
                folderSegments = segments.size() > 1 ? segments.subList(0, segments.size() - 1) : new ArrayList<>();
                folderDisplay = folderSegments.isEmpty() ? "/" : "/" + String.join("/", folderSegments);
            }

            // Resolve assignee email -> accountId (cached)
            String accountId = null;
            if (!assigneeEmail.isEmpty()) {
                if (!assigneeCache.containsKey(assigneeEmail)) {
                    assigneeCache.put(assigneeEmail,
                            getAccountIdByEmail(cfg.jiraBaseUrl, assigneeEmail, cfg.jiraEmail, cfg.jiraApiToken));
                }
                accountId = assigneeCache.get(assigneeEmail);
            }

            String payload = buildIssuePayload(cfg.projectKey, summary, labels, accountId);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .header("Authorization", basicAuthHeader(cfg.jiraEmail, cfg.jiraApiToken))
                    .POST(HttpRequest.BodyPublishers.ofString(payload))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200 || response.statusCode() == 201) {
                String issueKey = extractJsonStringField(response.body(), "key");
                String issueId  = extractJsonStringField(response.body(), "id");
                if (issueKey == null) issueKey = "?";

                String assigneeLabel = !assigneeEmail.isEmpty() ? " (assignee: " + assigneeEmail + ")" : "";
                System.out.println("  Created: " + issueKey + " — "
                        + summary.substring(0, Math.min(60, summary.length())) + assigneeLabel);

                if (issueId != null && !issueId.isEmpty()) {
                    // Set Xray Test Type
                    try {
                        updateTestType(cfg.xrayBaseUrl, xrayToken, issueId, testType);
                        System.out.println("    → Test Type: " + testType);
                    } catch (Exception e) {
                        System.out.println("    [Warn] Failed to set Test Type: " + e.getMessage());
                    }

                    // Add Action/Expected Result steps (paired by position; if counts
                    // differ, missing expected results default to an empty string)
                    int stepCount = Math.max(actions.size(), expectedResults.size());
                    for (int i = 0; i < stepCount; i++) {
                        String action = i < actions.size() ? actions.get(i) : "";
                        String expected = i < expectedResults.size() ? expectedResults.get(i) : "";
                        if (action.isEmpty() && expected.isEmpty()) continue;
                        try {
                            addTestStep(cfg.xrayBaseUrl, xrayToken, issueId, action, expected);
                        } catch (Exception e) {
                            System.out.println("    [Warn] Failed to add step " + (i + 1) + ": " + e.getMessage());
                        }
                    }
                    if (stepCount > 0) {
                        System.out.println("    → Added " + stepCount + " step(s)");
                    }
                }

                if (issueId != null && !issueId.isEmpty() && !folderSegments.isEmpty()) {
                    String folderPathToUse = folderPathInput != null
                            ? folderPathInput
                            : "/" + String.join("/", folderSegments);
                    if (!folderIdCache.containsKey(folderPathToUse)) {
                        ensureFolderExists(cfg.xrayBaseUrl, xrayToken, jiraProjectId, folderPathToUse);
                        folderIdCache.put(folderPathToUse, true);
                    }
                    addTestToFolder(cfg.xrayBaseUrl, xrayToken, jiraProjectId, folderPathToUse, issueId, 5, 2.0);
                    System.out.println("    → Placed in: " + folderDisplay);
                }

                successCount++;
            } else {
                System.out.println("  FAILED (" + response.statusCode() + "): "
                        + summary.substring(0, Math.min(60, summary.length())));
                String respText = response.body();
                System.out.println("    Response: " + respText.substring(0, Math.min(200, respText.length())));
                failCount++;
            }
        }

        System.out.println("\nDone. " + successCount + " created, " + failCount + " failed.");
    }

    /**
     * Xây dựng JSON payload cho Jira REST API tạo issue.
     * Cấu trúc: { fields: { project, summary, issuetype:"Test", assignee?, labels? } }
     * assignee và labels chỉ thêm vào nếu có giá trị.
     */
    static String buildIssuePayload(String projectKey, String summary, List<String> labels, String accountId) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fields\":{");
        sb.append("\"project\":{\"key\":").append(jsonString(projectKey)).append("},");
        sb.append("\"summary\":").append(jsonString(summary)).append(",");
        sb.append("\"issuetype\":{\"name\":\"Test\"}");
        if (accountId != null) {
            sb.append(",\"assignee\":{\"accountId\":").append(jsonString(accountId)).append("}");
        }
        if (labels != null && !labels.isEmpty()) {
            sb.append(",\"labels\":[");
            for (int i = 0; i < labels.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(jsonString(labels.get(i)));
            }
            sb.append("]");
        }
        sb.append("}}");
        return sb.toString();
    }

    // ---------------------------------------------------------------------------
    // CSV loading
    // ---------------------------------------------------------------------------

    /**
     * Đọc file CSV → normalize header qua ColumnConfig → trả về list rows đã chuẩn hóa.
     *
     * Flow:
     *   1. Đọc CSV thô thành List<Map<String,String>> (header → value).
     *   2. Với mỗi row: gọi columnConfig.normalizeRow() để chuẩn hóa header (uppercase, underscore).
     *   3. Dùng columnConfig.resolve() để map từng role (SUMMARY, ACTIONS, ...) sang giá trị thực.
     *   4. Bỏ qua row có SUMMARY trống hoặc "nan".
     *   5. Output: mỗi row là Map<RoleName, Value> — key luôn là tên Role chuẩn (SUMMARY, ACTIONS, ...).
     */
    static List<Map<String, String>> loadCsvTestCases(String csvPath, ColumnConfig columnConfig) throws IOException {
        Path path = Paths.get(csvPath);
        if (!Files.exists(path)) {
            System.out.println("CSV file not found: " + csvPath);
            System.exit(1);
        }

        List<Map<String, String>> rawRows = readCsvAsDicts(path);

        if (rawRows.isEmpty()) {
            System.out.println("CSV file is empty.");
            System.exit(1);
        }

        List<Map<String, String>> rows = new ArrayList<>();
        for (Map<String, String> item : rawRows) {
            Map<String, String> norm = columnConfig.normalizeRow(item);

            String summary = columnConfig.resolve(norm, ColumnConfig.Role.SUMMARY);
            if (summary.isEmpty() || summary.equalsIgnoreCase("nan")) {
                continue;
            }

            Map<String, String> row = new LinkedHashMap<>();
            row.put("SUMMARY", summary);
            for (ColumnConfig.Role role : ColumnConfig.Role.values()) {
                if (role == ColumnConfig.Role.SUMMARY) continue;
                row.put(role.name(), columnConfig.resolve(norm, role));
            }
            rows.add(row);
        }

        return rows;
    }

    /**
     * Tách 1 cell chứa nhiều test step thành danh sách từng step riêng lẻ.
     * Separator: dấu ";" hoặc xuống dòng (\n, \r\n). Bỏ qua segment trống.
     * Ví dụ: "Open app;Enter creds;Tap login" → ["Open app", "Enter creds", "Tap login"]
     */
    static List<String> splitSteps(String cell) {
        List<String> result = new ArrayList<>();
        if (cell == null || cell.trim().isEmpty()) {
            return result;
        }
        for (String part : cell.split("\\r?\\n|;")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    /**
     * Tách 1 cell chứa nhiều labels thành danh sách labels riêng lẻ.
     * Separator: dấu ";" hoặc ",". Khoảng trắng trong label bị thay bằng "-"
     * vì Jira không cho phép label chứa space.
     * Ví dụ: "smoke;regression test" → ["smoke", "regression-test"]
     */
    static List<String> splitLabels(String cell) {
        List<String> result = new ArrayList<>();
        if (cell == null || cell.trim().isEmpty()) {
            return result;
        }
        for (String part : cell.split("[;,]")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed.replaceAll("\\s+", "-"));
            }
        }
        return result;
    }

    /**
     * Chuẩn hóa folder path: đảm bảo bắt đầu bằng "/" và không kết thúc bằng "/".
     * null hoặc blank → trả về "". Ví dụ: "PLUK/Sign in/" → "/PLUK/Sign in"
     */
    static String normaliseFolder(String folder) {
        if (folder == null) return "";
        String f = folder.trim();
        if (f.isEmpty()) return "";
        if (!f.startsWith("/")) {
            f = "/" + f;
        }
        if (f.length() > 1 && f.endsWith("/")) {
            f = f.substring(0, f.length() - 1);
        }
        return f;
    }

    /**
     * Đọc file CSV thành List<Map<header, value>> (tương tự Python csv.DictReader).
     * Tự strip UTF-8 BOM nếu có. Row đầu tiên là header, các row sau là data.
     * Bỏ qua trailing blank lines. Mỗi Map dùng header gốc (chưa normalize) làm key.
     */
    static List<Map<String, String>> readCsvAsDicts(Path path) throws IOException {
        String content = Files.readString(path, StandardCharsets.UTF_8);
        if (!content.isEmpty() && content.charAt(0) == '\uFEFF') {
            content = content.substring(1);
        }

        List<List<String>> records = parseCsv(content);
        List<Map<String, String>> result = new ArrayList<>();
        if (records.isEmpty()) return result;

        List<String> headers = records.get(0);
        for (int i = 1; i < records.size(); i++) {
            List<String> rec = records.get(i);
            // Skip fully blank trailing lines
            if (rec.size() == 1 && rec.get(0).isEmpty()) continue;
            Map<String, String> row = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                String value = c < rec.size() ? rec.get(c) : "";
                row.put(headers.get(c), value);
            }
            result.add(row);
        }
        return result;
    }

    /**
     * Parser CSV thủ công (không dùng thư viện ngoài).
     * Hỗ trợ: quoted fields ("..."), escaped quotes (""), dấu phẩy và xuống dòng trong field.
     * Duyệt từng ký tự, toggle trạng thái inQuotes khi gặp '"'.
     * Khi gặp ',' (ngoài quotes) → kết thúc field. Khi gặp '\n' → kết thúc record.
     * Trả về List<List<String>>: mỗi inner list là 1 row, mỗi String là 1 field.
     */
    static List<List<String>> parseCsv(String content) {
        List<List<String>> records = new ArrayList<>();
        List<String> current = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean inQuotes = false;
        int i = 0;
        int len = content.length();

        while (i < len) {
            char c = content.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < len && content.charAt(i + 1) == '"') {
                        field.append('"');
                        i += 2;
                        continue;
                    } else {
                        inQuotes = false;
                        i++;
                        continue;
                    }
                } else {
                    field.append(c);
                    i++;
                    continue;
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                    i++;
                    continue;
                } else if (c == ',') {
                    current.add(field.toString());
                    field.setLength(0);
                    i++;
                    continue;
                } else if (c == '\r') {
                    i++;
                    continue;
                } else if (c == '\n') {
                    current.add(field.toString());
                    field.setLength(0);
                    records.add(current);
                    current = new ArrayList<>();
                    i++;
                    continue;
                } else {
                    field.append(c);
                    i++;
                    continue;
                }
            }
        }
        // Final field/record (if file doesn't end with newline)
        if (field.length() > 0 || !current.isEmpty()) {
            current.add(field.toString());
            records.add(current);
        }
        return records;
    }

    // ---------------------------------------------------------------------------
    // JSON helpers — không dùng thư viện ngoài, chỉ đủ dùng cho script này
    // ---------------------------------------------------------------------------

    /** Escape các ký tự đặc biệt trong JSON string: ", \, \n, \r, \t, control chars. */
    static String jsonEscape(String s) {
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
    static String jsonString(String s) {
        return "\"" + jsonEscape(s) + "\"";
    }

    /**
     * Trích xuất giá trị string từ JSON bằng regex (không parse full JSON).
     * Tìm pattern "field":"value" ở top-level. Hỗ trợ escaped chars trong value.
     * Ví dụ: extractJsonStringField(json, "key") → lấy giá trị của "key":"GTP-123"
     */
    static String extractJsonStringField(String json, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return unescapeJson(m.group(1));
        }
        return null;
    }

    /** Unescape JSON string: chuyển \\n → \n, \\\" → ", \\uXXXX → ký tự unicode, v.v. */
    static String unescapeJson(String s) {
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

    /**
     * Trích xuất tất cả "message" từ JSON errors array, nối bằng space.
     * Dùng để hiển thị lỗi từ Xray/Jira API response.
     * Nếu không tìm thấy field "message" → trả về raw JSON làm fallback.
     */
    static String extractErrorMessages(String json) {
        Pattern p = Pattern.compile("\"message\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"");
        Matcher m = p.matcher(json);
        StringBuilder sb = new StringBuilder();
        while (m.find()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(unescapeJson(m.group(1)));
        }
        if (sb.length() == 0) {
            // Fallback: return raw errors block if no message field matched
            return json;
        }
        return sb.toString();
    }

    /**
     * Parse response từ /user/search API: tách JSON array thành list user objects.
     * Mỗi user chứa accountId, emailAddress (có thể bị ẩn do GDPR), displayName, active.
     * Dùng splitTopLevelJsonObjects để xử lý nested objects (avatarUrls, ...) chính xác.
     */
    static List<Map<String, String>> parseUserSearchResults(String json) {
        List<Map<String, String>> users = new ArrayList<>();
        // Split into top-level array elements by tracking brace depth, so that
        // nested objects (e.g. "avatarUrls": {...}) don't get mistaken for
        // separate user entries the way a naive {[^{}]*} regex would.
        List<String> objects = splitTopLevelJsonObjects(json);
        for (String obj : objects) {
            Map<String, String> user = new LinkedHashMap<>();
            String accountId   = extractJsonStringField(obj, "accountId");
            String email       = extractJsonStringField(obj, "emailAddress");
            String displayName = extractJsonStringField(obj, "displayName");
            String active      = extractJsonBooleanField(obj, "active");
            if (accountId != null) user.put("accountId", accountId);
            if (email != null) user.put("emailAddress", email);
            if (displayName != null) user.put("displayName", displayName);
            if (active != null) user.put("active", active);
            if (!user.isEmpty()) users.add(user);
        }
        return users;
    }

    /**
     * Tách JSON array thành từng object riêng lẻ ở top-level.
     * Theo dõi brace depth ({...}) và trạng thái inString ("...") để không bị
     * nhầm lẫn bởi dấu {} hoặc dấu phẩy bên trong nested objects hay string values.
     * Ví dụ: [{"a":1,"b":{"c":2}}, {"d":3}] → ["{"a":1,...}", "{"d":3}"]
     */
    static List<String> splitTopLevelJsonObjects(String json) {
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

    /** Trích xuất giá trị boolean (true/false) từ JSON bằng regex. Tương tự extractJsonStringField nhưng cho boolean. */
    static String extractJsonBooleanField(String json, String field) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(field) + "\"\\s*:\\s*(true|false)");
        Matcher m = p.matcher(json);
        if (m.find()) {
            return m.group(1);
        }
        return null;
    }

    // ---------------------------------------------------------------------------
    // HTTP utility helpers
    // ---------------------------------------------------------------------------

    /** Tạo header "Basic <base64(email:token)>" cho Jira REST API authentication. */
    static String basicAuthHeader(String email, String token) {
        String creds = email + ":" + token;
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }

    /** Throw RuntimeException nếu HTTP status code không nằm trong 2xx. Tương tự Python requests.raise_for_status(). */
    static void raiseForStatus(HttpResponse<String> response) {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new RuntimeException("HTTP " + code + " error for url " + response.uri()
                    + " — body: " + response.body());
        }
    }

    /** Xóa dấu "/" ở cuối URL để tránh double-slash khi nối path. */
    static String stripTrailingSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }


    /**
     * Tạo HttpClient bỏ qua SSL certificate verification (trust all certs).
     * Giống Python requests(verify=False). CHỈ dùng cho internal tooling —
     * không an toàn cho production. Cần thiết vì Jira server nội bộ có thể dùng self-signed cert.
     */
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
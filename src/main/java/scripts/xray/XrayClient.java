package scripts.xray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * Xray Cloud GraphQL API client.
 * Chịu trách nhiệm: authenticate, thêm test step, set test type,
 * tạo folder, gán test vào folder.
 */
public class XrayClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private String token;

    public XrayClient(HttpClient httpClient, ImportConfig config) {
        this.httpClient = httpClient;
        this.baseUrl = stripTrailingSlash(config.getXrayBaseUrl());
    }

    /**
     * Xác thực bằng client_id + client_secret, lưu JWT token nội bộ.
     * Phải gọi trước khi dùng các method khác.
     */
    public void authenticate(String clientId, String clientSecret) throws Exception {
        String url = baseUrl + "/api/v2/authenticate";
        String body = "{\"client_id\":\"" + JsonHelper.escape(clientId)
                + "\",\"client_secret\":\"" + JsonHelper.escape(clientSecret) + "\"}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);

        String raw = response.body().trim();
        if (raw.startsWith("\"") && raw.endsWith("\"")) {
            raw = raw.substring(1, raw.length() - 1);
        }
        this.token = raw;
    }

    /**
     * Thêm 1 test step (Action + Expected Result) vào Test issue.
     */
    public void addTestStep(String issueId, String action, String expectedResult) throws Exception {
        String mutation = "mutation { addTestStep(issueId: \"" + issueId
                + "\", step: { action: " + JsonHelper.quoted(action)
                + ", result: " + JsonHelper.quoted(expectedResult)
                + " }) { id action result } }";

        String respBody = executeGraphQL(mutation);
        if (respBody.contains("\"errors\"")) {
            throw new RuntimeException("Xray addTestStep error: " + JsonHelper.extractErrorMessages(respBody));
        }
    }

    /**
     * Cập nhật Test Type (Manual / Generic / Cucumber / Automated).
     */
    public void updateTestType(String issueId, String testTypeName) throws Exception {
        String mutation = "mutation { updateTestType(issueId: \"" + issueId
                + "\", testType: { name: " + JsonHelper.quoted(testTypeName)
                + " }) { issueId testType { name } } }";

        String respBody = executeGraphQL(mutation);
        if (respBody.contains("\"errors\"")) {
            throw new RuntimeException("Xray updateTestType error: " + JsonHelper.extractErrorMessages(respBody));
        }
    }

    /**
     * Tạo folder trong Xray Test Repository nếu chưa tồn tại.
     * Bỏ qua lỗi "already exists".
     */
    public void ensureFolderExists(String projectId, String folderPath) throws Exception {
        String mutation = "mutation { createFolder(projectId: \"" + projectId
                + "\", path: \"" + folderPath
                + "\") { folder { name path } warnings } }";

        String respBody = executeGraphQL(mutation);
        if (respBody.contains("\"errors\"")) {
            String msgs = JsonHelper.extractErrorMessages(respBody);
            if (!msgs.toLowerCase().contains("already exists")) {
                throw new RuntimeException("Xray createFolder error: " + msgs);
            }
            System.out.println("    [Xray] Folder already exists: " + folderPath);
        } else {
            System.out.println("    [Xray] Folder ready: " + folderPath);
        }
    }

    /**
     * Gán Test issue vào folder Xray. Có cơ chế retry cho Jira index-lag.
     */
    public void addTestToFolder(String projectId, String folderPath, String issueId) throws Exception {
        String mutation = "mutation { addTestsToFolder(projectId: \"" + projectId
                + "\", path: \"" + folderPath
                + "\", testIssueIds: [\"" + issueId
                + "\"]) { folder { name path } warnings } }";

        int maxRetries = 5;
        double retryDelay = 2.0;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            String respBody = executeGraphQL(mutation);

            if (!respBody.contains("\"errors\"")) {
                return;
            }

            String errMsg = JsonHelper.extractErrorMessages(respBody);
            boolean isIndexLag = errMsg.contains("don't exist on Jira") || errMsg.contains("invalid issue ids");
            if (isIndexLag && attempt < maxRetries) {
                System.out.println("    [Xray] Issue not yet indexed, retrying (" + attempt + "/" + (maxRetries - 1) + ")...");
                Thread.sleep((long) (retryDelay * 1000));
                continue;
            }
            throw new RuntimeException("Xray addTestsToFolder error: " + errMsg);
        }
    }

    /**
     * Lấy danh sách test issue IDs từ 1 Test Execution.
     * Dùng GraphQL query getTestExecution, phân trang qua cursor.
     */
    public List<String> getTestsFromExecution(String testExecIssueId) throws Exception {
        List<String> testIds = new ArrayList<>();
        String cursor = null;
        boolean hasMore = true;

        while (hasMore) {
            String afterClause = cursor != null ? ", after: \"" + cursor + "\"" : "";
            String query = "{ getTestExecution(issueId: \"" + testExecIssueId + "\") {"
                    + " tests(limit: 100" + afterClause + ") {"
                    + " total results { issueId } } } }";

            String respBody = executeGraphQL(query);
            if (respBody.contains("\"errors\"")) {
                throw new RuntimeException("Xray getTestExecution error: " + JsonHelper.extractErrorMessages(respBody));
            }

            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("\"issueId\"\\s*:\\s*\"([^\"]+)\"")
                    .matcher(respBody);
            int count = 0;
            while (m.find()) {
                testIds.add(m.group(1));
                count++;
            }

            hasMore = count == 100;
            if (hasMore && !testIds.isEmpty()) {
                cursor = testIds.get(testIds.size() - 1);
            }
        }

        return testIds;
    }

    /**
     * Add danh sách test issues vào 1 Test Execution.
     * Có retry cho Jira index-lag (TE mới tạo chưa được index).
     */
    public void addTestsToExecution(String testExecIssueId, List<String> testIssueIds) throws Exception {
        if (testIssueIds.isEmpty()) return;

        StringBuilder idsArray = new StringBuilder("[");
        for (int i = 0; i < testIssueIds.size(); i++) {
            if (i > 0) idsArray.append(",");
            idsArray.append("\"").append(testIssueIds.get(i)).append("\"");
        }
        idsArray.append("]");

        String mutation = "mutation { addTestsToTestExecution(issueId: \"" + testExecIssueId
                + "\", testIssueIds: " + idsArray
                + ") { addedTests warning } }";

        int maxRetries = 5;
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            String respBody = executeGraphQL(mutation);

            if (!respBody.contains("\"errors\"")) {
                return;
            }

            String errMsg = JsonHelper.extractErrorMessages(respBody);
            boolean isIndexLag = errMsg.contains("don't exist on Jira") || errMsg.contains("invalid issue ids");
            if (isIndexLag && attempt < maxRetries) {
                System.out.println("    [Xray] Issue not yet indexed, retrying (" + attempt + "/" + (maxRetries - 1) + ")...");
                Thread.sleep(2000);
                continue;
            }
            throw new RuntimeException("Xray addTestsToTestExecution error: " + errMsg);
        }
    }

    public boolean isAuthenticated() {
        return token != null;
    }

    // --- private helpers ---

    private String executeGraphQL(String mutation) throws Exception {
        String body = "{\"query\":" + JsonHelper.quoted(mutation) + "}";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/v2/graphql"))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);
        return response.body();
    }

    private void raiseForStatus(HttpResponse<String> response) {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new RuntimeException("HTTP " + code + " — " + response.uri() + " — " + response.body());
        }
    }

    private static String stripTrailingSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}

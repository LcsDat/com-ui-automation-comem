package scripts.xray;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Jira REST API client.
 * Chịu trách nhiệm: tạo issue, lookup user, list fields, build payload.
 * Dùng Basic Auth (email:token) cho mọi request.
 */
public class JiraClient {

    private final HttpClient httpClient;
    private final String baseUrl;
    private final String email;
    private final String apiToken;

    public JiraClient(HttpClient httpClient, ImportConfig config) {
        this.httpClient = httpClient;
        this.baseUrl = stripTrailingSlash(config.getJiraBaseUrl());
        this.email = config.getJiraEmail();
        this.apiToken = config.getJiraApiToken();
    }

    /**
     * Lấy numeric project ID từ project key (ví dụ "GTP" → "12345").
     */
    public String getProjectId(String projectKey) throws Exception {
        String url = baseUrl + "/rest/api/2/project/" + projectKey;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);
        return JsonHelper.extractStringField(response.body(), "id");
    }

    /**
     * Lấy issue ID từ issue key (ví dụ "GTP-500" → "123456").
     */
    public String getIssueId(String issueKey) throws Exception {
        String url = baseUrl + "/rest/api/2/issue/" + issueKey + "?fields=id";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);
        return JsonHelper.extractStringField(response.body(), "id");
    }

    /**
     * Tạo 1 Jira issue với issue type tùy chọn (Test, Story, Bug, Task, ...).
     * extraFields: map các field bổ sung dạng key → JSON value fragment (đã format sẵn).
     * Ví dụ: {"priority": "{\"name\":\"High\"}", "customfield_10001": "\"value\""}
     */
    public HttpResponse<String> createIssue(String projectKey, String issueType, String summary,
                                            List<String> labels, String accountId,
                                            Map<String, String> extraFields) throws Exception {
        String url = baseUrl + "/rest/api/2/issue";
        String payload = buildIssuePayload(projectKey, issueType, summary, labels, accountId, extraFields);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader())
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Tra cứu Jira accountId từ email.
     * Ưu tiên exact match, fallback lấy active user đầu tiên (GDPR mode ẩn email).
     */
    public String getAccountIdByEmail(String targetEmail) throws Exception {
        String url = baseUrl + "/rest/api/2/user/search?query="
                + URLEncoder.encode(targetEmail, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            System.out.println("    [Warn] Could not look up assignee '" + targetEmail + "': HTTP " + response.statusCode());
            return null;
        }

        List<Map<String, String>> users = parseUserSearchResults(response.body());
        if (users.isEmpty()) {
            System.out.println("    [Warn] Assignee email not found in Jira: " + targetEmail);
            return null;
        }

        for (Map<String, String> user : users) {
            String userEmail = user.get("emailAddress");
            if (userEmail != null && userEmail.equalsIgnoreCase(targetEmail)) {
                return user.get("accountId");
            }
        }

        for (Map<String, String> user : users) {
            String active = user.get("active");
            if (active == null || active.equalsIgnoreCase("true")) {
                String displayName = user.getOrDefault("displayName", "?");
                System.out.println("    [Info] emailAddress hidden by Jira; matched '" + targetEmail
                        + "' to account " + displayName + " (" + user.get("accountId") + ")");
                return user.get("accountId");
            }
        }

        System.out.println("    [Warn] Assignee email not found in Jira: " + targetEmail);
        return null;
    }

    /**
     * In ra toàn bộ Jira fields (id + name) để tra cứu custom field ID.
     */
    public void listFields() throws Exception {
        String url = baseUrl + "/rest/api/2/field";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .header("Authorization", basicAuthHeader())
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        raiseForStatus(response);

        List<String> objects = JsonHelper.splitTopLevelObjects(response.body());
        System.out.println("Found " + objects.size() + " fields:\n");
        for (String obj : objects) {
            String id = JsonHelper.extractStringField(obj, "id");
            String name = JsonHelper.extractStringField(obj, "name");
            if (id != null || name != null) {
                System.out.println("  " + (id != null ? id : "?") + "  —  " + (name != null ? name : "?"));
            }
        }
    }

    /**
     * Xây dựng JSON payload tạo issue.
     * issueType: "Test", "Story", "Bug", "Task", "Sub-task", v.v.
     * extraFields: các field bổ sung — key là field name, value là JSON fragment.
     */
    String buildIssuePayload(String projectKey, String issueType, String summary,
                             List<String> labels, String accountId, Map<String, String> extraFields) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"fields\":{");
        sb.append("\"project\":{\"key\":").append(JsonHelper.quoted(projectKey)).append("},");
        sb.append("\"summary\":").append(JsonHelper.quoted(summary)).append(",");
        sb.append("\"issuetype\":{\"name\":").append(JsonHelper.quoted(issueType)).append("}");
        if (accountId != null) {
            sb.append(",\"assignee\":{\"accountId\":").append(JsonHelper.quoted(accountId)).append("}");
        }
        if (labels != null && !labels.isEmpty()) {
            sb.append(",\"labels\":[");
            for (int i = 0; i < labels.size(); i++) {
                if (i > 0) sb.append(",");
                sb.append(JsonHelper.quoted(labels.get(i)));
            }
            sb.append("]");
        }
        if (extraFields != null) {
            for (Map.Entry<String, String> entry : extraFields.entrySet()) {
                sb.append(",\"").append(JsonHelper.escape(entry.getKey())).append("\":").append(entry.getValue());
            }
        }
        sb.append("}}");
        return sb.toString();
    }

    // --- private helpers ---

    private String basicAuthHeader() {
        String creds = email + ":" + apiToken;
        return "Basic " + Base64.getEncoder().encodeToString(creds.getBytes(StandardCharsets.UTF_8));
    }

    private void raiseForStatus(HttpResponse<String> response) {
        int code = response.statusCode();
        if (code < 200 || code >= 300) {
            throw new RuntimeException("HTTP " + code + " — " + response.uri() + " — " + response.body());
        }
    }

    private List<Map<String, String>> parseUserSearchResults(String json) {
        List<Map<String, String>> users = new ArrayList<>();
        List<String> objects = JsonHelper.splitTopLevelObjects(json);
        for (String obj : objects) {
            Map<String, String> user = new LinkedHashMap<>();
            String accountId = JsonHelper.extractStringField(obj, "accountId");
            String emailAddr = JsonHelper.extractStringField(obj, "emailAddress");
            String displayName = JsonHelper.extractStringField(obj, "displayName");
            String active = JsonHelper.extractBooleanField(obj, "active");
            if (accountId != null) user.put("accountId", accountId);
            if (emailAddr != null) user.put("emailAddress", emailAddr);
            if (displayName != null) user.put("displayName", displayName);
            if (active != null) user.put("active", active);
            if (!user.isEmpty()) users.add(user);
        }
        return users;
    }

    private static String stripTrailingSlash(String url) {
        if (url == null) return "";
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }
}

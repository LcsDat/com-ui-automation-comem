package scripts.xray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Tempo REST API v4 client.
 * Chịu trách nhiệm: tạo worklog entries.
 *
 * API docs: https://apidocs.tempo.io/#tag/Worklogs
 * Auth: Bearer token (Tempo API Token, tạo từ Tempo > Settings > API Integration)
 *
 * Endpoint: POST https://api.tempo.io/4/worklogs
 * Body:
 * {
 *   "issueKey": "GTP-123",
 *   "timeSpentSeconds": 3600,
 *   "startDate": "2026-06-21",
 *   "startTime": "09:00:00",
 *   "authorAccountId": "5e4b...",
 *   "description": "Testing login flow"
 * }
 */
public class TempoClient {

    private static final String TEMPO_BASE_URL = "https://api.tempo.io/4";

    private final HttpClient httpClient;
    private final String apiToken;

    public TempoClient(HttpClient httpClient, String tempoApiToken) {
        this.httpClient = httpClient;
        this.apiToken = tempoApiToken;
    }

    /**
     * Tạo 1 worklog entry trên Tempo.
     *
     * @param issueKey       Jira issue key (ví dụ "GTP-123")
     * @param authorAccountId Jira accountId của người log work
     * @param startDate      Ngày bắt đầu (format: "2026-06-21")
     * @param startTime      Giờ bắt đầu (format: "09:00:00"), nullable
     * @param timeSpentSeconds Thời gian làm việc tính bằng giây
     * @param description    Mô tả công việc, nullable
     * @return HttpResponse chứa kết quả
     */
    public HttpResponse<String> createWorklog(String issueKey, String authorAccountId,
                                              String startDate, String startTime,
                                              int timeSpentSeconds, String description) throws Exception {
        String payload = buildWorklogPayload(issueKey, authorAccountId, startDate, startTime, timeSpentSeconds, description);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEMPO_BASE_URL + "/worklogs"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .POST(HttpRequest.BodyPublishers.ofString(payload))
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    /**
     * Lấy danh sách worklogs theo issue key (để verify / kiểm tra trùng lặp).
     */
    public HttpResponse<String> getWorklogsByIssue(String issueKey) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(TEMPO_BASE_URL + "/worklogs?issue=" + issueKey))
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + apiToken)
                .GET()
                .build();

        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private String buildWorklogPayload(String issueKey, String authorAccountId,
                                       String startDate, String startTime,
                                       int timeSpentSeconds, String description) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"issueKey\":").append(JsonHelper.quoted(issueKey));
        sb.append(",\"authorAccountId\":").append(JsonHelper.quoted(authorAccountId));
        sb.append(",\"startDate\":").append(JsonHelper.quoted(startDate));
        sb.append(",\"timeSpentSeconds\":").append(timeSpentSeconds);

        if (startTime != null && !startTime.isBlank()) {
            sb.append(",\"startTime\":").append(JsonHelper.quoted(startTime));
        }
        if (description != null && !description.isBlank()) {
            sb.append(",\"description\":").append(JsonHelper.quoted(description));
        }
        sb.append("}");
        return sb.toString();
    }
}

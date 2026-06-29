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
 * Import worklogs từ CSV vào Tempo.
 *
 * CSV format:
 *   Issue Key,Date,Hours,Start Time,Description,Author
 *   GTP-123,2026-06-21,2,09:00,Testing login flow,someone@email.com
 *   GTP-124,2026-06-21,1.5,10:30,Writing test cases,
 *
 * Columns:
 *   Issue Key   — (bắt buộc) Jira issue key để log work vào
 *   Date        — (bắt buộc) Ngày bắt đầu (YYYY-MM-DD)
 *   Hours       — (bắt buộc) Số giờ (hỗ trợ decimal: 1.5 = 1h30m)
 *   Start Time  — (tùy chọn) Giờ bắt đầu (HH:mm hoặc HH:mm:ss)
 *   Description — (tùy chọn) Mô tả công việc
 *   Author      — (tùy chọn) Email người log work. Nếu trống → dùng jira.email trong config
 *
 * Usage:
 *   javac *.java
 *   java TempoImporter --csv /path/to/worklogs.csv
 *   java TempoImporter --csv /path/to/worklogs.csv --config /path/to/xray-config.properties
 *   java TempoImporter --csv /path/to/worklogs.csv --column-map "HOURS=Time Spent"
 *
 * Credentials: env TEMPO_API_TOKEN hoặc tempo.api.token trong properties file
 */
public class TempoImporter {

    public enum WorklogRole {
        ISSUE_KEY, DATE, HOURS, START_TIME, DESCRIPTION, AUTHOR
    }

    public static void main(String[] args) throws Exception {
        String csvPath = null;
        String configPath = null;
        List<String> columnMappings = new ArrayList<>();

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--csv":        csvPath = args[++i]; break;
                case "--config":     configPath = args[++i]; break;
                case "--column-map": columnMappings.add(args[++i]); break;
                default:
                    System.out.println("Unknown argument: " + args[i]);
                    System.out.println("Usage: java TempoImporter --csv /path/to/worklogs.csv [--config path] [--column-map ROLE=Header]");
                    System.exit(1);
            }
        }

        if (csvPath == null || csvPath.isBlank()) {
            System.out.println("Error: --csv path is required.");
            System.out.println("Usage: java TempoImporter --csv /path/to/worklogs.csv");
            System.exit(1);
        }

        ImportConfig config = ImportConfig.load(configPath);
        validateTempoConfig(config);

        HttpClient httpClient = buildTrustAllHttpClient();
        TempoClient tempo = new TempoClient(httpClient, config.getTempoApiToken());
        JiraClient jira = new JiraClient(httpClient, config);

        // Read CSV
        CsvDataReader csvReader = new CsvDataReader();
        List<String> headers = csvReader.readHeaders(csvPath);
        System.out.println("CSV headers detected: " + headers);

        Map<WorklogRole, String> roleMapping = buildWorklogMapping(headers, columnMappings);
        List<Map<String, String>> rawRows = csvReader.readAll(csvPath);
        System.out.println("Loaded " + rawRows.size() + " worklog entries.\n");

        // Cache author email → accountId
        Map<String, String> authorCache = new LinkedHashMap<>();
        String defaultAuthorId = jira.getAccountIdByEmail(config.getJiraEmail());

        int success = 0, fail = 0;

        for (Map<String, String> rawRow : rawRows) {
            Map<String, String> row = normalizeRow(rawRow);

            String issueKey   = resolveField(row, roleMapping, WorklogRole.ISSUE_KEY, "");
            String date       = resolveField(row, roleMapping, WorklogRole.DATE, "");
            String hoursStr   = resolveField(row, roleMapping, WorklogRole.HOURS, "0");
            String startTime  = resolveField(row, roleMapping, WorklogRole.START_TIME, "");
            String description = resolveField(row, roleMapping, WorklogRole.DESCRIPTION, "");
            String authorEmail = resolveField(row, roleMapping, WorklogRole.AUTHOR, "");

            if (issueKey.isEmpty() || date.isEmpty()) {
                System.out.println("  [Skip] Missing issue key or date: " + rawRow);
                continue;
            }

            double hours;
            try {
                hours = Double.parseDouble(hoursStr);
            } catch (NumberFormatException e) {
                System.out.println("  [Skip] Invalid hours value '" + hoursStr + "' for " + issueKey);
                continue;
            }
            int timeSpentSeconds = (int) (hours * 3600);
            if (timeSpentSeconds <= 0) {
                System.out.println("  [Skip] Hours must be > 0 for " + issueKey);
                continue;
            }

            // Normalize startTime to HH:mm:ss
            if (!startTime.isEmpty() && startTime.length() == 5) {
                startTime = startTime + ":00";
            }

            // Resolve author
            String authorAccountId;
            if (authorEmail.isEmpty()) {
                authorAccountId = defaultAuthorId;
            } else {
                if (!authorCache.containsKey(authorEmail)) {
                    authorCache.put(authorEmail, jira.getAccountIdByEmail(authorEmail));
                }
                authorAccountId = authorCache.get(authorEmail);
            }

            if (authorAccountId == null) {
                System.out.println("  [Skip] Could not resolve author for " + issueKey + ": " + authorEmail);
                continue;
            }

            // Create worklog
            try {
                HttpResponse<String> response = tempo.createWorklog(
                        issueKey, authorAccountId, date,
                        startTime.isEmpty() ? null : startTime,
                        timeSpentSeconds, description.isEmpty() ? null : description);

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    System.out.println("  OK: " + issueKey + " | " + date + " | " + hours + "h"
                            + (description.isEmpty() ? "" : " | " + description.substring(0, Math.min(40, description.length()))));
                    success++;
                } else {
                    System.out.println("  FAILED (" + response.statusCode() + "): " + issueKey + " | " + date);
                    System.out.println("    " + response.body().substring(0, Math.min(200, response.body().length())));
                    fail++;
                }
            } catch (Exception e) {
                System.out.println("  ERROR: " + issueKey + " | " + e.getMessage());
                fail++;
            }
        }

        System.out.println("\nDone. " + success + " logged, " + fail + " failed.");
    }

    // =========================================================================
    // Column mapping for Tempo
    // =========================================================================

    static Map<WorklogRole, String> buildWorklogMapping(List<String> csvHeaders, List<String> mappings) {
        Map<WorklogRole, String> roleMap = new LinkedHashMap<>();

        // Defaults
        roleMap.put(WorklogRole.ISSUE_KEY, "ISSUE_KEY");
        roleMap.put(WorklogRole.DATE, "DATE");
        roleMap.put(WorklogRole.HOURS, "HOURS");
        roleMap.put(WorklogRole.START_TIME, "START_TIME");
        roleMap.put(WorklogRole.DESCRIPTION, "DESCRIPTION");
        roleMap.put(WorklogRole.AUTHOR, "AUTHOR");

        if (mappings != null) {
            for (String mapping : mappings) {
                String[] parts = mapping.split("=", 2);
                if (parts.length != 2) continue;
                String roleName = parts[0].trim().toUpperCase();
                String header = parts[1].trim().toUpperCase().replaceAll("[\\s_]+", "_");
                try {
                    WorklogRole role = WorklogRole.valueOf(roleName);
                    roleMap.put(role, header);
                } catch (IllegalArgumentException e) {
                    System.out.println("[Warn] Unknown worklog role: " + roleName
                            + ". Valid: " + Arrays.toString(WorklogRole.values()));
                }
            }
        }
        return roleMap;
    }

    static Map<String, String> normalizeRow(Map<String, String> rawRow) {
        Map<String, String> norm = new LinkedHashMap<>();
        for (Map.Entry<String, String> e : rawRow.entrySet()) {
            String key = e.getKey().trim().toUpperCase().replaceAll("[\\s_]+", "_");
            norm.put(key, e.getValue());
        }
        return norm;
    }

    static String resolveField(Map<String, String> row, Map<WorklogRole, String> mapping,
                               WorklogRole role, String defaultValue) {
        String header = mapping.get(role);
        if (header == null) return defaultValue;
        String value = row.get(header);
        if (value != null && !value.trim().isEmpty()) return value.trim();
        return defaultValue;
    }

    static void validateTempoConfig(ImportConfig config) {
        if (config.getTempoApiToken() == null || config.getTempoApiToken().isBlank()) {
            System.out.println("Error: TEMPO_API_TOKEN is required.");
            System.out.println("Set via: env var TEMPO_API_TOKEN, or tempo.api.token in properties file.");
            System.exit(1);
        }
        if (config.getJiraEmail() == null || config.getJiraEmail().isBlank()) {
            System.out.println("Error: JIRA_EMAIL is required (for default author lookup).");
            System.exit(1);
        }
        if (config.getJiraApiToken() == null || config.getJiraApiToken().isBlank()) {
            System.out.println("Error: JIRA_API_TOKEN is required (for author accountId lookup).");
            System.exit(1);
        }
    }

    static HttpClient buildTrustAllHttpClient() throws Exception {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() { return new X509Certificate[0]; }
                    public void checkClientTrusted(X509Certificate[] c, String a) {}
                    public void checkServerTrusted(X509Certificate[] c, String a) {}
                }
        };
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, trustAllCerts, new java.security.SecureRandom());
        return HttpClient.newBuilder().sslContext(ctx).followRedirects(Redirect.NORMAL).build();
    }
}

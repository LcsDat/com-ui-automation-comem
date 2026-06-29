package scripts.xray;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Quản lý credentials và config cho Jira/Xray.
 * Priority load: CLI args > Environment variables > .properties file > hard-coded defaults.
 *
 * Env vars:
 *   JIRA_BASE_URL, JIRA_EMAIL, JIRA_API_TOKEN, JIRA_PROJECT_KEY
 *   XRAY_BASE_URL, XRAY_CLIENT_ID, XRAY_CLIENT_SECRET
 *
 * Properties file (default: ./xray-config.properties):
 *   jira.base.url, jira.email, jira.api.token, jira.project.key
 *   xray.base.url, xray.client.id, xray.client.secret
 */
public class ImportConfig {

    private String jiraBaseUrl;
    private String jiraEmail;
    private String jiraApiToken;
    private String projectKey;
    private String xrayBaseUrl;
    private String xrayClientId;
    private String xrayClientSecret;
    private String tempoApiToken;
    private String folderPath;

    private ImportConfig() {}

    public static ImportConfig load(String propertiesPath) {
        ImportConfig cfg = new ImportConfig();
        Properties props = loadProperties(propertiesPath);

        cfg.jiraBaseUrl      = resolve(null, "JIRA_BASE_URL", props, "jira.base.url", "");
        cfg.jiraEmail        = resolve(null, "JIRA_EMAIL", props, "jira.email", "");
        cfg.jiraApiToken     = resolve(null, "JIRA_API_TOKEN", props, "jira.api.token", "");
        cfg.projectKey       = resolve(null, "JIRA_PROJECT_KEY", props, "jira.project.key", "");
        cfg.xrayBaseUrl      = resolve(null, "XRAY_BASE_URL", props, "xray.base.url", "https://xray.cloud.getxray.app");
        cfg.xrayClientId     = resolve(null, "XRAY_CLIENT_ID", props, "xray.client.id", "");
        cfg.xrayClientSecret = resolve(null, "XRAY_CLIENT_SECRET", props, "xray.client.secret", "");
        cfg.tempoApiToken    = resolve(null, "TEMPO_API_TOKEN", props, "tempo.api.token", "");

        return cfg;
    }

    /**
     * Resolve 1 config value theo priority: cliValue > env var > properties > fallback.
     */
    private static String resolve(String cliValue, String envKey, Properties props, String propKey, String fallback) {
        if (cliValue != null && !cliValue.isBlank()) return cliValue;

        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) return envValue;

        String propValue = props.getProperty(propKey);
        if (propValue != null && !propValue.isBlank()) return propValue;

        return fallback;
    }

    private static Properties loadProperties(String path) {
        Properties props = new Properties();
        if (path == null || path.isBlank()) {
            path = "xray-config.properties";
        }
        Path filePath = Path.of(path);
        if (Files.exists(filePath)) {
            try (InputStream is = Files.newInputStream(filePath)) {
                props.load(is);
            } catch (IOException e) {
                System.out.println("[Warn] Could not load properties file: " + path);
            }
        }
        return props;
    }

    public ImportConfig withCliOverrides(String jiraBase, String email, String token,
                                         String projectKey, String xrayBase,
                                         String clientId, String clientSecret, String folder) {
        if (jiraBase != null && !jiraBase.isBlank())    this.jiraBaseUrl = jiraBase;
        if (email != null && !email.isBlank())          this.jiraEmail = email;
        if (token != null && !token.isBlank())          this.jiraApiToken = token;
        if (projectKey != null && !projectKey.isBlank()) this.projectKey = projectKey;
        if (xrayBase != null && !xrayBase.isBlank())    this.xrayBaseUrl = xrayBase;
        if (clientId != null && !clientId.isBlank())    this.xrayClientId = clientId;
        if (clientSecret != null && !clientSecret.isBlank()) this.xrayClientSecret = clientSecret;
        if (folder != null && !folder.isBlank())        this.folderPath = folder;
        return this;
    }

    public void validate() {
        StringBuilder missing = new StringBuilder();
        if (jiraBaseUrl.isBlank())      missing.append("  - JIRA_BASE_URL\n");
        if (jiraEmail.isBlank())        missing.append("  - JIRA_EMAIL\n");
        if (jiraApiToken.isBlank())     missing.append("  - JIRA_API_TOKEN\n");
        if (projectKey.isBlank())       missing.append("  - JIRA_PROJECT_KEY\n");
        if (xrayClientId.isBlank())     missing.append("  - XRAY_CLIENT_ID\n");
        if (xrayClientSecret.isBlank()) missing.append("  - XRAY_CLIENT_SECRET\n");

        if (!missing.isEmpty()) {
            throw new IllegalStateException(
                    "Missing required config (set via env vars, properties file, or CLI args):\n" + missing);
        }
    }

    public ImportConfig copy() {
        ImportConfig c = new ImportConfig();
        c.jiraBaseUrl = this.jiraBaseUrl;
        c.jiraEmail = this.jiraEmail;
        c.jiraApiToken = this.jiraApiToken;
        c.projectKey = this.projectKey;
        c.xrayBaseUrl = this.xrayBaseUrl;
        c.xrayClientId = this.xrayClientId;
        c.xrayClientSecret = this.xrayClientSecret;
        c.tempoApiToken = this.tempoApiToken;
        c.folderPath = this.folderPath;
        return c;
    }

    // Getters
    public String getJiraBaseUrl()      { return jiraBaseUrl; }
    public String getJiraEmail()        { return jiraEmail; }
    public String getJiraApiToken()     { return jiraApiToken; }
    public String getProjectKey()       { return projectKey; }
    public String getXrayBaseUrl()      { return xrayBaseUrl; }
    public String getXrayClientId()     { return xrayClientId; }
    public String getXrayClientSecret() { return xrayClientSecret; }
    public String getTempoApiToken()    { return tempoApiToken; }
    public String getFolderPath()       { return folderPath; }

    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }
}

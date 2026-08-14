package com.khedmataktak.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * External CV extraction API (Python service). Backend only proxies uploads — no local parsing.
 */
@ConfigurationProperties(prefix = "app.cv-parser")
public class CvParserProperties {

    /** When false, import endpoints reject with a clear error. */
    private boolean enabled = true;

    /** Base URL of the CV extraction service, e.g. http://localhost:8000 */
    private String baseUrl = "http://localhost:8000";

    /** Path for multipart CV import, e.g. /v1/cv/import */
    private String importPath = "/v1/cv/import";

    /** Optional health path for readiness checks, e.g. /health */
    private String healthPath = "/health";

    private int timeoutSeconds = 300;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getImportPath() {
        return importPath;
    }

    public void setImportPath(String importPath) {
        this.importPath = importPath;
    }

    public String getHealthPath() {
        return healthPath;
    }

    public void setHealthPath(String healthPath) {
        this.healthPath = healthPath;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String importUrl() {
        return join(baseUrl, importPath);
    }

    public String healthUrl() {
        return join(baseUrl, healthPath);
    }

    private static String join(String base, String path) {
        String b = base == null ? "" : base.replaceAll("/+$", "");
        String p = path == null || path.isBlank() ? "" : (path.startsWith("/") ? path : "/" + path);
        return b + p;
    }
}

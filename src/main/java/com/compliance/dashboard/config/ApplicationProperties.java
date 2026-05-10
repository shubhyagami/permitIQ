package com.compliance.dashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record ApplicationProperties(
        Jwt jwt,
        Upload upload,
        Gemini gemini,
        Reminder reminder
) {

    public record Jwt(String secret, long expirationMillis) {
    }

    public record Upload(String dir, long maxFileSizeBytes) {
    }

    public record Gemini(String apiKey, String model, String baseUrl) {
    }

    public record Reminder(int expiringSoonDays, String statusRefreshCron, String dailyReminderCron) {
    }
}

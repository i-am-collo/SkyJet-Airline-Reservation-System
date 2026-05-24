package models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Audit log entry for login attempts.
 * Tracks successful and failed login attempts for security auditing.
 */
public class LoginAudit {

    private String userId;
    private String email;
    private String timestamp;
    private boolean success;
    private String reason;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public LoginAudit(String email, boolean success, String reason) {
        this.email = email;
        this.success = success;
        this.reason = reason;
        this.timestamp = LocalDateTime.now().format(formatter);
        this.userId = "UNKNOWN";
    }

    public LoginAudit(String userId, String email, boolean success, String reason) {
        this.userId = userId;
        this.email = email;
        this.success = success;
        this.reason = reason;
        this.timestamp = LocalDateTime.now().format(formatter);
    }

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s (%s)",
                timestamp,
                email,
                success ? "SUCCESS" : "FAILED",
                reason);
    }
}

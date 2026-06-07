package com.skyjet.backend.service;

import com.skyjet.backend.entity.AuditLog;
import com.skyjet.backend.entity.LoginAudit;
import com.skyjet.backend.entity.User;
import com.skyjet.backend.repository.AuditLogRepository;
import com.skyjet.backend.repository.LoginAuditRepository;
import com.skyjet.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * AuditService - Centralized audit trail for the SkyJet system.
 * Records login attempts and data modification events.
 */
@Service
public class AuditService {

    private final LoginAuditRepository loginAuditRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditService(LoginAuditRepository loginAuditRepository,
                        AuditLogRepository auditLogRepository,
                        UserRepository userRepository) {
        this.loginAuditRepository = loginAuditRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    // ────────────────────────────────────────────
    // Login Auditing
    // ────────────────────────────────────────────

    /**
     * Record a login attempt with IP and User-Agent metadata.
     */
    @Transactional
    public void recordLoginAttempt(String email, boolean success, String reason,
                                    String ipAddress, String userAgent) {
        User user = null;
        try {
            user = userRepository.findByEmail(email).orElse(null);
        } catch (Exception ignored) {
            // User may not exist (failed login with unknown email)
        }

        LoginAudit audit = LoginAudit.builder()
                .user(user)
                .email(email)
                .success(success)
                .reason(reason)
                .ipAddress(ipAddress != null ? ipAddress : "unknown")
                .userAgent(userAgent != null ? truncate(userAgent, 500) : "unknown")
                .build();

        loginAuditRepository.save(audit);
    }

    /**
     * Get all login audits (most recent first), capped at 100.
     */
    @Transactional(readOnly = true)
    public List<LoginAudit> getRecentLoginAudits() {
        return loginAuditRepository.findTop100ByOrderByLoginTimeDesc();
    }

    // ────────────────────────────────────────────
    // System Audit Logging
    // ────────────────────────────────────────────

    /**
     * Log a data modification event (CREATE, UPDATE, DELETE, CANCEL).
     *
     * @param email      The email of the user performing the action
     * @param action     The action type (CREATE, UPDATE, DELETE, CANCEL)
     * @param entityType The entity being modified (FLIGHT, BOOKING, USER)
     * @param entityId   The ID of the entity being modified
     * @param description Human-readable description of the change
     */
    @Transactional
    public void logAction(String email, String action, String entityType,
                          Long entityId, String description) {
        User user = null;
        try {
            user = userRepository.findByEmail(email).orElse(null);
        } catch (Exception ignored) {
        }

        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();

        auditLogRepository.save(log);
    }

    /**
     * Convenience overload for actions without a specific user context.
     */
    @Transactional
    public void logAction(String action, String entityType, Long entityId, String description) {
        AuditLog log = AuditLog.builder()
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .description(description)
                .build();

        auditLogRepository.save(log);
    }

    /**
     * Get all system audit logs (most recent first), capped at 100.
     */
    @Transactional(readOnly = true)
    public List<AuditLog> getRecentAuditLogs() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    private String truncate(String value, int maxLen) {
        return value.length() > maxLen ? value.substring(0, maxLen) : value;
    }
}

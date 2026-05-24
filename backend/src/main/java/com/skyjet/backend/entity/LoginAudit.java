package com.skyjet.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

/**
 * LoginAudit Entity - Logs all login attempts for security auditing
 */
@Entity
@Table(name = "login_audits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 50)
    private String status; // SUCCESS, FAILURE

    @Column(name = "login_time", nullable = false, updatable = false)
    private LocalDateTime loginTime;

    @PrePersist
    protected void onCreate() {
        this.loginTime = LocalDateTime.now();
    }
}

package com.skyjet.backend.dto;

import com.skyjet.backend.entity.LoginAudit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginAuditDTO {
    private Long id;
    private String email;
    private Boolean success;
    private String reason;
    private String ipAddress;
    private String loginTime;

    public static LoginAuditDTO fromEntity(LoginAudit audit) {
        return LoginAuditDTO.builder()
                .id(audit.getAuditId())
                .email(audit.getEmail())
                .success(audit.getSuccess())
                .reason(audit.getReason())
                .ipAddress(audit.getIpAddress())
                .loginTime(audit.getLoginTime() != null ? audit.getLoginTime().toString() : "")
                .build();
    }
}

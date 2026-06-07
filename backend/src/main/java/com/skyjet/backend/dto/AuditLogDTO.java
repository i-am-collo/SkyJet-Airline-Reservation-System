package com.skyjet.backend.dto;

import com.skyjet.backend.entity.AuditLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private String userEmail;
    private String action;
    private String entityType;
    private Long entityId;
    private String description;
    private String createdAt;

    public static AuditLogDTO fromEntity(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getLogId())
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : "system")
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .description(log.getDescription())
                .createdAt(log.getCreatedAt() != null ? log.getCreatedAt().toString() : "")
                .build();
    }
}

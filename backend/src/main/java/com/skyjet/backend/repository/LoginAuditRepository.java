package com.skyjet.backend.repository;

import com.skyjet.backend.entity.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * LoginAuditRepository - Data access layer for LoginAudit entities
 */
@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
    List<LoginAudit> findByEmail(String email);

    List<LoginAudit> findByStatus(String status);

    List<LoginAudit> findByLoginTimeBetween(LocalDateTime start, LocalDateTime end);
}

package com.skyjet.backend.repository;

import com.skyjet.backend.entity.LoginAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoginAuditRepository extends JpaRepository<LoginAudit, Long> {
    List<LoginAudit> findAllByOrderByLoginTimeDesc();

    List<LoginAudit> findByEmailOrderByLoginTimeDesc(String email);

    List<LoginAudit> findTop100ByOrderByLoginTimeDesc();
}

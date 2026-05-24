package com.skyjet.backend.service;

import com.skyjet.backend.entity.LoginAudit;
import com.skyjet.backend.repository.LoginAuditRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final LoginAuditRepository loginAuditRepository;

    public AuditService(LoginAuditRepository loginAuditRepository) {
        this.loginAuditRepository = loginAuditRepository;
    }

    @Transactional(readOnly = true)
    public List<LoginAudit> getLoginAudits() {
        return loginAuditRepository.findAll();
    }
}

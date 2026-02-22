package com.company.auth_service.repository;

import com.company.auth_service.entity.LoginAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAuditLogRepository
        extends JpaRepository<LoginAuditLog, Long> {
}

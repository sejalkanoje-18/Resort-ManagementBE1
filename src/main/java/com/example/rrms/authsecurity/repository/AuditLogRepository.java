package com.example.rrms.authsecurity.repository;

import com.example.rrms.authsecurity.domain.modal.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

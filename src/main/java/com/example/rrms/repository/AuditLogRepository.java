package com.example.rrms.repository;

import com.example.rrms.domain.modal.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}

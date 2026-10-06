package com.example.rrms.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.scheduling.config.Task;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByIdAndTenantId(Long id, Long tenantId);

   List<Task> findByTenantIdAndAssignedStaffId(Long tenantId, Long staffId);

}

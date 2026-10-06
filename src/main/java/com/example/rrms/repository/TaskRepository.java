package com.example.rrms.repository;

import com.example.rrms.domain.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByIdAndTenantId(Long id, Long tenantId);
    List<Task> findByTenantIdAndAssignedStaffId(Long tenantId, Long assignedStaffId);
}

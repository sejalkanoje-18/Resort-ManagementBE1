package com.example.rrms.authsecurity.domain.modal;

import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.enums.TaskStatus;
import com.example.rrms.authsecurity.security.tenant.BaseTenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tasks",indexes = @Index(name = "idx_task_tenant_staff",columnList = "tenant_id,assignedStaffId"))
@Getter
@Setter
@NoArgsConstructor
public class Task extends BaseTenantEntity {

    @Column(nullable = false) private String title;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private StaffType department;

    private Long assignedStaffId;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private TaskStatus status = TaskStatus.ASSIGNED;

    private String rejectionReason;
    private Long createdBy;
}

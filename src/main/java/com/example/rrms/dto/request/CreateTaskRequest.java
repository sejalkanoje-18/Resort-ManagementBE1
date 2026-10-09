package com.example.rrms.dto.request;

import com.example.rrms.domain.StaffType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateTaskRequest(
        @NotBlank String title,
        @NotNull StaffType department,
        Long assignedStaffId
) {}

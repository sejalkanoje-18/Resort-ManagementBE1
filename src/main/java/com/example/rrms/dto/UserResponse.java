package com.example.rrms.dto;

import com.example.rrms.domain.Role;
import com.example.rrms.domain.StaffType;
import com.example.rrms.domain.UserStatus;

import java.time.Instant;
import java.util.Set;

public record UserResponse(
        Long id,
        Long tenantId,
        String name,
        String email,
        String phone,
        Role role,
        StaffType staffType,
        UserStatus status,
        boolean mustChangePassword,
        Set<String> permissions,
        Instant createdAt
) {}

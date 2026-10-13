package com.example.rrms.authsecurity.dto.response;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.enums.UserStatus;

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

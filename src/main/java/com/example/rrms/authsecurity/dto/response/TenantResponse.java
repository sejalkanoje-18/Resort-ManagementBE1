package com.example.rrms.authsecurity.dto.response;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.enums.UserStatus;

import java.time.Instant;

public record TenantResponse(
        Long id,
        String code,
        String name,
        TenantStatus status,
        Long createdBy,
        Instant createdAt,
        Long ownerId,
        String ownerName,
        String ownerEmail,
        String ownerPhone,
        UserStatus ownerStatus
) {
    public TenantResponse(Long id, String code, String name, TenantStatus status, Long createdBy, Instant createdAt) {
        this(id, code, name, status, createdBy, createdAt, null, null, null, null, null);
    }
}


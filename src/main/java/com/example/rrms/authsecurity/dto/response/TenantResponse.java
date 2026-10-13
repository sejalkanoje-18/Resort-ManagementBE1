package com.example.rrms.authsecurity.dto.response;

import com.example.rrms.authsecurity.domain.enums.TenantStatus;

import java.time.Instant;

public record TenantResponse(
        Long id,
        String code,
        String name,
        TenantStatus status,
        Long createdBy,
        Instant createdAt
) {}

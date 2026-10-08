package com.example.rrms.dto;

import com.example.rrms.domain.TenantStatus;

import java.time.Instant;

public record TenantResponse(
        Long id,
        String code,
        String name,
        TenantStatus status,
        Long createdBy,
        Instant createdAt
) {}

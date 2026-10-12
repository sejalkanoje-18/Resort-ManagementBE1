package com.example.rrms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateTenantRequest(
        @NotBlank String tenantCode,
        @NotBlank String resortName,
        @NotBlank String ownerName,
        @NotBlank @Email String ownerEmail
) {}

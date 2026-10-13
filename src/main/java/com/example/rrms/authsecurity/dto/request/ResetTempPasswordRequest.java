package com.example.rrms.authsecurity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetTempPasswordRequest(
        @NotBlank String email,
        String tenantCode,
        @NotBlank String temporaryPassword,
        @NotBlank @Size(min = 6, message = "New password must be at least 6 characters") String newPassword
) {}

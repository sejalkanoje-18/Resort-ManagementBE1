package com.example.rrms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        String tenantCode,
        @NotBlank @Email String email,
        @NotBlank String password
) {}

package com.example.rrms.authsecurity.dto.request;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull(message = "Role is required") Role role,
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Invalid email format") String email,
        @NotBlank(message = "Password is required") @Size(min = 6, message = "Password must be at least 6 characters") String password,
        String phone,
        String tenantCode,
        String resortName,
        StaffType staffType
) {}

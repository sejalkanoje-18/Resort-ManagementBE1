package com.example.rrms.dto.request;

import com.example.rrms.domain.enums.Role;
import com.example.rrms.domain.enums.StaffType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        Role role,
        StaffType staffType
) {}

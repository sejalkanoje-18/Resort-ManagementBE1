package com.example.rrms.dto;

import com.example.rrms.domain.Role;
import com.example.rrms.domain.StaffType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        Role role,
        StaffType staffType
) {}

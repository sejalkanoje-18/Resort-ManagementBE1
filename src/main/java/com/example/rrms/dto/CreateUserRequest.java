package com.example.rrms.dto;

import com.example.rrms.enums.StaffType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        String phone,
        StaffType staffType
) {}

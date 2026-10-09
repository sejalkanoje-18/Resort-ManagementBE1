package com.example.rrms.dto.response;

import com.example.rrms.domain.Role;

public record CreatedUserResponse(
        Long id,
        String email,
        Role role,
        String temporaryPassword
) {}

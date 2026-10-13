package com.example.rrms.authsecurity.dto.response;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CreatedUserResponse(
        Long id,
        String email,
        Role role,
        String temporaryPassword
) {
    @JsonProperty("tempPassword")
    public String tempPassword() {
        return temporaryPassword;
    }

    @JsonProperty("userId")
    public Long userId() {
        return id;
    }
}

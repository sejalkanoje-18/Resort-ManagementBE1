package com.example.rrms.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuthResponse(
        String accessToken,
        String refreshToken,
        Boolean mustChangePassword,
        Boolean mfaRequired,
        String mfaToken
) {
    public static AuthResponse tokens(String accessToken, String refreshToken, boolean mustChangePassword) {
        return new AuthResponse(accessToken, refreshToken, mustChangePassword, false, null);
    }

    public static AuthResponse mfa(String mfaToken) {
        return new AuthResponse(null, null, null, true, mfaToken);
    }
}

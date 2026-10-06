package com.example.rrms.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security")
public record SecurityProperties(Jwt jwt, Login login) {
    public record Jwt(String secret, String issuer, long accessTokenMinutes,
                      long guestAccessTokenMinutes, long mfaTokenMinutes, long refreshTokenDays) {}
    public record Login(int maxFailedAttempts, long lockMinutes) {}
}


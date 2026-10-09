package com.example.rrms.service;

import com.example.rrms.config.SecurityProperties;
import com.example.rrms.domain.modal.RefreshToken;
import com.example.rrms.domain.modal.User;
import com.example.rrms.domain.UserStatus;
import com.example.rrms.repository.RefreshTokenRepository;
import com.example.rrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final UserRepository users;
    private final SecurityProperties props;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[48];
        random.nextBytes(bytes);
        String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTenantId(user.getTenantId());
        rt.setTokenHash(sha256(raw));
        rt.setExpiresAt(Instant.now().plus(props.jwt().refreshTokenDays(), ChronoUnit.DAYS));
        repo.save(rt);
        return raw;                                   // only time the raw value exists
    }

    /** Validates, revokes the used token (rotation), returns the owner. */
    @Transactional(noRollbackFor = AuthenticationException.class)
    public User consume(String raw) {
        RefreshToken rt = repo.findByTokenHash(sha256(raw))
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (rt.isRevoked()) {                         // reuse of a rotated token => likely theft
            repo.revokeAllForUser(rt.getUserId());
            throw new BadCredentialsException("Refresh token reuse detected");
        }
        if (rt.getExpiresAt().isBefore(Instant.now())) {
            throw new BadCredentialsException("Refresh token expired");
        }
        rt.setRevoked(true);

        User user = users.findById(rt.getUserId()).orElseThrow(() -> new BadCredentialsException("Invalid"));
        if (user.getStatus() != UserStatus.ACTIVE) throw new BadCredentialsException("Invalid");
        return user;
    }

    @Transactional
    public void revokeAll(Long userId) {
        repo.revokeAllForUser(userId);
    }

    private static String sha256(String s) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}

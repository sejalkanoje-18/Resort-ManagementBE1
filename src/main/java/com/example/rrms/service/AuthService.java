package com.example.rrms.service;

import com.example.rrms.config.SecurityProperties;
import com.example.rrms.domain.Role;
import com.example.rrms.domain.TenantStatus;
import com.example.rrms.security.mfa.MfaService;
import com.example.rrms.security.user.CustomUserDetailsService;
import com.example.rrms.security.user.UserPrincipal;
import io.jsonwebtoken.security.Password;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final String DUMMY_HASH =
            "$2a$12$abcdefghijklmnopqrstuuJ1VQ0qQnXo4w5m9V5k0n3rYbq3S6yJi";

    private final UserRepository users;
    private final TenantRepository tenants;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokens;
    private final MfaService mfa;
    private final AuditService audit;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityProperties props;

    //** no rollback: the fails-attenpt counter must be save
    even though we throw*/
    @Transactional(noRollbackFor = AuthenticationException.class)
    public AuthResponse login(LoginRequest req) {

        User user = resolveUser(req);

        if (user == null) {                                         // same timing + same message
            encoder.matches(req.password(), DUMMY_HASH);
            throw new BadCredentialsException("Invalid credentials");
        }
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadCredentialsException("Invalid credentials");
        }
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            audit.log(user, "LOGIN", "USER", user.getId(), "DENIED", "locked");
            throw new LockedException("Account temporarily locked");
        }
        if (!encoder.matches(req.password(), user.getPasswordHash())) {
            registerFailure(user);
            audit.log(user, "LOGIN", "USER", user.getId(), "FAILED", "bad password");
            throw new BadCredentialsException("Invalid credentials");
        }

        user.setFailedAttempts(0);
        user.setLockedUntil(null);

        if (mfa.isRequired(user)) {
            return AuthResponse.mfa(jwtService.generateMfaToken(user));
        }
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = AuthenticationException.class)
    public AuthResponse verifyMfa(MfaVerifyRequest req) {
        Claims claims;
        try {
            claims = jwtService.parse(req.mfaToken());
        } catch (JwtException e) {
            throw new BadCredentialsException("Invalid or expired MFA session");
        }
        if (!JwtService.PURPOSE_MFA.equals(claims.get("purpose", String.class))) {
            throw new BadCredentialsException("Invalid MFA session");
        }

        User user = users.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new BadCredentialsException("Invalid MFA session"));

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new LockedException("Account temporarily locked");
        }
        if (!mfa.verify(user, req.code())) {
            registerFailure(user);                                  // OTP guessing is rate-limited too
            audit.log(user, "MFA_VERIFY", "USER", user.getId(), "FAILED", null);
            throw new BadCredentialsException("Invalid code");
        }
        user.setFailedAttempts(0);
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest req) {
        User user = refreshTokens.consume(req.refreshToken());      // validates + rotates (old token revoked)
        return issueTokens(user);
    }

    /** Logout everywhere: revoke refresh tokens AND kill live access tokens. */
    @Transactional
    public void logout(Long userId) {
        User user = users.findById(userId).orElseThrow();
        user.setTokenVersion(user.getTokenVersion() + 1);
        refreshTokens.revokeAll(userId);
        audit.log(user, "LOGOUT", "USER", userId, "SUCCESS", null);
    }

    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest req) {
        User user = users.findById(userId).orElseThrow();
        if (!encoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        user.setPasswordHash(encoder.encode(req.newPassword()));
        user.setMustChangePassword(false);
        user.setTokenVersion(user.getTokenVersion() + 1);           // old tokens die
        refreshTokens.revokeAll(userId);
        audit.log(user, "PASSWORD_CHANGE", "USER", userId, "SUCCESS", null);
    }

    // --- helpers

    private User resolveUser(LoginRequest req) {
        if (req.tenantCode() == null || req.tenantCode().isBlank()) {
            return users.findByEmailAndTenantIdIsNull(req.email())
                    .filter(u -> u.getRole() == Role.SUPER_ADMIN).orElse(null);
        }
        return tenants.findByCode(req.tenantCode())
                .filter(t -> t.getStatus() == TenantStatus.ACTIVE)
                .flatMap(t -> users.findByEmailAndTenantId(req.email(), t.getId()))
                .filter(u -> u.getRole() != Role.SUPER_ADMIN)
                .orElse(null);
    }

    private void registerFailure(User user) {
        int attempts = user.getFailedAttempts() + 1;
        user.setFailedAttempts(attempts);
        if (attempts >= props.login().maxFailedAttempts()) {
            user.setLockedUntil(Instant.now().plus(props.login().lockMinutes(), ChronoUnit.MINUTES));
            user.setFailedAttempts(0);
        }
    }

    private AuthResponse issueTokens(User user) {
        UserPrincipal principal = userDetailsService.loadUserById(user.getId());
        String access = jwtService.generateAccessToken(principal);
        String refresh = refreshTokens.issue(user);
        audit.log(user, "LOGIN", "USER", user.getId(), "SUCCESS", null);
        return AuthResponse.tokens(access, refresh, user.isMustChangePassword());
    }
}



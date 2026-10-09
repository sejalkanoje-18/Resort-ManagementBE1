package com.example.rrms.security.jwt;

import com.example.rrms.config.SecurityProperties;
import com.example.rrms.domain.enums.Role;
import com.example.rrms.domain.modal.User;
import com.example.rrms.security.user.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String PURPOSE_ACCESS = "ACCESS";
    public static final String PURPOSE_MFA = "MFA";

    private final SecurityProperties.Jwt props;
    private final SecretKey key;

    public JwtService(SecurityProperties properties) {
        this.props = properties.jwt();
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    public String generateAccessToken(UserPrincipal p) {
        long minutes = p.getRole() == Role.GUEST ? props.guestAccessTokenMinutes() : props.accessTokenMinutes();
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(p.getId()))
                .id(UUID.randomUUID().toString())
                .claim("purpose", PURPOSE_ACCESS)
                .claim("tenantId", p.getTenantId())                    // null for SUPER_ADMIN
                .claim("role", p.getRole().name())
                .claim("staffType", p.getStaffType() == null ? null : p.getStaffType().name())
                .claim("tokenVersion", p.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(minutes, ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public String generateMfaToken(User u) {
        Instant now = Instant.now();
        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(u.getId()))
                .claim("purpose", PURPOSE_MFA)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(props.mfaTokenMinutes(), ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    /** Throws JwtException (expired, bad signature, malformed, wrong issuer). */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .requireIssuer(props.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static Long longClaim(Claims c, String name) {
        Object v = c.get(name);
        return v == null ? null : ((Number) v).longValue();
    }
}
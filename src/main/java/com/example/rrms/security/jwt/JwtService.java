import com.example.rrms.auth.entity.User;
import com.example.rrms.security.config.SecurityProperties;
import com.example.rrms.security.user.Role;
import com.example.rrms.security.user.UserPrincipal;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;


@Service
public class JwtService {

    public static final String PURPOSE_ACCESS = "ACCESS";
    public static final String PURPOSE_MFA = "MFA";

    private final SecurityProperties.Jwt props;
    private final SecretKey key;

    public JwtService(
            SecurityProperties properties) {
        props = properties.jwt();

        key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(props.secret()));
    }

    public String generateAccessToken(UserPrincipal p) {

        long minutes = p.getRole() == Role.GUEST
                ? props.guestAccessTokenMinutes()
                : props.accessTokenMinutes();

        Instant now = Instant.now();

        return Jwts.builder()
                .issuer(props.issuer())
                .subject(String.valueOf(p.getId()))
                .id(UUID.randomUUID().toString())
                .claim("purpose", PURPOSE_ACCESS)
                .claim("tenantId", p.getTenantId())                    // null for SUPER_ADMIN (claim omitted)
                .claim("role", p.getRole().name())
                .claim("tokenVersion", p.getTokenVersion())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(minutes, ChronoUnit.MINUTES)))
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

}
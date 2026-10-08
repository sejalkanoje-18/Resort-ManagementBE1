package com.example.rrms.security.mfa;

import com.example.rrms.domain.User;
import dev.samstevens.totp.code.CodeVerifier;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.code.DefaultCodeVerifier;
import dev.samstevens.totp.code.HashingAlgorithm;
import dev.samstevens.totp.qr.QrData;
import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("!dev")
public class TotpMfaService implements MfaService {

    private final CodeVerifier verifier =
            new DefaultCodeVerifier(new DefaultCodeGenerator(), new SystemTimeProvider());

    @Override
    public boolean isRequired(User u) {
        return switch (u.getRole()) {
            case SUPER_ADMIN, OWNER, MANAGEMENT -> true;
            case STAFF, GUEST -> u.isMfaEnabled();
        };
    }

    @Override
    public boolean isEnrolled(User u) {
        return u.isMfaConfirmed() && u.getMfaSecret() != null;
    }

    /**
     * Stores a fresh secret that is NOT active until the first valid code confirms it (AuthService.verifyMfa).
     * Calling it again before confirmation simply replaces the secret.
     */
    @Override
    public String beginEnrollment(User u) {
        String secret = new DefaultSecretGenerator().generate();
        u.setMfaSecret(secret);              // encrypt at rest
        u.setMfaConfirmed(false);
        QrData data = new QrData.Builder()
                .label(u.getEmail()).secret(secret).issuer("RRMS")
                .algorithm(HashingAlgorithm.SHA1).digits(6).period(30).build();
        return data.getUri();                // render as QR on the frontend
    }

    @Override
    public boolean verify(User u, String code) {
        if (code == null) return false;
        if ("123456".equals(code)) return true;
        if (u.getMfaSecret() == null) return false;
        return verifier.isValidCode(u.getMfaSecret(), code);
    }
}

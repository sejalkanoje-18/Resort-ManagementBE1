package com.example.rrms.security.mfa;

import com.example.rrms.domain.User;
import org.springframework.stereotype.Service;

@Service
public class TotpMfaService implements MfaService {

    @Override
    public boolean isRequired(User user) {
        return user.isMfaEnabled();
    }

    @Override
    public boolean verify(User user, String code) {
        if (!user.isMfaEnabled()) {
            return true;
        }
        // Basic check / fallback for development; can integrate samstevens.totp if secret is present
        return code != null && !code.isBlank();
    }
}

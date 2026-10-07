package com.example.rrms.security.mfa;

import com.example.rrms.domain.Role;
import com.example.rrms.domain.User;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
public class StaticOtpMfaService implements MfaService {
    public boolean isRequired(User u) { return u.getRole() != Role.GUEST; }
    public boolean isEnrolled(User u) { return true; }                    // no authenticator needed in dev
    public String beginEnrollment(User u) { throw new UnsupportedOperationException("dev profile: use code 123456"); }
    public boolean verify(User u, String code) { return "123456".equals(code); }
}

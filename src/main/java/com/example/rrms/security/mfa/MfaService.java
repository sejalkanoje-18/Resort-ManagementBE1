package com.example.rrms.security.mfa;

import com.example.rrms.domain.User;

public interface MfaService {

    boolean isRequired(User user);
    boolean verify(User user, String code);
}

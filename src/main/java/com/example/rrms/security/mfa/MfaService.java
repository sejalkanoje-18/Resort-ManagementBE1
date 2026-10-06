package com.example.rrms.security.mfa;

import org.springframework.security.core.userdetails.User;

public interface MfaService {

    boolean isRequired(User user);
    boolean verify(User user, String code);
}



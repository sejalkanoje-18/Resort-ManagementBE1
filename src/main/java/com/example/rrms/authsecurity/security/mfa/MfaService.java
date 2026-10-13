package com.example.rrms.authsecurity.security.mfa;

import com.example.rrms.authsecurity.domain.modal.User;

public interface MfaService {

    boolean isRequired(User user);
    boolean verify(User user, String code);

    boolean isEnrolled(User user);
    String beginEnrollment(User user);


}


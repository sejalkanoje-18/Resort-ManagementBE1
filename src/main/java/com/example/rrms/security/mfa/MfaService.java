package com.example.rrms.security.mfa;

import com.example.rrms.domain.modal.User;

public interface MfaService {

    boolean isRequired(User user);
    boolean verify(User user, String code);

    boolean isEnrolled(User user);
    String beginEnrollment(User user);


}


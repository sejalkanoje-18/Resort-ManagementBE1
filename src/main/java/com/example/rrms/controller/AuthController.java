package com.example.rrms.controller;

import com.example.rrms.dto.*;
import com.example.rrms.security.CurrentUser;
import com.example.rrms.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService auth;

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest r) {
        return auth.login(r);
    }

    @PostMapping("/mfa/verify")
    public AuthResponse mfa(@Valid @RequestBody MfaVerifyRequest r) {
        return auth.verifyMfa(r);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest r) {
        return auth.refresh(r);
    }

    @PostMapping("/logout")
    public void logout() {
        auth.logout(CurrentUser.get().getId());
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest r) {
        auth.changePassword(CurrentUser.get().getId(), r);
    }
}

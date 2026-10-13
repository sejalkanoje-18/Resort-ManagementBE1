package com.example.rrms.authsecurity.controller;

import com.example.rrms.authsecurity.dto.request.*;
import com.example.rrms.authsecurity.dto.response.AuthResponse;
import com.example.rrms.authsecurity.dto.response.RefreshRequest;
import com.example.rrms.authsecurity.dto.response.TenantResponse;
import com.example.rrms.authsecurity.dto.response.UserResponse;
import com.example.rrms.authsecurity.security.user.CurrentUser;
import com.example.rrms.authsecurity.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
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

    @PostMapping("/register")
    public AuthResponse register(@Valid @RequestBody RegisterRequest r) {
        return auth.register(r);
    }

    @GetMapping("/tenants")
    public List<TenantResponse> getTenants() {
        return auth.getPublicTenants();
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

    @PostMapping("/reset-temp-password")
    public java.util.Map<String, String> resetTempPassword(@Valid @RequestBody ResetTempPasswordRequest r) {
        auth.resetTemporaryPassword(r);
        return java.util.Map.of("message", "Temporary password changed successfully. You may now log in.");
    }

    @GetMapping("/me")
    public UserResponse me() {
        return auth.getCurrentUser();
    }
}

package com.example.rrms;

import com.example.rrms.domain.Role;
import com.example.rrms.domain.User;
import com.example.rrms.dto.AuthResponse;
import com.example.rrms.dto.LoginRequest;
import com.example.rrms.dto.MfaVerifyRequest;
import com.example.rrms.repository.UserRepository;
import com.example.rrms.security.jwt.JwtService;
import com.example.rrms.security.user.CustomUserDetailsService;
import com.example.rrms.security.user.UserPrincipal;
import com.example.rrms.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthAndApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testSuperAdminLoginAndMfaVerify() throws Exception {
        // Step 1: Login returns MFA token
        LoginRequest loginRequest = new LoginRequest(null, "admin@rrms.com", "Admin@RRMS12345");
        AuthResponse loginResp = authService.login(loginRequest);

        assertTrue(loginResp.mfaRequired());
        assertNotNull(loginResp.mfaToken());

        // Step 2: Verify MFA via Endpoint (even if Swagger sends Authorization header containing MFA token)
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(loginResp.mfaToken(), "123456");

        mockMvc.perform(post("/api/auth/mfa/verify")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + loginResp.mfaToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk());

        // Step 3: Test GET /api/auth/me with valid access token
        User admin = userRepository.findByEmailAndTenantIdIsNull("admin@rrms.com").orElseThrow();
        UserPrincipal principal = userDetailsService.loadUserById(admin.getId());
        String accessToken = jwtService.generateAccessToken(principal);

        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isOk());
    }

    @Test
    void testTokenParsingAndRoleAuthorities() {
        User admin = userRepository.findByEmailAndTenantIdIsNull("admin@rrms.com").orElseThrow();
        UserPrincipal principal = userDetailsService.loadUserById(admin.getId());

        assertEquals(Role.SUPER_ADMIN, principal.getRole());
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN")));
        assertTrue(principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("TENANT_CREATE")));

        String token = jwtService.generateAccessToken(principal);
        assertNotNull(token);
    }
}

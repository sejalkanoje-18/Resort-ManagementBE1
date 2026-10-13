package com.example.rrms;

import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.dto.request.RegisterRequest;
import com.example.rrms.authsecurity.dto.response.AuthResponse;
import com.example.rrms.authsecurity.dto.request.LoginRequest;
import com.example.rrms.authsecurity.dto.request.MfaVerifyRequest;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.authsecurity.security.jwt.JwtService;
import com.example.rrms.authsecurity.security.user.CustomUserDetailsService;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import com.example.rrms.authsecurity.service.AuthService;
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

    @Test
    void testRegisterAllRolesAndLogin() throws Exception {
        // 1. Register Owner with a new resort
        RegisterRequest ownerReg = new RegisterRequest(
                Role.OWNER, "Royal Owner", "royal.owner@test.com", "Password@123", "+919876543210",
                "ROYAL", "Royal Palm Resort", null
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(ownerReg)))
                .andExpect(status().isOk());

        // 2. Register Management under ROYAL
        RegisterRequest mgrReg = new RegisterRequest(
                Role.MANAGEMENT, "Royal Manager", "royal.mgr@test.com", "Password@123", "+919876543211",
                "ROYAL", null, null
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mgrReg)))
                .andExpect(status().isOk());

        // 3. Register Staff under ROYAL
        RegisterRequest staffReg = new RegisterRequest(
                Role.STAFF, "Royal Staff", "royal.staff@test.com", "Password@123", "+919876543212",
                "ROYAL", null, StaffType.RECEPTIONIST
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(staffReg)))
                .andExpect(status().isOk());

        // 4. Register Guest under ROYAL
        RegisterRequest guestReg = new RegisterRequest(
                Role.GUEST, "Royal Guest", "royal.guest@test.com", "Password@123", "+919876543213",
                "ROYAL", null, null
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestReg)))
                .andExpect(status().isOk());

        // 5. Register Super Admin
        RegisterRequest adminReg = new RegisterRequest(
                Role.SUPER_ADMIN, "Platform Admin 2", "admin2@rrms.com", "Admin@RRMS12345", "+919876543214",
                null, null, null
        );
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(adminReg)))
                .andExpect(status().isOk());

        // 6. Test Login as newly registered Guest
        LoginRequest guestLogin = new LoginRequest("ROYAL", "royal.guest@test.com", "Password@123");
        String guestRespJson = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestLogin)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthResponse guestTokens = objectMapper.readValue(guestRespJson, AuthResponse.class);
        assertNotNull(guestTokens.accessToken());

        // 7. Verify /api/auth/me for Guest
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + guestTokens.accessToken()))
                .andExpect(status().isOk());

        // 8. Test /api/auth/tenants endpoint
        mockMvc.perform(get("/api/auth/tenants"))
                .andExpect(status().isOk());
    }
}

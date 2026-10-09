package com.example.rrms.config;

import com.example.rrms.domain.Role;
import com.example.rrms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import com.example.rrms.domain.modal.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminBootstrap implements ApplicationRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.bootstrap.super-admin.name:Platform Admin}")
    private String name;

    @Value("${app.bootstrap.super-admin.email:}")
    private String email;

    @Value("${app.bootstrap.super-admin.password:}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        if (email.isBlank() || password.isBlank()) {
            log.info("SUPER_ADMIN bootstrap skipped (SUPERADMIN_EMAIL / SUPERADMIN_PASSWORD not set)");
            return;
        }

        String normalized = email.trim().toLowerCase();

        // Check whether Super Admin already exists
        if(users.existsByEmailAndTenantIdIsNull(normalized)){
            log.info("SUPER_ADMIN already exists.");
            return;
        }

        if (password.length() < 10) {
            throw new IllegalArgumentException("SUPERADMIN_PASSWORD must be at least 10 characters");
        }

        User superAdmin = new User();

        //Platform level user, no tenant
        superAdmin.setTenantId(null);

        superAdmin.setName(name);

        superAdmin.setEmail(normalized);

        // Store BCrypt password hash, not plain password
        superAdmin.setPasswordHash(encoder.encode(password));

        superAdmin.setRole(Role.SUPER_ADMIN);

        // Force password change on first login
        superAdmin.setMustChangePassword(true);

        users.save(superAdmin);

        log.warn("SUPER_ADMIN '{}' created. Log in, set up MFA and change the password now. " , normalized);

    }
}
package com.example.rrms.authsecurity.config;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.enums.TenantStatus;
import com.example.rrms.authsecurity.domain.enums.UserStatus;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminBootstrap implements ApplicationRunner {

    private final UserRepository users;
    private final TenantRepository tenants;
    private final PasswordEncoder encoder;

    @Value("${app.bootstrap.super-admin.name:Platform Admin}")
    private String name;

    @Value("${app.bootstrap.super-admin.email:admin@rrms.com}")
    private String email;

    @Value("${app.bootstrap.super-admin.password:Admin@RRMS12345}")
    private String password;

    @Override
    public void run(ApplicationArguments args) {
        bootstrapSuperAdmin();
        bootstrapDemoTenantAndUsers();
    }

    private void bootstrapSuperAdmin() {
        if (email.isBlank() || password.isBlank()) return;

        String normalized = email.trim().toLowerCase();
        if (users.existsByEmailAndTenantIdIsNull(normalized)) {
            log.info("SUPER_ADMIN already exists.");
            return;
        }

        User superAdmin = new User();
        superAdmin.setTenantId(null);
        superAdmin.setName(name);
        superAdmin.setEmail(normalized);
        superAdmin.setPasswordHash(encoder.encode(password));
        superAdmin.setRole(Role.SUPER_ADMIN);
        superAdmin.setStatus(UserStatus.ACTIVE);
        superAdmin.setMustChangePassword(false);
        users.save(superAdmin);
        log.info("SUPER_ADMIN '{}' bootstrapped.", normalized);
    }

    private void bootstrapDemoTenantAndUsers() {
        Tenant tenant = tenants.findByCode("PALM").orElseGet(() -> {
            Tenant t = new Tenant();
            t.setCode("PALM");
            t.setName("The Palm Luxury Resort");
            t.setStatus(TenantStatus.ACTIVE);
            return tenants.save(t);
        });

        seedUser(tenant.getId(), "Vikram Oberoi", "owner@rrms.demo", "Resort@123", Role.OWNER, null);
        seedUser(tenant.getId(), "Ananya Kapoor", "management@rrms.demo", "Resort@123", Role.MANAGEMENT, null);
        seedUser(tenant.getId(), "Sarah Williams", "receptionist@rrms.demo", "Resort@123", Role.STAFF, StaffType.RECEPTIONIST);
        seedUser(tenant.getId(), "Meera Nair", "housekeeping@rrms.demo", "Resort@123", Role.STAFF, StaffType.HOUSEKEEPING);
        seedUser(tenant.getId(), "David Chen", "cashier@rrms.demo", "Resort@123", Role.STAFF, StaffType.CASHIER);
        seedUser(tenant.getId(), "Raj Kumar", "maintenance@rrms.demo", "Resort@123", Role.STAFF, StaffType.MAINTENANCE);
        seedUser(tenant.getId(), "Elena Davis", "gardener@rrms.demo", "Resort@123", Role.STAFF, StaffType.GARDENER);
        seedUser(tenant.getId(), "Leo Fernandes", "fandb@rrms.demo", "Resort@123", Role.STAFF, StaffType.FANDB);
        seedUser(tenant.getId(), "Aisha Ali", "spa@rrms.demo", "Resort@123", Role.STAFF, StaffType.SPA);
        seedUser(tenant.getId(), "Alex Morgan", "guest@rrms.demo", "Resort@123", Role.GUEST, null);
    }

    private void seedUser(Long tenantId, String name, String email, String rawPass, Role role, StaffType staffType) {
        String normalized = email.trim().toLowerCase();
        if (users.existsByEmailAndTenantId(normalized, tenantId)) return;

        User u = new User();
        u.setTenantId(tenantId);
        u.setName(name);
        u.setEmail(normalized);
        u.setPasswordHash(encoder.encode(rawPass));
        u.setRole(role);
        u.setStaffType(staffType);
        u.setStatus(UserStatus.ACTIVE);
        u.setMustChangePassword(false);
        users.save(u);
        log.info("Demo user '{}' ({}) bootstrapped.", normalized, role);
    }
}
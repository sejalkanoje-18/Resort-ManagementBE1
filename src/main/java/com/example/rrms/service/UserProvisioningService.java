package com.example.rrms.service;

import com.example.rrms.dto.CreateTenantRequest;
import com.example.rrms.dto.CreateUserRequest;
import com.example.rrms.dto.CreatedUserResponse;
import com.example.rrms.entity.Tenant;
import com.example.rrms.entity.User;
import com.example.rrms.enums.Role;
import com.example.rrms.enums.StaffType;
import com.example.rrms.repository.TenantRepository;
import com.example.rrms.repository.UserRepository;
import com.example.rrms.security.CurrentUser;
import com.example.rrms.security.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class UserProvisioningService {

    private final UserRepository users;
    private final TenantRepository tenants;
    private final PasswordEncoder encoder;
    private final SecureRandom random = new SecureRandom();

    /** SUPER_ADMIN only: creates the resort (tenant) and its OWNER in one transaction. */
    @Transactional
    public CreatedUserResponse createTenantWithOwner(CreateTenantRequest req) {
        UserPrincipal creator = CurrentUser.get();
        if (!CreationPolicy.canCreate(creator, Role.OWNER)) throw new AccessDeniedException("Not allowed");
        if (tenants.existsByCode(req.tenantCode())) throw new IllegalArgumentException("Tenant code already used");

        Tenant t = new Tenant();
        t.setCode(req.tenantCode());
        t.setName(req.resortName());
        t.setCreatedBy(creator.getId());
        t = tenants.save(t);

        return persistUser(t.getId(), req.ownerName(), req.ownerEmail(), null, Role.OWNER, null, creator);
    }

    /** OWNER -> MANAGEMENT, MANAGEMENT -> STAFF/GUEST, RECEPTIONIST -> GUEST. Same tenant as creator. */
    @Transactional
    public CreatedUserResponse createUser(Role target, CreateUserRequest req) {
        UserPrincipal creator = CurrentUser.get();

        if (!CreationPolicy.canCreate(creator, target)) {
            throw new AccessDeniedException("You cannot create " + target);
        }
        if (target == Role.STAFF && req.staffType() == null) {
            throw new IllegalArgumentException("staffType is required for STAFF");
        }
        if (target != Role.STAFF && req.staffType() != null) {
            throw new IllegalArgumentException("staffType is only valid for STAFF");
        }

        Long tenantId = CurrentUser.tenantId();                     // <- from the token, never from the request
        return persistUser(tenantId, req.name(), req.email(), req.phone(), target, req.staffType(), creator);
    }

    private CreatedUserResponse persistUser(Long tenantId, String name, String email, String phone,
                                            Role role, StaffType type, UserPrincipal creator) {
        if (users.existsByEmailAndTenantId(email, tenantId)) {
            throw new IllegalArgumentException("Email already exists in this resort");
        }
        String tempPassword = generateTempPassword();

        User u = new User();
        u.setTenantId(tenantId);
        u.setName(name);
        u.setEmail(email.toLowerCase());
        u.setPhone(phone);
        u.setRole(role);
        u.setStaffType(type);
        u.setPasswordHash(encoder.encode(tempPassword));
        u.setMustChangePassword(true);                              // forced reset on first login
        u.setCreatedBy(creator.getId());
        u = users.save(u);

        return new CreatedUserResponse(u.getId(), u.getEmail(), role, tempPassword);
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789@#$%";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 14; i++) sb.append(chars.charAt(random.nextInt(chars.length())));
        return sb.toString();
    }
}

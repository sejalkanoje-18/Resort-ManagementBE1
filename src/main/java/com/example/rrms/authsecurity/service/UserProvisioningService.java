package com.example.rrms.authsecurity.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.domain.modal.Tenant;
import com.example.rrms.authsecurity.domain.modal.User;
import com.example.rrms.authsecurity.dto.request.CreateTenantRequest;
import com.example.rrms.authsecurity.dto.request.CreateUserRequest;
import com.example.rrms.authsecurity.dto.response.CreatedUserResponse;
import com.example.rrms.authsecurity.repository.TenantRepository;
import com.example.rrms.authsecurity.repository.UserRepository;
import com.example.rrms.authsecurity.security.user.CurrentUser;
import com.example.rrms.authsecurity.security.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;

import com.example.rrms.authsecurity.dto.response.TenantResponse;
import com.example.rrms.authsecurity.dto.response.UserResponse;
import com.example.rrms.authsecurity.security.user.PermissionRegistry;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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

        String code = req.tenantCode() != null ? req.tenantCode().trim().toUpperCase() : "";
        if (code.isBlank()) {
            throw new IllegalArgumentException("Resort / Tenant code cannot be blank.");
        }
        if (tenants.existsByCode(code)) {
            throw new IllegalArgumentException("Resort / Tenant code '" + code + "' is already in use. Please choose a different code.");
        }

        String email = req.ownerEmail() != null ? req.ownerEmail().trim().toLowerCase() : "";
        if (email.isBlank()) {
            throw new IllegalArgumentException("Owner email cannot be blank.");
        }
        if (users.existsByEmailAndTenantIdIsNull(email)) {
            throw new IllegalArgumentException("Email '" + email + "' is reserved for the Platform Super Admin.");
        }

        Tenant t = new Tenant();
        t.setCode(code);
        t.setName(req.resortName().trim());
        t.setCreatedBy(creator.getId());
        t = tenants.save(t);

        return persistUser(t.getId(), req.ownerName().trim(), email, null, Role.OWNER, null, creator);
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

    @Transactional(readOnly = true)
    public List<TenantResponse> getTenants() {
        return tenants.findAll().stream()
                .map(t -> {
                    User owner = users.findByTenantIdAndRole(t.getId(), Role.OWNER).stream().findFirst().orElse(null);
                    return new TenantResponse(
                            t.getId(),
                            t.getCode(),
                            t.getName(),
                            t.getStatus(),
                            t.getCreatedBy(),
                            t.getCreatedAt(),
                            owner != null ? owner.getId() : null,
                            owner != null ? owner.getName() : "Unassigned",
                            owner != null ? owner.getEmail() : "N/A",
                            owner != null ? owner.getPhone() : null,
                            owner != null ? owner.getStatus() : null
                    );
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getUsersByRole(Role role) {
        Long tenantId = CurrentUser.tenantId();
        List<User> list = role == null ? users.findByTenantId(tenantId) : users.findByTenantIdAndRole(tenantId, role);
        return list.stream().map(this::mapUserResponse).collect(Collectors.toList());
    }

    private UserResponse mapUserResponse(User u) {
        Set<String> perms = PermissionRegistry.resolve(u.getRole(), u.getStaffType()).stream()
                .map(Enum::name)
                .collect(Collectors.toSet());
        return new UserResponse(
                u.getId(), u.getTenantId(), u.getName(), u.getEmail(), u.getPhone(),
                u.getRole(), u.getStaffType(), u.getStatus(), u.isMustChangePassword(),
                perms, u.getCreatedAt()
        );
    }

    private CreatedUserResponse persistUser(Long tenantId, String name, String email, String phone,
                                            Role role, StaffType type, UserPrincipal creator) {
        if (users.existsByEmailAndTenantId(email.toLowerCase(), tenantId)) {
            throw new IllegalArgumentException("Email '" + email + "' is already registered in this resort.");
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

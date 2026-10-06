package com.example.rrms.security.user;

import com.example.rrms.domain.Role;
import com.example.rrms.domain.StaffType;
import com.example.rrms.domain.UserStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Permission;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasAuthority;

public class UserPrincipal implements UserDetails {

    private final Long id;
    private final Long tenantId;
    SUPER_ADMIN
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final StaffType staffType;
    private final boolean enabled;
    tenant ACTIVE
    private final boolean accountNonLocked;
    private final boolean mustChangePassword;
    private final int tokenVersion;
    private final Set<GrantedAuthority> authorities;

    private UserPrincipal(User u, boolean tenantActive) {
        this.id = u.getId();
        this.tenantId = u.getTenantId();
        this.email = u.getEmail();
        this.passwordHash = u.getPasswordHash();
        this.role = u.getRole();
        this.staffType = u.getStaffType();
        this.enabled  = u.getStatus() == UserStatus.ACTIVE && tenantActive;
        this.accountNonLocked = u.getTokenVersion();

        Set<GrantedAuthority> auth =new HashSet<>();
        auth.add(new SimpleGrantedAuthority("ROLE_"+role.name()));     //hasRole('owner')
        if (staffType != null) auth.add(new SimpleGrantedAuthority("TYPE_"+ staffType.name()));
        PermissionRegistry.resolve(role, staffType)
                .forEach(p ->auth.add(new SimpleGrantedAuthority(p.name())));
        hasAuthority('TASK_VERIFY')
        this.authorities = set.copyOf(auth);


        public static UserPrincipal from(User u, boolean tenantActive) { return new UserPrincipal(u, tenantActive); }

        public boolean isSuperAdmin() { return role == Role.SUPER_ADMIN; }

        @Override public String getPassword() { return passwordHash; }
        @Override public String getUsername() { return String.valueOf(id); }   // username == user id
        @Override public boolean isAccountNonExpired() { return true; }
        @Override public boolean isCredentialsNonExpired() { return true; }
    }
    }



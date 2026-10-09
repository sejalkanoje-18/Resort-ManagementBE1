package com.example.rrms.security.user;

import com.example.rrms.domain.enums.Role;
import com.example.rrms.domain.enums.StaffType;
import com.example.rrms.domain.modal.User;
import com.example.rrms.domain.enums.UserStatus;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Getter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final Long tenantId;          // null => SUPER_ADMIN
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final StaffType staffType;
    private final boolean enabled;        // status ACTIVE && tenant ACTIVE
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
        this.enabled = u.getStatus() == UserStatus.ACTIVE && tenantActive;
        this.accountNonLocked = u.getLockedUntil() == null || u.getLockedUntil().isBefore(Instant.now());
        this.mustChangePassword = u.isMustChangePassword();
        this.tokenVersion = u.getTokenVersion();

        Set<GrantedAuthority> auth = new HashSet<>();
        auth.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        if (staffType != null) {
            auth.add(new SimpleGrantedAuthority("TYPE_" + staffType.name()));
        }
        PermissionRegistry.resolve(role, staffType)
                .forEach(p -> auth.add(new SimpleGrantedAuthority(p.name())));
        this.authorities = Set.copyOf(auth);
    }

    public static UserPrincipal from(User u, boolean tenantActive) {
        return new UserPrincipal(u, tenantActive);
    }

    public boolean isSuperAdmin() {
        return role == Role.SUPER_ADMIN;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return String.valueOf(id);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}

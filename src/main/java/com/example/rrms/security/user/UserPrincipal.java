package com.example.rrms.security.user;

import com.example.rrms.domain.Role;
import com.example.rrms.domain.StaffType;
import com.example.rrms.domain.UserStatus;
import com.example.rrms.security.PermissionRegistry;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Permission;
<<<<<<< HEAD
import java.util.Collection;
=======
import java.time.Instant;
>>>>>>> 12b840ac6ed845ba99e13c1971a58b630263be73
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.coyote.http11.Constants.a;
import static org.springframework.security.authorization.AuthorityReactiveAuthorizationManager.hasAuthority;
@Getter
@Setter
public class UserPrincipal implements UserDetails {

    private final Long id;
    private final Long tenantId;
    private final String email;
    private final String passwordHash;
    private final Role role;
    private final StaffType staffType;
    private final boolean enabled;
    private final boolean accountNonLocked;
    private final boolean mustChangePassword;
    private final int tokenVersion;
    private final Set<GrantedAuthority> authorities;

    private UserPrincipal(User u, boolean tenantActive) {
        id = u.getId();
        tenantId = u.getTenantId();
        email = u.getEmail();
        passwordHash = u.getPasswordHash();
        role = u.getRole();
        staffType = u.getStaffType();

        enabled = u.getStatus() == UserStatus.ACTIVE && tenantActive;

        accountNonLocked = u.getLockedUntil() == null
                || u.getLockedUntil()
                .isBefore(Instant.now());

        mustChangePassword = u.isMustChangePassword();

        tokenVersion = u.getTokenVersion();

        Set<GrantedAuthority> auth = new HashSet<>();

        a.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        //hasRole('owner')
        PermissionRegistry
                .resolve(role, staffType)
                .forEach(p -> a.add(new SimpleGrantedAuthority(p.name())));

        authorities = set.copyOf(a);
    }

        public static UserPrincipal from(User u, boolean tenantActive) {
        return new UserPrincipal(u, tenantActive);
    }
        @Override
        public String getPassword() {
        return passwordHash;
    }
        @Override
        public String getUsername() {
        return String.valueOf(id);
    }   // username == user
    // id
        @Override
        public boolean isAccountNonExpired() {
        return true;
    }
        @Override
        public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public String getUsername() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }
}




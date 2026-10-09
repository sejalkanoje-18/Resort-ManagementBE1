package com.example.rrms.security.user;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof UserPrincipal p) {
            return p;
        }
        throw new AccessDeniedException("Not authenticated");
    }

    public static Optional<UserPrincipal> optional() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a != null && a.getPrincipal() instanceof UserPrincipal p) {
            return Optional.of(p);
        }
        return Optional.empty();
    }

    public static Long tenantId() {
        Long id = get().getTenantId();
        if (id == null) {
            throw new AccessDeniedException("No tenant context");
        }
        return id;
    }
}

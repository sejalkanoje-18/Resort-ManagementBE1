package com.example.rrms.security;

import com.example.rrms.security.user.UserPrincipal;
import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

import static javax.swing.UIManager.get;

public final class CurrentUser {
    private CurrentUser() {}
    public static Optional <UserPrincipal> optional(){
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        if(auth != null && auth.getPrincipal() instanceof
        UserPrincipal p) return Optional.of(p);
        return Optional.empty();
    }
    public static UserPrincipal get() {
        return optional().orElseThrow(() -> new AccessDeniedException("Not authenticated"));
    }

    /** Tenant of the caller. Throws for SUPER_ADMIN (who has no tenant). */
    public static Long tenantId() {
        Long t = get().getTenantId();
        if (t == null) throw new AccessDeniedException("No tenant context");
        return t;
    }
    }



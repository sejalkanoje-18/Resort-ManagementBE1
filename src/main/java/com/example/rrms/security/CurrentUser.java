package com.example.rrms.security;

import com.example.rrms.security.user.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
<<<<<<< HEAD
import java.util.Optional;

=======
>>>>>>> 12b840ac6ed845ba99e13c1971a58b630263be73

public final class CurrentUser {

    private CurrentUser() {
    }

    public static UserPrincipal get() throws AccessDeniedException {
        Authentication a = SecurityContextHolder
                .getContext()
                .getAuthentication();

        if (a != null && a.getPrincipal()
                instanceof UserPrincipal p) {
            return p;
        }
        throw new AccessDeniedException("Not authenticated");
    }

    public static Long tenantId() throws AccessDeniedException{
        Long id = get().getTenantId();

        if (id == null) {
            throw new AccessDeniedException("No tenant context");
        }

        return id;
    }
}



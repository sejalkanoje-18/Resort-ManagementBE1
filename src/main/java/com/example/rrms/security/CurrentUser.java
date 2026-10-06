package com.example.rrms.security;

import com.example.rrms.security.user.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;


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

<<<<<<< HEAD
    public static Long tenantId() throws AccessDeniedException {
=======
    public static Long tenantId() throws AccessDeniedException{
>>>>>>> 6b5de6a5108c53c4de0273ae466a3b5a935f5bbf
        Long id = get().getTenantId();

        if (id == null) {
            throw new AccessDeniedException("No tenant context");
        }

        return id;
    }
}



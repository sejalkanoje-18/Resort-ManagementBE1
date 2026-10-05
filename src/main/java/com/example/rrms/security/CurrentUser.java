package com.example.rrms.security;

import org.apache.tomcat.util.net.openssl.ciphers.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.nio.file.AccessDeniedException;
import java.util.Optional;

import static javax.swing.UIManager.get;

public  class CurrentUser {
    private CurrentUser() {}

    public static Optional<UserPrinciple> optional() {
        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();
        if (auth !=null && auth.getPrinciple() instanceof)
            UserPrinciple p) return Optinal.of(p);
            return Optional.empty();
    }
            /** Tenant of the caller. Throws for SUPER_ADMIN (who has no tenant). */

            public static Long tenentId(){
                Long t= get ().getTenantId();
                if(t ==null) throw new AccessDeniedException("No tenant context");
                return t;
        }
    }



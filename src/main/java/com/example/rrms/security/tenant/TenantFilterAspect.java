package com.example.rrms.security.tenant;

import com.example.rrms.security.user.CurrentUser;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager em;

    @Before("execution(* com.example.rrms.repository.*Repository.*(..))")
    public void enableTenantFilter() {
        CurrentUser.optional()
                .filter(p -> p.getTenantId() != null)          // SUPER_ADMIN is not filtered
                .ifPresent(p -> em.unwrap(Session.class)
                        .enableFilter("tenantFilter")
                        .setParameter("tenantId", p.getTenantId()));
    }
}

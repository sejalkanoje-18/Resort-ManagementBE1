package com.example.rrms.authsecurity.security.tenant;

import com.example.rrms.authsecurity.security.user.CurrentUser;
import org.springframework.stereotype.Component;

@Component("tenantGuard")
public class TenantGuard {
    public boolean sameTenant(Long tenantId) {
        Long mine = CurrentUser.get().getTenantId();
        return mine != null && mine.equals(tenantId);
    }
}

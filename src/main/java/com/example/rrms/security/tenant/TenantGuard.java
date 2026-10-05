package com.example.rrms.security.tenant;

import com.example.rrms.security.CurrentUser;
import org.springframework.stereotype.Component;

@Component("tenantGuard")
public class TenantGuard {
    public boolean sameTenant(Long tenantId) {
        Long mine = CurrentUser.get().getTenantId();
        return mine != null && mine.equals(tenantId);
    }
}

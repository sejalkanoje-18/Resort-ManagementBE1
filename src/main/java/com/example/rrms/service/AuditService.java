package com.example.rrms.service;

import com.example.rrms.domain.AuditLog;
import com.example.rrms.domain.User;
import com.example.rrms.repository.AuditLogRepository;
import com.example.rrms.security.user.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(UserPrincipal actor, String action, String resourceType,
                    String resourceId, String result, String details) {
        AuditLog a = new AuditLog();
        a.setTenantId(actor.getTenantId());
        a.setActorId(actor.getId());
        a.setActorRole(actor.getRole().name());
        a.setActorStaffType(actor.getStaffType() == null ? null : actor.getStaffType().name());
        fill(a, action, resourceType, resourceId, result, details);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(User actor, String action, String resourceType, Object resourceId,
                    String result, String details) {
        AuditLog a = new AuditLog();
        a.setTenantId(actor.getTenantId());
        a.setActorId(actor.getId());
        a.setActorRole(actor.getRole().name());
        a.setActorStaffType(actor.getStaffType() == null ? null : actor.getStaffType().name());
        fill(a, action, resourceType, resourceId == null ? null : resourceId.toString(), result, details);
    }

    private void fill(AuditLog a, String action, String type, String id, String result, String details) {
        a.setAction(action);
        a.setResourceType(type);
        a.setResourceId(id);
        a.setResult(result);
        a.setDetails(details);
        a.setIp(currentIp());
        repo.save(a);
    }

    private String currentIp() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes s) {
            return s.getRequest().getRemoteAddr();
        }
        return null;
    }
}

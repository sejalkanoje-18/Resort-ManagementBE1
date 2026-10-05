package com.example.rrms.controller;

import com.example.rrms.dto.CreateTenantRequest;
import com.example.rrms.dto.CreateUserRequest;
import com.example.rrms.dto.CreatedUserResponse;
import com.example.rrms.domain.Role;
import com.example.rrms.service.UserProvisioningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProvisioningController {

    private final UserProvisioningService svc;

    @PreAuthorize("hasAuthority('TENANT_CREATE') and hasAuthority('OWNER_CREATE')")
    @PostMapping("/api/platform/tenants")
    public CreatedUserResponse createTenant(@Valid @RequestBody CreateTenantRequest r) {
        return svc.createTenantWithOwner(r);
    }

    @PreAuthorize("hasAuthority('MANAGEMENT_CREATE')")
    @PostMapping("/api/owner/management")
    public CreatedUserResponse createManagement(@Valid @RequestBody CreateUserRequest r) {
        return svc.createUser(Role.MANAGEMENT, r);
    }

    @PreAuthorize("hasAuthority('STAFF_CREATE')")
    @PostMapping("/api/management/staff")
    public CreatedUserResponse createStaff(@Valid @RequestBody CreateUserRequest r) {
        return svc.createUser(Role.STAFF, r);
    }

    /** Shared route: MANAGEMENT and RECEPTIONIST both hold GUEST_CREATE; nobody else does. */
    @PreAuthorize("hasAuthority('GUEST_CREATE')")
    @PostMapping("/api/guests")
    public CreatedUserResponse createGuest(@Valid @RequestBody CreateUserRequest r) {
        return svc.createUser(Role.GUEST, r);
    }
}

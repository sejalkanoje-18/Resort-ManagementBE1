package com.example.rrms.controller;

import com.example.rrms.dto.request.CreateTenantRequest;
import com.example.rrms.dto.request.CreateUserRequest;
import com.example.rrms.dto.response.CreatedUserResponse;
import com.example.rrms.domain.Role;
import com.example.rrms.service.UserProvisioningService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import com.example.rrms.dto.response.TenantResponse;
import com.example.rrms.dto.response.UserResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ProvisioningController {

    private final UserProvisioningService svc;

    @PreAuthorize("hasAuthority('TENANT_CREATE') and hasAuthority('OWNER_CREATE')")
    @PostMapping("/api/platform/tenants")
    public CreatedUserResponse createTenant(@Valid @RequestBody CreateTenantRequest r) {
        return svc.createTenantWithOwner(r);
    }

    @PreAuthorize("hasAuthority('TENANT_VIEW')")
    @GetMapping("/api/platform/tenants")
    public List<TenantResponse> getTenants() {
        return svc.getTenants();
    }

    @PreAuthorize("hasAuthority('MANAGEMENT_CREATE')")
    @PostMapping("/api/owner/management")
    public CreatedUserResponse createManagement(@Valid @RequestBody CreateUserRequest r) {
        return svc.createUser(Role.MANAGEMENT, r);
    }

    @PreAuthorize("hasAuthority('MANAGEMENT_VIEW')")
    @GetMapping("/api/owner/management")
    public List<UserResponse> getManagement() {
        return svc.getUsersByRole(Role.MANAGEMENT);
    }

    @PreAuthorize("hasAuthority('STAFF_CREATE')")
    @PostMapping("/api/management/staff")
    public CreatedUserResponse createStaff(@Valid @RequestBody CreateUserRequest r) {
        return svc.createUser(Role.STAFF, r);
    }

    @PreAuthorize("hasAuthority('STAFF_VIEW')")
    @GetMapping("/api/management/staff")
    public List<UserResponse> getStaff() {
        return svc.getUsersByRole(Role.STAFF);
    }

    /** Shared route: MANAGEMENT and RECEPTIONIST both hold GUEST_CREATE; nobody else does. */
    @PreAuthorize("hasAuthority('GUEST_CREATE')")
    @PostMapping("/api/guests")
    public CreatedUserResponse createGuest(@Valid @RequestBody CreateUserRequest r) {
        return svc.createUser(Role.GUEST, r);
    }

    @PreAuthorize("hasAuthority('GUEST_VIEW')")
    @GetMapping("/api/guests")
    public List<UserResponse> getGuests() {
        return svc.getUsersByRole(Role.GUEST);
    }
}

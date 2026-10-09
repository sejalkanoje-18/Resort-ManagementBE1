package com.example.rrms.security.user;

import com.example.rrms.domain.enums.Permission;
import com.example.rrms.domain.enums.Role;
import com.example.rrms.domain.enums.StaffType;

import java.util.EnumSet;
import java.util.Set;

import static com.example.rrms.domain.enums.Permission.*;

public final class PermissionRegistry {

    private PermissionRegistry() {
    }

    public static Set<Permission> resolve(Role role, StaffType staffType) {
        return switch (role) {
            case SUPER_ADMIN -> EnumSet.of(TENANT_CREATE, TENANT_VIEW, TENANT_UPDATE, OWNER_CREATE, PLATFORM_AUDIT_VIEW);

            case OWNER -> EnumSet.of(REVENUE_VIEW, REPORT_VIEW, ROLE_MANAGE, RESORT_CONFIG,
                    AUDIT_VIEW, SETTINGS_MANAGE, MANAGEMENT_CREATE, MANAGEMENT_VIEW); // No GUEST_CREATE

            case MANAGEMENT -> EnumSet.of(TASK_CREATE, TASK_ASSIGN, TASK_VIEW, TASK_INSPECT, TASK_VERIFY,
                    TASK_REJECT, TASK_REASSIGN, STAFF_CREATE, STAFF_VIEW, STAFF_UPDATE,
                    GUEST_CREATE, GUEST_VIEW, GUEST_UPDATE,
                    BOOKING_CREATE, BOOKING_VIEW, BOOKING_UPDATE);

            case STAFF -> {
                if (staffType == null) yield EnumSet.noneOf(Permission.class);
                yield switch (staffType) {
                    case RECEPTIONIST -> EnumSet.of(GUEST_CREATE, GUEST_VIEW, BOOKING_CREATE,
                            BOOKING_VIEW, BOOKING_MANAGE, CHECK_IN, CHECK_OUT);
                    case HOUSEKEEPING, MAINTENANCE, GARDENER -> EnumSet.of(TASK_VIEW_ASSIGNED, TASK_START, TASK_UPDATE, TASK_COMPLETE);
                };
            }

            case GUEST -> EnumSet.of(GUEST_PROFILE_VIEW, GUEST_PROFILE_UPDATE, GUEST_BOOKING_VIEW,
                    GUEST_BOOKING_CANCEL, GUEST_ADDON_VIEW, GUEST_ADDON_REQUEST,
                    GUEST_BILLING_VIEW, GUEST_LOYALTY_VIEW, GUEST_LOYALTY_REDEEM);
        };
    }
}

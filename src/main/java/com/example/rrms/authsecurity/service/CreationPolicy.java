package com.example.rrms.authsecurity.service;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.enums.StaffType;
import com.example.rrms.authsecurity.security.user.UserPrincipal;

final class CreationPolicy {

    private CreationPolicy() {}

    static boolean canCreate(UserPrincipal creator, Role target) {
        return switch (creator.getRole()) {
            case SUPER_ADMIN -> target == Role.OWNER;
            case OWNER       -> target == Role.MANAGEMENT;
            case MANAGEMENT  -> target == Role.STAFF || target == Role.GUEST;
            case STAFF       -> creator.getStaffType() == StaffType.RECEPTIONIST && target == Role.GUEST;
            case GUEST       -> false;
        };
    }
}

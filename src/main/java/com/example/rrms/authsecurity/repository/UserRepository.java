package com.example.rrms.authsecurity.repository;

import com.example.rrms.authsecurity.domain.enums.Role;
import com.example.rrms.authsecurity.domain.modal.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    Optional<User> findByEmailAndTenantIdIsNull(String email);            // SUPER_ADMIN login
    List<User> findByEmail(String email);
    Optional<User> findByIdAndTenantId(Long id, Long tenantId);
    List<User> findByTenantId(Long tenantId);
    List<User> findByTenantIdAndRole(Long tenantId, Role role);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
    boolean existsByEmailAndTenantIdIsNull(String email);
}
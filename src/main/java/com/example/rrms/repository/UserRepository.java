package com.example.rrms.repository;

import com.example.rrms.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndTenantId(String email, Long tenantId);
    Optional<User> findByEmailAndTenantIdIsNull(String email);            // SUPER_ADMIN login
    Optional<User> findByIdAndTenantId(Long id, Long tenantId);
    boolean existsByEmailAndTenantId(String email, Long tenantId);
    boolean existsByEmailAndTenantIdIsNull(String email);
}
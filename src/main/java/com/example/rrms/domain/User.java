package com.example.rrms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.Instant;

@Entity
@Table(name = "users",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_user_tenant_email",
               columnNames = {"tenant_id", "email"}),
       indexes = @Index(
               name = "idx_user_tenant",
               columnList = "tenant_id"))
@Filter(name = "tenantFilter", condition = "tenant_id = :tenantId")
@Getter
@Setter
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tenant_id")
    private Long tenantId; // null only for SUPER_ADMIN

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String passwordHash; // BCrypt hash

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    private StaffType staffType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserStatus status = UserStatus.ACTIVE;

    private String phone;

    private boolean mustChangePassword = true;

    private boolean mfaEnabled;

    private String mfaSecret;

    private int failedAttempts;

    private Instant lockedUntil;

    private int tokenVersion = 0;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}

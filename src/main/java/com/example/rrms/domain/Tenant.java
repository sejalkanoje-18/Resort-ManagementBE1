package com.example.rrms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "tenants", uniqueConstraints = @UniqueConstraint(
        name = "uk_tenant_name",
        columnNames = "code" ))
@Setter
@Getter
@NoArgsConstructor
public class Tenant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TenantStatus status;

    private Long createdBy;

    @Column(nullable = false, updatable = false)
    private Instant createdAt =  Instant.now();
}

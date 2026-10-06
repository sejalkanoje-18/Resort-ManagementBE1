package com.example.rrms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "audit_Logs",indexes = {
        @Index(name = "idx_audit_tenant_time",columnList = "tenantId,createdAt")})
        @Getter
        @Setter
        @NoArgsConstructor
public class AuditLog {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
     private Long tenantId;
     private Long actorId;
     private String actorRole;
     private String actorStaffType;

     @Column(nullable = false)
     private String action;
     private String resourceType;
     private String resourceId;

     @Column(nullable = false)
    private String result;
     private String details;
     private String ip;

     @Column(nullable = false,updatable = false)
    private Instant createdAt = Instant.now();
}

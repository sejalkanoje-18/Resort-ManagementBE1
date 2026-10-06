package com.example.rrms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

}

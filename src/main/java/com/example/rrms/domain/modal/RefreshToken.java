package com.example.rrms.domain.modal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "refresh_tokens",
        indexes = @Index(
                name = "idx_rt_hash",
                columnList = "tokenHash",
                unique = true))
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    private Long tenantId;

    @Column(nullable = false, length = 64)
    private String tokenHash; // SHA-256

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
}

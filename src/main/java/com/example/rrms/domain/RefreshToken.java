package com.example.rrms.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

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
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private Long userId;

    private Long  tenantId;

    @Column(nullable = false, length = 64)
    private String tokenHash; //SHA-256, never raw token

    @Column(nullable = false)
    private Instant expiresAt;

    private boolean revoked;

    private Instant createdAt = Instant.now();

}

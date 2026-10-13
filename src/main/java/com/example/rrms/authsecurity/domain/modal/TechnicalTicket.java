package com.example.rrms.authsecurity.domain.modal;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "technical_tickets")
@Getter
@Setter
@NoArgsConstructor
public class TechnicalTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long tenantId;

    @Column(nullable = false, length = 50)
    private String tenantCode;

    @Column(nullable = false)
    private String resortName;

    @Column(nullable = false)
    private Long ownerId;

    @Column(nullable = false)
    private String ownerName;

    @Column(nullable = false)
    private String ownerEmail;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, length = 50)
    private String category; // SYSTEM, DATABASE, INTEGRATION, BILLING, PERFORMANCE, SECURITY

    @Column(nullable = false, length = 50)
    private String priority; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(nullable = false, length = 50)
    private String status = "OPEN"; // OPEN, IN_PROGRESS, RESOLVED

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String resolutionNotes;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
}

package com.example.rrms.authsecurity.dto.response;

import java.time.Instant;

public record TechnicalTicketResponse(
        Long id,
        Long tenantId,
        String tenantCode,
        String resortName,
        Long ownerId,
        String ownerName,
        String ownerEmail,
        String title,
        String category,
        String priority,
        String status,
        String description,
        String resolutionNotes,
        Instant createdAt,
        Instant updatedAt
) {}

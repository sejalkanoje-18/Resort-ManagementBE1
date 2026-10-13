package com.example.rrms.authsecurity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UpdateTicketStatusRequest(
        @NotBlank String status,
        String resolutionNotes
) {}

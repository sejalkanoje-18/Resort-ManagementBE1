package com.example.rrms.authsecurity.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateTechnicalTicketRequest(
        @NotBlank String title,
        @NotBlank String category,
        @NotBlank String priority,
        @NotBlank String description
) {}

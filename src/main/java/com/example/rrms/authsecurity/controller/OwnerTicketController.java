package com.example.rrms.authsecurity.controller;

import com.example.rrms.authsecurity.dto.request.CreateTechnicalTicketRequest;
import com.example.rrms.authsecurity.dto.response.TechnicalTicketResponse;
import com.example.rrms.authsecurity.service.PlatformSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/owner/tickets")
@RequiredArgsConstructor
public class OwnerTicketController {

    private final PlatformSupportService supportService;

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGEMENT')")
    @PostMapping
    public TechnicalTicketResponse createTicket(@Valid @RequestBody CreateTechnicalTicketRequest r) {
        return supportService.createOwnerTicket(r);
    }

    @PreAuthorize("hasAnyRole('OWNER', 'MANAGEMENT')")
    @GetMapping
    public List<TechnicalTicketResponse> getMyTickets() {
        return supportService.getOwnerTickets();
    }
}

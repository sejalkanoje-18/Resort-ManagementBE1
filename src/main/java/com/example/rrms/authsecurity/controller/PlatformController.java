package com.example.rrms.authsecurity.controller;

import com.example.rrms.authsecurity.dto.request.UpdateTicketStatusRequest;
import com.example.rrms.authsecurity.dto.response.SystemMonitoringResponse;
import com.example.rrms.authsecurity.dto.response.TechnicalTicketResponse;
import com.example.rrms.authsecurity.service.PlatformSupportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class PlatformController {

    private final PlatformSupportService supportService;

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/tickets")
    public List<TechnicalTicketResponse> getAllTickets() {
        return supportService.getAllPlatformTickets();
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @RequestMapping(value = "/tickets/{id}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public TechnicalTicketResponse updateTicketStatus(@PathVariable Long id, @Valid @RequestBody UpdateTicketStatusRequest r) {
        return supportService.updateTicketStatus(id, r);
    }

    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @GetMapping("/system-monitoring")
    public SystemMonitoringResponse getSystemMonitoring() {
        return supportService.getSystemMonitoring();
    }
}

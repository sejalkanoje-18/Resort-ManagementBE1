package com.example.rrms.authsecurity.dto.response;

import java.util.Map;

public record SystemMonitoringResponse(
        String status,
        long uptimeSeconds,
        long usedMemoryMb,
        long freeMemoryMb,
        long totalMemoryMb,
        long maxMemoryMb,
        double memoryUsagePercent,
        int activeThreads,
        int availableProcessors,
        String javaVersion,
        long totalTenants,
        long activeTenants,
        long totalUsers,
        Map<String, Long> usersByRole,
        long openTicketsCount,
        long criticalTicketsCount,
        long totalAuditEvents,
        String databaseStatus
) {}

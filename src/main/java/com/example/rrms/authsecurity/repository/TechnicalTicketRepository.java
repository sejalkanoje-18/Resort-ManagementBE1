package com.example.rrms.authsecurity.repository;

import com.example.rrms.authsecurity.domain.modal.TechnicalTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TechnicalTicketRepository extends JpaRepository<TechnicalTicket, Long> {
    List<TechnicalTicket> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
    List<TechnicalTicket> findAllByOrderByCreatedAtDesc();
}

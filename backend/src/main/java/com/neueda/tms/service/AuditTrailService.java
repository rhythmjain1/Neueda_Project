package com.neueda.tms.service;

import com.neueda.tms.model.*;
import com.neueda.tms.repository.AlertAuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTrailService {

    private final AlertAuditTrailRepository auditTrailRepository;

    @Transactional
    public AlertAuditTrail recordAction(Alert alert, AlertAuditTrail.AuditAction action,
                                         String performedBy, String notes) {
        AlertAuditTrail entry = AlertAuditTrail.builder()
                .alert(alert)
                .action(action)
                .performedBy(performedBy)
                .notes(notes)
                .build();
        AlertAuditTrail saved = auditTrailRepository.save(entry);
        log.info("Audit recorded — Alert: {}, Action: {}, By: {}", alert.getId(), action, performedBy);
        return saved;
    }
}

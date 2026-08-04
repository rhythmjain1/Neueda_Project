package com.neueda.tms.service.alert;

import com.neueda.tms.repository.alert.Alert;
import com.neueda.tms.repository.alert.AlertAuditTrail;

import com.neueda.tms.repository.*;
import com.neueda.tms.repository.alert.AlertAuditTrailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AuditTrailService {

    private final AlertAuditTrailRepository auditTrailRepository;

    @Autowired
    public AuditTrailService(AlertAuditTrailRepository auditTrailRepository) {
        this.auditTrailRepository = auditTrailRepository;
    }

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

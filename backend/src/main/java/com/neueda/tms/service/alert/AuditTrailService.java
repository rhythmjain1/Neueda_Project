package com.neueda.tms.service.alert;

import com.neueda.tms.repository.alert.Alert;
import com.neueda.tms.repository.alert.AlertAuditTrail;

import com.neueda.tms.repository.alert.AlertAuditTrailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditTrailService {
    private static final Logger log = LoggerFactory.getLogger(AuditTrailService.class);


    private final AlertAuditTrailRepository auditTrailRepository;

    @Autowired
    public AuditTrailService(AlertAuditTrailRepository auditTrailRepository) {
        this.auditTrailRepository = auditTrailRepository;
    }

    @Transactional
    public AlertAuditTrail recordAction(Alert alert, AlertAuditTrail.AuditAction action,
                                        String performedBy, String notes) {
        AlertAuditTrail entry = new AlertAuditTrail(alert, action, performedBy, notes);
        AlertAuditTrail saved = auditTrailRepository.save(entry);
        log.info("Audit recorded — Alert: {}, Action: {}, By: {}", alert.getId(), action, performedBy);
        return saved;
    }
}

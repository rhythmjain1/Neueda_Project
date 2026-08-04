package com.neueda.tms.service.alert;

import com.neueda.tms.repository.alert.Alert;
import com.neueda.tms.repository.alert.AlertAuditTrail;

import com.neueda.tms.controller.alert.AlertDTO;
import com.neueda.tms.controller.alert.AuditTrailDTO;
import com.neueda.tms.controller.common.PageResponse;
import com.neueda.tms.repository.alert.AlertAuditTrailRepository;
import com.neueda.tms.repository.alert.AlertRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class AlertService implements IAlertService {

    private final AlertRepository alertRepository;
    private final AlertAuditTrailRepository auditTrailRepository;
    private final AuditTrailService auditTrailService;

    @Autowired
    public AlertService(AlertRepository alertRepository,
                        AlertAuditTrailRepository auditTrailRepository,
                        AuditTrailService auditTrailService) {
        this.alertRepository = alertRepository;
        this.auditTrailRepository = auditTrailRepository;
        this.auditTrailService = auditTrailService;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlertDTO.Response> searchAlerts(
            String status, String severity,
            LocalDateTime fromDate, LocalDateTime toDate,
            String search, int page, int size, String sortBy, String sortDir) {

        Alert.AlertStatus statusEnum = status != null && !status.isBlank()
                ? Alert.AlertStatus.valueOf(status.toUpperCase()) : null;
        Alert.AlertSeverity severityEnum = severity != null && !severity.isBlank()
                ? Alert.AlertSeverity.valueOf(severity.toUpperCase()) : null;

        int offset = page * size;
        List<Alert> content = alertRepository.searchAlerts(
                statusEnum, severityEnum, fromDate, toDate, search,
                offset, size, sortBy, sortDir);

        long totalElements = alertRepository.countSearchAlerts(
                statusEnum, severityEnum, fromDate, toDate, search);

        long totalPages = (totalElements + size - 1) / size;

        return new PageResponse<>(
                content.stream().map(this::toResponse).toList(),
                page,
                size,
                totalElements,
                (int) totalPages,
                page >= totalPages - 1,
                page == 0
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AlertDTO.Response getAlert(Long id) {
        Alert alert = findAlert(id);
        return toResponse(alert);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AuditTrailDTO> getAuditTrail(Long alertId) {
        return auditTrailRepository.findByAlertIdOrderByCreatedAtAsc(alertId)
                .stream().map(this::toAuditDto).toList();
    }

    @Override
    @Transactional
    public AlertDTO.Response forwardAlert(Long id, AlertDTO.ActionRequest request, String operator) {
        Alert alert = findAlert(id);
        assertStatus(alert, Alert.AlertStatus.OPEN, "Can only forward OPEN alerts");

        alert.setStatus(Alert.AlertStatus.FORWARDED);
        alert.setAssignedTo(request.getAssignedTo() != null
                ? request.getAssignedTo() : "Investigation Team");
        alert.setUpdatedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        auditTrailService.recordAction(saved, AlertAuditTrail.AuditAction.FORWARDED, operator,
                request.getNotes() != null ? request.getNotes() : "Alert forwarded to investigation team.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AlertDTO.Response dismissAlert(Long id, AlertDTO.ActionRequest request, String operator) {
        Alert alert = findAlert(id);
        assertStatus(alert, Alert.AlertStatus.OPEN, "Can only dismiss OPEN alerts");

        alert.setStatus(Alert.AlertStatus.DISMISSED);
        alert.setUpdatedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        auditTrailService.recordAction(saved, AlertAuditTrail.AuditAction.DISMISSED, operator,
                request.getNotes() != null ? request.getNotes() : "Alert dismissed.");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public AlertDTO.Response closeAlert(Long id, AlertDTO.ActionRequest request, String operator) {
        Alert alert = findAlert(id);
        if (alert.getStatus() == Alert.AlertStatus.CLOSED) {
            throw new IllegalStateException("Alert is already closed.");
        }

        alert.setStatus(Alert.AlertStatus.CLOSED);
        alert.setUpdatedAt(LocalDateTime.now());
        Alert saved = alertRepository.save(alert);

        auditTrailService.recordAction(saved, AlertAuditTrail.AuditAction.CLOSED, operator,
                request.getNotes() != null ? request.getNotes() : "Alert closed.");
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public AlertDTO.StatsResponse getStats() {
        long total = alertRepository.count();
        long open = alertRepository.countByStatus(Alert.AlertStatus.OPEN);
        long forwarded = alertRepository.countByStatus(Alert.AlertStatus.FORWARDED);
        long dismissed = alertRepository.countByStatus(Alert.AlertStatus.DISMISSED);
        long closed = alertRepository.countByStatus(Alert.AlertStatus.CLOSED);
        double pctForwarded = total > 0 ? (double) forwarded / total * 100 : 0;

        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        LocalDateTime last7d = LocalDateTime.now().minusDays(7);

        return new AlertDTO.StatsResponse(
                total,
                open,
                forwarded,
                dismissed,
                closed,
                Math.round(pctForwarded * 100.0) / 100.0,
                alertRepository.countAlertsSince(last24h),
                alertRepository.countAlertsSince(last7d)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<AlertDTO.Response> getForwardedAlerts() {
        return alertRepository.findByStatus(Alert.AlertStatus.FORWARDED, 0, 1000)
                .stream().map(this::toResponse).toList();
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private Alert findAlert(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Alert not found: " + id));
    }

    private void assertStatus(Alert alert, Alert.AlertStatus required, String message) {
        if (alert.getStatus() != required) {
            throw new IllegalStateException(message + ". Current status: " + alert.getStatus());
        }
    }

    private AlertDTO.Response toResponse(Alert a) {
        return new AlertDTO.Response(
                a.getId(),
                a.getTransaction().getId(),
                a.getTransaction().getTransactionRef(),
                a.getTransaction().getAccountId(),
                a.getTransaction().getCustomerName(),
                a.getRule().getId(),
                a.getRule().getRuleCode(),
                a.getRule().getRuleName(),
                a.getStatus(),
                a.getSeverity(),
                a.getDescription(),
                a.getAssignedTo(),
                a.getCreatedAt(),
                a.getUpdatedAt()
        );
    }

    private AuditTrailDTO toAuditDto(AlertAuditTrail t) {
        return new AuditTrailDTO(
                t.getId(),
                t.getAlert().getId(),
                t.getAlert().getTransaction().getId(),
                t.getAlert().getTransaction().getTransactionRef(),
                t.getAlert().getTransaction().getAccountId(),
                t.getAction(),
                t.getPerformedBy(),
                t.getNotes(),
                t.getCreatedAt()
        );
    }
}

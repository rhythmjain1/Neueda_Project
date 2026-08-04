package com.neueda.tms.service.report;

import com.neueda.tms.controller.*;
import com.neueda.tms.repository.alert.Alert;
import com.neueda.tms.repository.alert.AlertAuditTrail;
import com.neueda.tms.repository.alert.AlertAuditTrailRepository;
import com.neueda.tms.repository.alert.AlertRepository;
import com.neueda.tms.repository.transaction.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ReportService implements IReportService {

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final AlertAuditTrailRepository auditTrailRepository;

    @Autowired
    public ReportService(TransactionRepository transactionRepository,
                         AlertRepository alertRepository,
                         AlertAuditTrailRepository auditTrailRepository) {
        this.transactionRepository = transactionRepository;
        this.alertRepository = alertRepository;
        this.auditTrailRepository = auditTrailRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<TransactionDTO.Response> getTransactionReport(
            LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

        int offset = page * size;
        var content = transactionRepository.searchTransactions(
                null, null, null, null, fromDate, toDate, null, null,
                offset, size, "created_at", "desc");

        long totalElements = transactionRepository.countSearchTransactions(
                null, null, null, null, fromDate, toDate, null, null);

        long totalPages = (totalElements + size - 1) / size;

        return PageResponse.<TransactionDTO.Response>builder()
                .content(content.stream().map(t -> TransactionDTO.Response.builder()
                        .id(t.getId())
                        .transactionRef(t.getTransactionRef())
                        .accountId(t.getAccountId())
                        .customerName(t.getCustomerName())
                        .amount(t.getAmount())
                        .currency(t.getCurrency())
                        .countryCode(t.getCountryCode())
                        .transactionType(t.getTransactionType())
                        .status(t.getStatus())
                        .isNewCustomer(t.getIsNewCustomer())
                        .createdAt(t.getCreatedAt())
                        .alertsGenerated(0)
                        .build()).toList())
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages((int) totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AlertDTO.Response> getAlertReport(
            LocalDateTime fromDate, LocalDateTime toDate, String status, int page, int size) {

        Alert.AlertStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = Alert.AlertStatus.valueOf(status.toUpperCase());
        }

        int offset = page * size;
        var content = alertRepository.searchAlerts(
                statusEnum, null, fromDate, toDate, null,
                offset, size, "created_at", "desc");

        long totalElements = alertRepository.countSearchAlerts(statusEnum, null, fromDate, toDate, null);
        long totalPages = (totalElements + size - 1) / size;

        return PageResponse.<AlertDTO.Response>builder()
                .content(content.stream().map(a -> AlertDTO.Response.builder()
                        .id(a.getId())
                        .transactionId(a.getTransaction().getId())
                        .transactionRef(a.getTransaction().getTransactionRef())
                        .accountId(a.getTransaction().getAccountId())
                        .customerName(a.getTransaction().getCustomerName())
                        .ruleId(a.getRule().getId())
                        .ruleCode(a.getRule().getRuleCode())
                        .ruleName(a.getRule().getRuleName())
                        .status(a.getStatus())
                        .severity(a.getSeverity())
                        .description(a.getDescription())
                        .assignedTo(a.getAssignedTo())
                        .createdAt(a.getCreatedAt())
                        .updatedAt(a.getUpdatedAt())
                        .build()).toList())
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages((int) totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAccountAlertReport() {
        return alertRepository.countGroupByStatus();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AuditTrailDTO> getAuditReport(
            LocalDateTime fromDate, LocalDateTime toDate, String action, int page, int size) {

        AlertAuditTrail.AuditAction actionEnum = null;
        if (action != null && !action.isBlank()) {
            actionEnum = AlertAuditTrail.AuditAction.valueOf(action.toUpperCase());
        }

        int offset = page * size;
        var content = auditTrailRepository.findAuditReport(fromDate, toDate, actionEnum, offset, size);
        long totalElements = auditTrailRepository.countAuditReport(fromDate, toDate, actionEnum);
        long totalPages = (totalElements + size - 1) / size;

        return PageResponse.<AuditTrailDTO>builder()
                .content(content.stream().map(t -> AuditTrailDTO.builder()
                        .id(t.getId())
                        .alertId(t.getAlert().getId())
                        .transactionId(t.getAlert().getTransaction().getId())
                        .transactionRef(t.getAlert().getTransaction().getTransactionRef())
                        .accountId(t.getAlert().getTransaction().getAccountId())
                        .action(t.getAction())
                        .performedBy(t.getPerformedBy())
                        .notes(t.getNotes())
                        .createdAt(t.getCreatedAt())
                        .build()).toList())
                .pageNumber(page)
                .pageSize(size)
                .totalElements(totalElements)
                .totalPages((int) totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}

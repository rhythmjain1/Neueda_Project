package com.neueda.tms.service;

import com.neueda.tms.dto.*;
import com.neueda.tms.model.Alert;
import com.neueda.tms.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final AlertRepository alertRepository;
    private final AlertAuditTrailRepository auditTrailRepository;

    @Transactional(readOnly = true)
    public PageResponse<TransactionDTO.Response> getTransactionReport(
            LocalDateTime fromDate, LocalDateTime toDate, int page, int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = transactionRepository.searchTransactions(
                null, null, null, null, fromDate, toDate, null, null, pageable);

        return PageResponse.<TransactionDTO.Response>builder()
                .content(result.getContent().stream().map(t -> TransactionDTO.Response.builder()
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
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<AlertDTO.Response> getAlertReport(
            LocalDateTime fromDate, LocalDateTime toDate, String status, int page, int size) {

        Alert.AlertStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = Alert.AlertStatus.valueOf(status.toUpperCase());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<com.neueda.tms.model.Alert> result = alertRepository.searchAlerts(
                statusEnum, null, fromDate, toDate, null, pageable);

        return PageResponse.<AlertDTO.Response>builder()
                .content(result.getContent().stream().map(a -> AlertDTO.Response.builder()
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
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAccountAlertReport() {
        List<Object[]> rawData = alertRepository.countGroupByStatus();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rawData) {
            result.add(Map.of("status", row[0].toString(), "count", row[1]));
        }
        return result;
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditTrailDTO> getAuditReport(
            LocalDateTime fromDate, LocalDateTime toDate, String action, int page, int size) {

        com.neueda.tms.model.AlertAuditTrail.AuditAction actionEnum = null;
        if (action != null && !action.isBlank()) {
            actionEnum = com.neueda.tms.model.AlertAuditTrail.AuditAction.valueOf(action.toUpperCase());
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        var result = auditTrailRepository.findAuditReport(fromDate, toDate, actionEnum, pageable);

        return PageResponse.<AuditTrailDTO>builder()
                .content(result.getContent().stream().map(t -> AuditTrailDTO.builder()
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
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }
}

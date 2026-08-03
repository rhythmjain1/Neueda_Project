package com.neueda.tms.service;

import com.neueda.tms.dto.PageResponse;
import com.neueda.tms.dto.TransactionDTO;
import com.neueda.tms.model.Alert;
import com.neueda.tms.model.Transaction;
import com.neueda.tms.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final MonitoringEngineService monitoringEngineService;

    @Transactional
    public TransactionDTO.Response submitTransaction(TransactionDTO.Request request) {
        if (transactionRepository.findByTransactionRef(request.getTransactionRef()).isPresent()) {
            throw new IllegalArgumentException("Transaction with ref '" + request.getTransactionRef() + "' already exists.");
        }

        Transaction transaction = Transaction.builder()
                .transactionRef(request.getTransactionRef())
                .accountId(request.getAccountId())
                .customerName(request.getCustomerName())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .countryCode(request.getCountryCode())
                .transactionType(request.getTransactionType())
                .isNewCustomer(request.getIsNewCustomer() != null ? request.getIsNewCustomer() : false)
                .metadata(request.getMetadata())
                .createdAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction saved: {}", saved.getTransactionRef());

        // Synchronous rule evaluation immediately after save
        List<Alert> alerts = monitoringEngineService.evaluate(saved);

        // If any alerts generated, flag the transaction
        if (!alerts.isEmpty()) {
            saved.setStatus(Transaction.TransactionStatus.FLAGGED);
            transactionRepository.save(saved);
        } else {
            saved.setStatus(Transaction.TransactionStatus.COMPLETED);
            transactionRepository.save(saved);
        }

        return toResponse(saved, alerts.size());
    }

    @Transactional(readOnly = true)
    public TransactionDTO.Response getTransaction(Long id) {
        Transaction tx = transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found: " + id));
        return toResponse(tx, 0);
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionDTO.Response> searchTransactions(
            String search,
            String status,
            String type,
            String countryCode,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Transaction.TransactionStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            statusEnum = Transaction.TransactionStatus.valueOf(status.toUpperCase());
        }

        Transaction.TransactionType typeEnum = null;
        if (type != null && !type.isBlank()) {
            typeEnum = Transaction.TransactionType.valueOf(type.toUpperCase());
        }

        Page<Transaction> result = transactionRepository.searchTransactions(
                search, statusEnum, typeEnum, countryCode,
                fromDate, toDate, minAmount, maxAmount, pageable
        );

        return PageResponse.<TransactionDTO.Response>builder()
                .content(result.getContent().stream().map(t -> toResponse(t, 0)).toList())
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<TransactionDTO.Response> getTransactionsByAccount(String accountId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> result = transactionRepository.findByAccountId(accountId, pageable);
        return PageResponse.<TransactionDTO.Response>builder()
                .content(result.getContent().stream().map(t -> toResponse(t, 0)).toList())
                .pageNumber(result.getNumber())
                .pageSize(result.getSize())
                .totalElements(result.getTotalElements())
                .totalPages(result.getTotalPages())
                .first(result.isFirst())
                .last(result.isLast())
                .build();
    }

    private TransactionDTO.Response toResponse(Transaction t, int alertsGenerated) {
        return TransactionDTO.Response.builder()
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
                .metadata(t.getMetadata())
                .alertsGenerated(alertsGenerated)
                .build();
    }
}

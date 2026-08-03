package com.neueda.tms.controller;

import com.neueda.tms.dto.PageResponse;
import com.neueda.tms.dto.TransactionDTO;
import com.neueda.tms.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    public ResponseEntity<TransactionDTO.Response> submit(@Valid @RequestBody TransactionDTO.Request request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.submitTransaction(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO.Response> getById(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.getTransaction(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<TransactionDTO.Response>> search(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String countryCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) BigDecimal minAmount,
            @RequestParam(required = false) BigDecimal maxAmount,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        return ResponseEntity.ok(transactionService.searchTransactions(
                search, status, type, countryCode,
                fromDate, toDate, minAmount, maxAmount,
                page, size, sortBy, sortDir));
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<PageResponse<TransactionDTO.Response>> getByAccount(
            @PathVariable String accountId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(transactionService.getTransactionsByAccount(accountId, page, size));
    }
}

package com.neueda.tms.service;

import com.neueda.tms.dto.PageResponse;
import com.neueda.tms.dto.TransactionDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ITransactionService {

    TransactionDTO.Response submitTransaction(TransactionDTO.Request request);

    TransactionDTO.Response getTransaction(Long id);

    PageResponse<TransactionDTO.Response> searchTransactions(
            String search, String status, String type, String countryCode,
            LocalDateTime fromDate, LocalDateTime toDate,
            BigDecimal minAmount, BigDecimal maxAmount,
            int page, int size, String sortBy, String sortDir);

    PageResponse<TransactionDTO.Response> getTransactionsByAccount(
            String accountId, int page, int size);
}

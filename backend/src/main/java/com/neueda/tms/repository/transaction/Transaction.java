package com.neueda.tms.repository.transaction;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    private Long id;

    private String transactionRef;

    private String accountId;

    private String customerName;

    private BigDecimal amount;

    private String currency;

    private String countryCode;

    private TransactionType transactionType;

    @Builder.Default
    private TransactionStatus status = TransactionStatus.PENDING;

    @Builder.Default
    private Boolean isNewCustomer = false;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private Map<String, Object> metadata;

    public enum TransactionType {
        CREDIT, DEBIT, TRANSFER
    }

    public enum TransactionStatus {
        PENDING, COMPLETED, FLAGGED
    }
}

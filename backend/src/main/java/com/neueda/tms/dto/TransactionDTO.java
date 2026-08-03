package com.neueda.tms.dto;

import com.neueda.tms.model.Transaction;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class TransactionDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {

        @NotBlank(message = "Transaction reference is required")
        @Size(max = 64)
        private String transactionRef;

        @NotBlank(message = "Account ID is required")
        @Size(max = 64)
        private String accountId;

        @NotBlank(message = "Customer name is required")
        private String customerName;

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be positive")
        @Digits(integer = 16, fraction = 2)
        private BigDecimal amount;

        @NotBlank(message = "Currency is required")
        @Size(min = 3, max = 10)
        private String currency;

        @NotBlank(message = "Country code is required")
        @Size(min = 2, max = 10)
        private String countryCode;

        @NotNull(message = "Transaction type is required")
        private Transaction.TransactionType transactionType;

        private Boolean isNewCustomer;

        private Map<String, Object> metadata;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String transactionRef;
        private String accountId;
        private String customerName;
        private BigDecimal amount;
        private String currency;
        private String countryCode;
        private Transaction.TransactionType transactionType;
        private Transaction.TransactionStatus status;
        private Boolean isNewCustomer;
        private LocalDateTime createdAt;
        private Map<String, Object> metadata;
        private int alertsGenerated;
    }
}

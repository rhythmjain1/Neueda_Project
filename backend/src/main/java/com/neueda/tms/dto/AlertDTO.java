package com.neueda.tms.dto;

import com.neueda.tms.model.Alert;
import lombok.*;

import java.time.LocalDateTime;

public class AlertDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private Long transactionId;
        private String transactionRef;
        private String accountId;
        private String customerName;
        private Long ruleId;
        private String ruleCode;
        private String ruleName;
        private Alert.AlertStatus status;
        private Alert.AlertSeverity severity;
        private String description;
        private String assignedTo;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActionRequest {
        private String notes;
        private String assignedTo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class StatsResponse {
        private long totalAlerts;
        private long openAlerts;
        private long forwardedAlerts;
        private long dismissedAlerts;
        private long closedAlerts;
        private double percentageForwarded;
        private long alertsLast24h;
        private long alertsLast7d;
    }
}

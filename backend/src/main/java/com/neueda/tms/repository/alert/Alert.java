package com.neueda.tms.repository.alert;

import com.neueda.tms.repository.transaction.Transaction;
import com.neueda.tms.repository.rule.MonitoringRule;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    private Long id;

    private Transaction transaction;

    private MonitoringRule rule;

    @Builder.Default
    private AlertStatus status = AlertStatus.OPEN;

    private AlertSeverity severity;

    private String description;

    private String assignedTo;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum AlertStatus {
        OPEN, FORWARDED, DISMISSED, CLOSED
    }

    public enum AlertSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}

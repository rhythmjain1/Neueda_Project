package com.neueda.tms.repository.alert;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlertAuditTrail {

    private Long id;

    private Alert alert;

    private AuditAction action;

    private String performedBy;

    private String notes;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum AuditAction {
        CREATED, FORWARDED, DISMISSED, CLOSED, NOTE_ADDED, REOPENED
    }
}

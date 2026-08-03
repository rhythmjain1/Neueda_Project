package com.neueda.tms.dto;

import com.neueda.tms.model.AlertAuditTrail;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditTrailDTO {
    private Long id;
    private Long alertId;
    private Long transactionId;
    private String transactionRef;
    private String accountId;
    private AlertAuditTrail.AuditAction action;
    private String performedBy;
    private String notes;
    private LocalDateTime createdAt;
}

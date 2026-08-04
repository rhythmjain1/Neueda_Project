package com.neueda.tms.controller.alert;

import com.neueda.tms.repository.alert.AlertAuditTrail;
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

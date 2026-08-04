package com.neueda.tms.repository.rule;

import lombok.*;

import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonitoringRule {

    private Long id;

    private String ruleCode;

    private String ruleName;

    private String description;

    private RuleSeverity severity;

    @Builder.Default
    private Boolean isActive = true;

    private Map<String, Object> parameters;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;

    public enum RuleSeverity {
        LOW, MEDIUM, HIGH, CRITICAL
    }
}

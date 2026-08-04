package com.neueda.tms.controller.rule;

import com.neueda.tms.repository.rule.MonitoringRule;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Map;

public class RuleDTO {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String ruleCode;
        private String ruleName;
        private String description;
        private MonitoringRule.RuleSeverity severity;
        private Boolean isActive;
        private Map<String, Object> parameters;
        private LocalDateTime updatedAt;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private Boolean isActive;
        private Map<String, Object> parameters;
    }
}

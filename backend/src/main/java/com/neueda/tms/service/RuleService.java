package com.neueda.tms.service;

import com.neueda.tms.dto.RuleDTO;
import com.neueda.tms.model.MonitoringRule;
import com.neueda.tms.repository.MonitoringRuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RuleService implements IRuleService {

    private final MonitoringRuleRepository ruleRepository;

    @Autowired
    public RuleService(MonitoringRuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RuleDTO.Response> getAllRules() {
        return ruleRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RuleDTO.Response getRule(Long id) {
        return toResponse(findRule(id));
    }

    @Override
    @Transactional
    public RuleDTO.Response updateRule(Long id, RuleDTO.UpdateRequest request) {
        MonitoringRule rule = findRule(id);

        if (request.getIsActive() != null) {
            rule.setIsActive(request.getIsActive());
        }
        if (request.getParameters() != null && !request.getParameters().isEmpty()) {
            rule.setParameters(request.getParameters());
        }
        rule.setUpdatedAt(LocalDateTime.now());

        return toResponse(ruleRepository.save(rule));
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private MonitoringRule findRule(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Rule not found: " + id));
    }

    private RuleDTO.Response toResponse(MonitoringRule r) {
        return RuleDTO.Response.builder()
                .id(r.getId())
                .ruleCode(r.getRuleCode())
                .ruleName(r.getRuleName())
                .description(r.getDescription())
                .severity(r.getSeverity())
                .isActive(r.getIsActive())
                .parameters(r.getParameters())
                .updatedAt(r.getUpdatedAt())
                .build();
    }
}

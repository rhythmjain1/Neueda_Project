package com.neueda.tms.repository;

import com.neueda.tms.model.MonitoringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MonitoringRuleRepository extends JpaRepository<MonitoringRule, Long> {

    Optional<MonitoringRule> findByRuleCode(String ruleCode);

    List<MonitoringRule> findByIsActiveTrue();

    boolean existsByRuleCode(String ruleCode);
}

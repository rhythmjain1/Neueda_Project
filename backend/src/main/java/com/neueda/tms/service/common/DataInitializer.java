package com.neueda.tms.service.common;

import com.neueda.tms.repository.*;
import com.neueda.tms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Seeds the database with:
 * 1. Default admin user (admin / admin123) — change in production via env vars
 * 2. All 6 monitoring rules with default parameter values
 */
@Component
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MonitoringRuleRepository ruleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           MonitoringRuleRepository ruleRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.ruleRepository = ruleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdminUser();
        seedMonitoringRules();
    }

    private void seedAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            User admin = User.builder()
                    .username("admin")
                    .passwordHash(passwordEncoder.encode("admin123"))
                    .role(User.UserRole.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Default admin user created. CHANGE PASSWORD IN PRODUCTION.");
        }
    }

    private void seedMonitoringRules() {
        List<MonitoringRule> rules = List.of(
                buildRule("HIGH_AMOUNT", "High Transaction Amount",
                        "Triggered when transaction amount exceeds the threshold.",
                        MonitoringRule.RuleSeverity.HIGH,
                        Map.of("threshold", 10000)),

                buildRule("RAPID_TRANSACTIONS", "Rapid Transactions",
                        "Triggered when same account makes multiple transactions in a short window.",
                        MonitoringRule.RuleSeverity.CRITICAL,
                        Map.of("maxCount", 5, "windowMinutes", 10)),

                buildRule("RESTRICTED_COUNTRY", "Restricted Country",
                        "Triggered when transaction originates from a restricted country.",
                        MonitoringRule.RuleSeverity.CRITICAL,
                        Map.of("countries", List.of("KP"))),

                buildRule("NEW_CUSTOMER_HIGH_AMOUNT", "New Customer High Amount",
                        "Triggered when a new customer makes a high-value transaction.",
                        MonitoringRule.RuleSeverity.HIGH,
                        Map.of("threshold", 5000)),

                buildRule("ROUND_AMOUNT", "Round Amount (Structuring)",
                        "Triggered when transaction amount is suspiciously round (possible structuring).",
                        MonitoringRule.RuleSeverity.MEDIUM,
                        Map.of("divisor", 1000, "minAmount", 1000)),

                buildRule("ODD_HOURS", "Odd Hours Transaction",
                        "Triggered when a transaction occurs during unusual hours (default: midnight to 4am).",
                        MonitoringRule.RuleSeverity.LOW,
                        Map.of("startHour", 0, "endHour", 4))
        );

        for (MonitoringRule rule : rules) {
            if (!ruleRepository.existsByRuleCode(rule.getRuleCode())) {
                ruleRepository.save(rule);
                log.info("Seeded monitoring rule: {}", rule.getRuleCode());
            }
        }
    }

    private MonitoringRule buildRule(String code, String name, String desc,
                                     MonitoringRule.RuleSeverity severity,
                                     Map<String, Object> params) {
        Map<String, Object> paramsCopy = new HashMap<>(params);
        return MonitoringRule.builder()
                .ruleCode(code)
                .ruleName(name)
                .description(desc)
                .severity(severity)
                .isActive(true)
                .parameters(paramsCopy)
                .build();
    }
}

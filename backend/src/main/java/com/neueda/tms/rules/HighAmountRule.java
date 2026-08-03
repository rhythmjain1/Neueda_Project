package com.neueda.tms.rules;

import com.neueda.tms.model.Alert;
import com.neueda.tms.model.MonitoringRule;
import com.neueda.tms.model.Transaction;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Rule: HIGH_AMOUNT
 * Triggered when a transaction amount exceeds a configurable threshold.
 * Default threshold: 10000 USD equivalent.
 */
@Component
public class HighAmountRule implements RuleEvaluator {

    @Override
    public String getRuleCode() {
        return "HIGH_AMOUNT";
    }

    @Override
    public Optional<String> evaluate(Transaction transaction, MonitoringRule rule) {
        BigDecimal threshold = getThreshold(rule);
        if (transaction.getAmount().compareTo(threshold) > 0) {
            return Optional.of(String.format(
                "Transaction amount %s %s exceeds the high amount threshold of %s.",
                transaction.getCurrency(),
                transaction.getAmount().toPlainString(),
                threshold.toPlainString()
            ));
        }
        return Optional.empty();
    }

    @Override
    public Alert.AlertSeverity getSeverity(MonitoringRule rule) {
        BigDecimal threshold = getThreshold(rule);
        // Escalate severity based on how much threshold is exceeded
        return Alert.AlertSeverity.HIGH;
    }

    private BigDecimal getThreshold(MonitoringRule rule) {
        if (rule.getParameters() != null && rule.getParameters().containsKey("threshold")) {
            return new BigDecimal(rule.getParameters().get("threshold").toString());
        }
        return new BigDecimal("10000");
    }
}

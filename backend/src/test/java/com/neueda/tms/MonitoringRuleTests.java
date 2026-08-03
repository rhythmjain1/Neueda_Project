package com.neueda.tms;

import com.neueda.tms.model.Alert;
import com.neueda.tms.model.MonitoringRule;
import com.neueda.tms.model.Transaction;
import com.neueda.tms.repository.TransactionRepository;
import com.neueda.tms.rules.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

class MonitoringRuleTests {

    @Mock
    private TransactionRepository transactionRepository;

    private Transaction buildTx() {
        return Transaction.builder()
                .id(1L)
                .transactionRef("REF-001")
                .accountId("ACC-123")
                .customerName("John Doe")
                .amount(new BigDecimal("15000"))
                .currency("USD")
                .countryCode("US")
                .transactionType(Transaction.TransactionType.CREDIT)
                .isNewCustomer(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private MonitoringRule buildRule(String code, Map<String, Object> params) {
        return MonitoringRule.builder()
                .id(1L)
                .ruleCode(code)
                .ruleName(code)
                .isActive(true)
                .parameters(params)
                .build();
    }

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    // ── HIGH_AMOUNT Rule ────────────────────────────────────────────────────────

    @Test
    void highAmount_shouldTrigger_whenAmountExceedsThreshold() {
        HighAmountRule rule = new HighAmountRule();
        Transaction tx = buildTx(); // amount = 15000
        MonitoringRule mr = buildRule("HIGH_AMOUNT", Map.of("threshold", 10000));
        Optional<String> result = rule.evaluate(tx, mr);
        assertThat(result).isPresent();
        assertThat(result.get()).contains("15000");
    }

    @Test
    void highAmount_shouldNotTrigger_whenAmountBelowThreshold() {
        HighAmountRule rule = new HighAmountRule();
        Transaction tx = buildTx();
        tx.setAmount(new BigDecimal("5000"));
        MonitoringRule mr = buildRule("HIGH_AMOUNT", Map.of("threshold", 10000));
        assertThat(rule.evaluate(tx, mr)).isEmpty();
    }

    @Test
    void highAmount_shouldUseDefaultThreshold_whenNoParams() {
        HighAmountRule rule = new HighAmountRule();
        Transaction tx = buildTx(); // 15000 > 10000 default
        MonitoringRule mr = buildRule("HIGH_AMOUNT", Map.of());
        assertThat(rule.evaluate(tx, mr)).isPresent();
    }

    // ── RESTRICTED_COUNTRY Rule ─────────────────────────────────────────────────

    @Test
    void restrictedCountry_shouldTrigger_forNorthKorea() {
        RestrictedCountryRule rule = new RestrictedCountryRule();
        Transaction tx = buildTx();
        tx.setCountryCode("KP");
        MonitoringRule mr = buildRule("RESTRICTED_COUNTRY", Map.of("countries", java.util.List.of("KP")));
        assertThat(rule.evaluate(tx, mr)).isPresent();
    }

    @Test
    void restrictedCountry_shouldNotTrigger_forAllowedCountry() {
        RestrictedCountryRule rule = new RestrictedCountryRule();
        Transaction tx = buildTx(); // countryCode = "US"
        MonitoringRule mr = buildRule("RESTRICTED_COUNTRY", Map.of("countries", java.util.List.of("KP")));
        assertThat(rule.evaluate(tx, mr)).isEmpty();
    }

    @Test
    void restrictedCountry_isCaseInsensitive() {
        RestrictedCountryRule rule = new RestrictedCountryRule();
        Transaction tx = buildTx();
        tx.setCountryCode("kp");
        MonitoringRule mr = buildRule("RESTRICTED_COUNTRY", Map.of("countries", java.util.List.of("KP")));
        assertThat(rule.evaluate(tx, mr)).isPresent();
    }

    // ── NEW_CUSTOMER_HIGH_AMOUNT Rule ───────────────────────────────────────────

    @Test
    void newCustomerHighAmount_shouldTrigger_forNewCustomerAboveThreshold() {
        NewCustomerHighAmountRule rule = new NewCustomerHighAmountRule();
        Transaction tx = buildTx();
        tx.setIsNewCustomer(true);
        tx.setAmount(new BigDecimal("6000"));
        MonitoringRule mr = buildRule("NEW_CUSTOMER_HIGH_AMOUNT", Map.of("threshold", 5000));
        assertThat(rule.evaluate(tx, mr)).isPresent();
    }

    @Test
    void newCustomerHighAmount_shouldNotTrigger_forExistingCustomer() {
        NewCustomerHighAmountRule rule = new NewCustomerHighAmountRule();
        Transaction tx = buildTx(); // isNewCustomer = false
        tx.setAmount(new BigDecimal("6000"));
        MonitoringRule mr = buildRule("NEW_CUSTOMER_HIGH_AMOUNT", Map.of("threshold", 5000));
        assertThat(rule.evaluate(tx, mr)).isEmpty();
    }

    // ── ROUND_AMOUNT Rule ───────────────────────────────────────────────────────

    @Test
    void roundAmount_shouldTrigger_forExactlyRoundAmount() {
        RoundAmountRule rule = new RoundAmountRule();
        Transaction tx = buildTx();
        tx.setAmount(new BigDecimal("5000.00"));
        MonitoringRule mr = buildRule("ROUND_AMOUNT", Map.of("divisor", 1000, "minAmount", 1000));
        assertThat(rule.evaluate(tx, mr)).isPresent();
    }

    @Test
    void roundAmount_shouldNotTrigger_forNonRoundAmount() {
        RoundAmountRule rule = new RoundAmountRule();
        Transaction tx = buildTx();
        tx.setAmount(new BigDecimal("5123.45"));
        MonitoringRule mr = buildRule("ROUND_AMOUNT", Map.of("divisor", 1000, "minAmount", 1000));
        assertThat(rule.evaluate(tx, mr)).isEmpty();
    }

    // ── ODD_HOURS Rule ──────────────────────────────────────────────────────────

    @Test
    void oddHours_shouldTrigger_atMidnight() {
        OddHoursRule rule = new OddHoursRule();
        Transaction tx = buildTx();
        tx.setCreatedAt(LocalDateTime.now().withHour(2).withMinute(30));
        MonitoringRule mr = buildRule("ODD_HOURS", Map.of("startHour", 0, "endHour", 4));
        assertThat(rule.evaluate(tx, mr)).isPresent();
    }

    @Test
    void oddHours_shouldNotTrigger_duringBusinessHours() {
        OddHoursRule rule = new OddHoursRule();
        Transaction tx = buildTx();
        tx.setCreatedAt(LocalDateTime.now().withHour(10).withMinute(0));
        MonitoringRule mr = buildRule("ODD_HOURS", Map.of("startHour", 0, "endHour", 4));
        assertThat(rule.evaluate(tx, mr)).isEmpty();
    }

    // ── RAPID_TRANSACTIONS Rule ─────────────────────────────────────────────────

    @Test
    void rapidTransactions_shouldTrigger_whenCountExceedsMax() {
        when(transactionRepository.countRecentTransactionsByAccount(eq("ACC-123"), any()))
                .thenReturn(5L); // already 5 in window

        RapidTransactionsRule rule = new RapidTransactionsRule(transactionRepository);
        Transaction tx = buildTx();
        MonitoringRule mr = buildRule("RAPID_TRANSACTIONS", Map.of("maxCount", 5, "windowMinutes", 10));
        assertThat(rule.evaluate(tx, mr)).isPresent();
    }

    @Test
    void rapidTransactions_shouldNotTrigger_whenCountBelowMax() {
        when(transactionRepository.countRecentTransactionsByAccount(eq("ACC-123"), any()))
                .thenReturn(2L);

        RapidTransactionsRule rule = new RapidTransactionsRule(transactionRepository);
        Transaction tx = buildTx();
        MonitoringRule mr = buildRule("RAPID_TRANSACTIONS", Map.of("maxCount", 5, "windowMinutes", 10));
        assertThat(rule.evaluate(tx, mr)).isEmpty();
    }
}

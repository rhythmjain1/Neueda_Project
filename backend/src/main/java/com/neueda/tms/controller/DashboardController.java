package com.neueda.tms.controller;

import com.neueda.tms.dto.AlertDTO;
import com.neueda.tms.dto.DashboardStatsDTO;
import com.neueda.tms.repository.AlertRepository;
import com.neueda.tms.repository.TransactionRepository;
import com.neueda.tms.service.AlertService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AlertService alertService;
    private final AlertRepository alertRepository;
    private final TransactionRepository transactionRepository;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        AlertDTO.StatsResponse alertStats = alertService.getStats();

        long totalTx = transactionRepository.countAll();
        long txLast24h = transactionRepository.countTransactionsSince(LocalDateTime.now().minusHours(24));

        // Chart: alerts by status
        List<Object[]> statusData = alertRepository.countGroupByStatus();
        List<Map<String, Object>> alertsByStatus = new ArrayList<>();
        for (Object[] row : statusData) {
            alertsByStatus.add(Map.of("status", row[0].toString(), "count", row[1]));
        }

        return ResponseEntity.ok(DashboardStatsDTO.builder()
                .totalAlerts(alertStats.getTotalAlerts())
                .openAlerts(alertStats.getOpenAlerts())
                .forwardedAlerts(alertStats.getForwardedAlerts())
                .dismissedAlerts(alertStats.getDismissedAlerts())
                .closedAlerts(alertStats.getClosedAlerts())
                .percentageForwarded(alertStats.getPercentageForwarded())
                .totalTransactions(totalTx)
                .transactionsLast24h(txLast24h)
                .alertsLast24h(alertStats.getAlertsLast24h())
                .alertsLast7d(alertStats.getAlertsLast7d())
                .alertsByStatus(alertsByStatus)
                .build());
    }
}

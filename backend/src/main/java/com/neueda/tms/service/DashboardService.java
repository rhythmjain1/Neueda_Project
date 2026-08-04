package com.neueda.tms.service;

import com.neueda.tms.dto.AlertDTO;
import com.neueda.tms.dto.DashboardStatsDTO;
import com.neueda.tms.repository.AlertRepository;
import com.neueda.tms.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service layer for Dashboard statistics.
 * Centralises dashboard logic so the controller does not touch repositories directly.
 */
@Service
public class DashboardService implements IDashboardService {

    private final AlertService alertService;
    private final AlertRepository alertRepository;
    private final TransactionRepository transactionRepository;

    @Autowired
    public DashboardService(AlertService alertService,
                            AlertRepository alertRepository,
                            TransactionRepository transactionRepository) {
        this.alertService = alertService;
        this.alertRepository = alertRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStatsDTO getDashboardStats() {
        AlertDTO.StatsResponse alertStats = alertService.getStats();

        long totalTx = transactionRepository.countAll();
        long txLast24h = transactionRepository.countTransactionsSince(
                LocalDateTime.now().minusHours(24));

        List<Map<String, Object>> alertsByStatus = alertRepository.countGroupByStatus();

        return DashboardStatsDTO.builder()
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
                .build();
    }
}

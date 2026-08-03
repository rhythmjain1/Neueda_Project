package com.neueda.tms.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    // Alerts
    private long totalAlerts;
    private long openAlerts;
    private long forwardedAlerts;
    private long dismissedAlerts;
    private long closedAlerts;
    private double percentageForwarded;

    // Transactions
    private long totalTransactions;
    private long transactionsLast24h;

    // Recent activity
    private long alertsLast24h;
    private long alertsLast7d;

    // Chart data
    private List<Map<String, Object>> alertsByStatus;
    private List<Map<String, Object>> alertsByRule;
}

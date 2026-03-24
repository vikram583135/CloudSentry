package com.devops.platform.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for Admin dashboard view - complete system overview.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboard {

    // System Health
    private SystemHealth systemHealth;

    // User Statistics
    private UserStats userStats;

    // Cost Overview
    private CostOverview costOverview;

    // Incident Summary
    private IncidentSummary incidentSummary;

    // Anomaly Summary
    private AnomalySummary anomalySummary;

    // Resource Summary
    private ResourceSummary resourceSummary;

    // Recent Activity
    private List<ActivityItem> recentActivity;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SystemHealth {
        private String overallStatus; // HEALTHY, DEGRADED, CRITICAL
        private Integer healthScore; // 0-100
        private Long totalApplications;
        private Long healthyApplications;
        private Long degradedApplications;
        private Long criticalApplications;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserStats {
        private Long totalUsers;
        private Long activeUsers;
        private Map<String, Long> usersByRole;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostOverview {
        private BigDecimal totalMonthlyCost;
        private Double costChangePercent;
        private BigDecimal potentialSavings;
        private BigDecimal realizedSavings;
        private Map<String, BigDecimal> costByProvider;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentSummary {
        private Long openIncidents;
        private Long criticalIncidents;
        private Long resolvedToday;
        private Double avgResolutionTimeHours;
        private Map<String, Long> incidentsBySeverity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalySummary {
        private Long openAnomalies;
        private Long detectedToday;
        private Long resolvedToday;
        private Map<String, Long> anomaliesBySeverity;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceSummary {
        private Long totalResources;
        private Long activeResources;
        private Long idleResources;
        private Long underutilizedResources;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActivityItem {
        private String timestamp;
        private String type;
        private String description;
        private String severity;
        private String actor;
    }
}

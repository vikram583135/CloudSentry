package com.devops.platform.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for Manager dashboard view - focused on costs, trends, and team
 * performance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ManagerDashboard {

    // Cost Summary
    private CostSummary costSummary;

    // Team Performance
    private TeamPerformance teamPerformance;

    // Incident Trends
    private IncidentTrends incidentTrends;

    // Top Cost Recommendations
    private List<RecommendationCard> topRecommendations;

    // Resource Allocation
    private ResourceAllocation resourceAllocation;

    // Compliance Overview
    private ComplianceOverview complianceOverview;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostSummary {
        private BigDecimal totalMonthlyCost;
        private BigDecimal previousMonthCost;
        private Double changePercent;
        private BigDecimal projectedAnnualCost;
        private BigDecimal potentialSavings;
        private BigDecimal realizedSavingsYtd;
        private Map<String, BigDecimal> costByTeam;
        private Map<String, BigDecimal> costByEnvironment;
        private List<CostTrendPoint> costTrend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostTrendPoint {
        private String date;
        private BigDecimal cost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeamPerformance {
        private Double avgMttr; // Mean Time To Resolve
        private Double mttrChange; // Compared to previous period
        private Long incidentsResolved;
        private Long slaBreaches;
        private Double slaCompliancePercent;
        private Map<String, Long> incidentsByTeam;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentTrends {
        private Long totalIncidentsThisMonth;
        private Long totalIncidentsLastMonth;
        private Double changePercent;
        private Map<String, Long> incidentsBySeverity;
        private List<TrendPoint> weeklyTrend;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String week;
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendationCard {
        private String id;
        private String title;
        private String type;
        private BigDecimal estimatedSavings;
        private String effortLevel;
        private String riskLevel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResourceAllocation {
        private Long totalResources;
        private Map<String, Long> resourcesByProvider;
        private Map<String, Long> resourcesByType;
        private Map<String, BigDecimal> costByProvider;
        private Long idleResources;
        private BigDecimal idleResourceCost;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceOverview {
        private Double overallScore; // 0-100
        private Long totalChecks;
        private Long passedChecks;
        private Long failedChecks;
        private List<ComplianceItem> criticalFindings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ComplianceItem {
        private String category;
        private String description;
        private String severity;
        private String recommendation;
    }
}

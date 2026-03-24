package com.devops.platform.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for Developer dashboard view - focused on application health and metrics.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperDashboard {

    // My Applications
    private List<ApplicationCard> myApplications;

    // Recent Anomalies (for my apps)
    private List<AnomalyCard> recentAnomalies;

    // Recent Incidents (for my apps)
    private List<IncidentCard> recentIncidents;

    // Performance Metrics
    private PerformanceMetrics performanceMetrics;

    // Deployment Status
    private List<DeploymentCard> recentDeployments;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApplicationCard {
        private String id;
        private String name;
        private String environment;
        private String status;
        private Double cpuUtilization;
        private Double memoryUtilization;
        private Long errorCountToday;
        private Double avgResponseTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyCard {
        private String id;
        private String applicationName;
        private String metricType;
        private String severity;
        private String status;
        private Double currentValue;
        private String detectedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IncidentCard {
        private String id;
        private String incidentNumber;
        private String title;
        private String severity;
        private String status;
        private String applicationName;
        private String createdAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PerformanceMetrics {
        private Map<String, Double> avgResponseTimeByApp;
        private Map<String, Long> errorCountByApp;
        private Map<String, Double> throughputByApp;
        private List<MetricTrend> trends;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetricTrend {
        private String timestamp;
        private Double responseTime;
        private Long requestCount;
        private Long errorCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DeploymentCard {
        private String id;
        private String applicationName;
        private String version;
        private String status; // PENDING, IN_PROGRESS, SUCCESS, FAILED, ROLLED_BACK
        private String deployedBy;
        private String deployedAt;
    }
}

package com.devops.platform.dashboard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * DTO for SRE dashboard view - focused on reliability and incidents.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SreDashboard {

    // Active Incidents
    private List<IncidentCard> activeIncidents;

    // Anomaly Overview
    private AnomalyOverview anomalyOverview;

    // SLA Metrics
    private SlaMetrics slaMetrics;

    // On-Call Info
    private OnCallInfo onCallInfo;

    // Service Health
    private List<ServiceHealth> serviceHealth;

    // Alert Summary
    private AlertSummary alertSummary;

    // Recent RCA Results
    private List<RcaCard> recentRcaResults;

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
        private String assignee;
        private String createdAt;
        private Long durationMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AnomalyOverview {
        private Long openCount;
        private Long criticalCount;
        private Long highCount;
        private Long last24hDetected;
        private Map<String, Long> byMetricType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SlaMetrics {
        private Double uptimePercent;
        private Double mttrHours; // Mean Time To Resolve
        private Double mttaMinutes; // Mean Time To Acknowledge
        private Long incidentsWithinSla;
        private Long incidentsBreachedSla;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OnCallInfo {
        private String currentOnCall;
        private String nextOnCall;
        private String rotationEndTime;
        private Integer pendingEscalations;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ServiceHealth {
        private String applicationId;
        private String applicationName;
        private String status; // HEALTHY, DEGRADED, DOWN
        private Double cpuUtilization;
        private Double memoryUtilization;
        private Long errorCount;
        private Double latencyP99;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AlertSummary {
        private Long pendingAlerts;
        private Long acknowledgedAlerts;
        private Long silencedAlerts;
        private Map<String, Long> alertsByChannel;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RcaCard {
        private String id;
        private String incidentNumber;
        private String patternType;
        private String confidenceLevel;
        private String summary;
        private String createdAt;
    }
}

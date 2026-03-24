package com.devops.platform.dashboard.service;

import com.devops.platform.analyzer.model.AnomalyStatus;
import com.devops.platform.analyzer.repository.AnomalyRepository;
import com.devops.platform.dashboard.dto.DeveloperDashboard;
import com.devops.platform.incident.model.IncidentStatus;
import com.devops.platform.incident.repository.IncidentRepository;
import com.devops.platform.metrics.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Developer dashboard data aggregation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DeveloperDashboardService {

    private final ApplicationRepository applicationRepository;
    private final AnomalyRepository anomalyRepository;
    private final IncidentRepository incidentRepository;

    public DeveloperDashboard getDashboard(UUID userId) {
        return DeveloperDashboard.builder()
                .myApplications(getMyApplications(userId))
                .recentAnomalies(getRecentAnomalies(userId))
                .recentIncidents(getRecentIncidents(userId))
                .performanceMetrics(getPerformanceMetrics(userId))
                .recentDeployments(getRecentDeployments(userId))
                .build();
    }

    private List<DeveloperDashboard.ApplicationCard> getMyApplications(UUID userId) {
        // Get applications owned by or assigned to user
        return applicationRepository.findByActiveTrue()
                .stream()
                .limit(10)
                .map(app -> DeveloperDashboard.ApplicationCard.builder()
                        .id(app.getId().toString())
                        .name(app.getName())
                        .environment(app.getEnvironment())
                        .status("HEALTHY")
                        .cpuUtilization(0.0)
                        .memoryUtilization(0.0)
                        .errorCountToday(0L)
                        .avgResponseTime(0.0)
                        .build())
                .collect(Collectors.toList());
    }

    private List<DeveloperDashboard.AnomalyCard> getRecentAnomalies(UUID userId) {
        return anomalyRepository.findByStatusOrderByDetectedAtDesc(
                AnomalyStatus.OPEN, PageRequest.of(0, 10))
                .stream()
                .map(anomaly -> DeveloperDashboard.AnomalyCard.builder()
                        .id(anomaly.getId().toString())
                        .applicationName(anomaly.getApplicationName())
                        .metricType(anomaly.getMetricType().name())
                        .severity(anomaly.getSeverity().name())
                        .status(anomaly.getStatus().name())
                        .currentValue(anomaly.getCurrentValue())
                        .detectedAt(anomaly.getDetectedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private List<DeveloperDashboard.IncidentCard> getRecentIncidents(UUID userId) {
        return incidentRepository.findByStatusNotOrderByCreatedAtDesc(
                IncidentStatus.CLOSED, PageRequest.of(0, 10))
                .stream()
                .map(incident -> DeveloperDashboard.IncidentCard.builder()
                        .id(incident.getId().toString())
                        .incidentNumber(incident.getIncidentNumber())
                        .title(incident.getTitle())
                        .severity(incident.getSeverity().name())
                        .status(incident.getStatus().name())
                        .applicationName(incident.getApplicationName())
                        .createdAt(incident.getCreatedAt().toString())
                        .build())
                .collect(Collectors.toList());
    }

    private DeveloperDashboard.PerformanceMetrics getPerformanceMetrics(UUID userId) {
        // Placeholder - would aggregate from metrics service
        return DeveloperDashboard.PerformanceMetrics.builder()
                .avgResponseTimeByApp(new HashMap<>())
                .errorCountByApp(new HashMap<>())
                .throughputByApp(new HashMap<>())
                .trends(new ArrayList<>())
                .build();
    }

    private List<DeveloperDashboard.DeploymentCard> getRecentDeployments(UUID userId) {
        // Placeholder - would integrate with CI/CD system
        return new ArrayList<>();
    }
}

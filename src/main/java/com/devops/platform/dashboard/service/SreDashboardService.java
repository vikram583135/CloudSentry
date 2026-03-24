package com.devops.platform.dashboard.service;

import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.AnomalyStatus;
import com.devops.platform.analyzer.repository.AnomalyRepository;
import com.devops.platform.dashboard.dto.SreDashboard;
import com.devops.platform.incident.model.IncidentStatus;
import com.devops.platform.incident.repository.IncidentRepository;
import com.devops.platform.metrics.repository.ApplicationRepository;
import com.devops.platform.notification.model.DeliveryStatus;
import com.devops.platform.notification.repository.NotificationRepository;
import com.devops.platform.rca.repository.RcaResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for SRE dashboard data aggregation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SreDashboardService {

        private final IncidentRepository incidentRepository;
        private final AnomalyRepository anomalyRepository;
        private final ApplicationRepository applicationRepository;
        private final NotificationRepository notificationRepository;
        private final RcaResultRepository rcaResultRepository;

        public SreDashboard getDashboard() {
                return SreDashboard.builder()
                                .activeIncidents(getActiveIncidents())
                                .anomalyOverview(getAnomalyOverview())
                                .slaMetrics(getSlaMetrics())
                                .onCallInfo(getOnCallInfo())
                                .serviceHealth(getServiceHealth())
                                .alertSummary(getAlertSummary())
                                .recentRcaResults(getRecentRcaResults())
                                .build();
        }

        private List<SreDashboard.IncidentCard> getActiveIncidents() {
                List<SreDashboard.IncidentCard> cards = new ArrayList<>();
                incidentRepository.findByStatusNotOrderByCreatedAtDesc(
                                IncidentStatus.CLOSED, PageRequest.of(0, 10))
                                .forEach(incident -> cards.add(SreDashboard.IncidentCard.builder()
                                                .id(incident.getId().toString())
                                                .incidentNumber(incident.getIncidentNumber())
                                                .title(incident.getTitle())
                                                .severity(incident.getSeverity().name())
                                                .status(incident.getStatus().name())
                                                .applicationName(incident.getApplicationName())
                                                .assignee(incident.getAssignedTeam() != null
                                                                ? incident.getAssignedTeam()
                                                                : "Unassigned")
                                                .createdAt(incident.getCreatedAt().toString())
                                                .durationMinutes(
                                                                Duration.between(incident.getCreatedAt(), Instant.now())
                                                                                .toMinutes())
                                                .build()));
                return cards;
        }

        private SreDashboard.AnomalyOverview getAnomalyOverview() {
                long openCount = anomalyRepository.countByStatus(AnomalyStatus.OPEN);
                long criticalCount = anomalyRepository.countBySeverityAndStatus(AnomalySeverity.CRITICAL,
                                AnomalyStatus.OPEN);
                long highCount = anomalyRepository.countBySeverityAndStatus(AnomalySeverity.HIGH, AnomalyStatus.OPEN);

                Instant last24h = Instant.now().minus(24, ChronoUnit.HOURS);
                long last24hDetected = anomalyRepository.countByDetectedAtAfter(last24h);

                Map<String, Long> byMetricType = new HashMap<>();
                // Simplified - would need custom query for full implementation

                return SreDashboard.AnomalyOverview.builder()
                                .openCount(openCount)
                                .criticalCount(criticalCount)
                                .highCount(highCount)
                                .last24hDetected(last24hDetected)
                                .byMetricType(byMetricType)
                                .build();
        }

        private SreDashboard.SlaMetrics getSlaMetrics() {
                // Calculate metrics from resolved incidents
                Double avgTtr = incidentRepository.getAverageTimeToResolve();
                Double avgTta = incidentRepository.getAverageTimeToAcknowledge();

                return SreDashboard.SlaMetrics.builder()
                                .uptimePercent(99.9)
                                .mttrHours(avgTtr != null ? avgTtr / 60 : 0.0)
                                .mttaMinutes(avgTta != null ? avgTta : 0.0)
                                .incidentsWithinSla(0L)
                                .incidentsBreachedSla(0L)
                                .build();
        }

        private SreDashboard.OnCallInfo getOnCallInfo() {
                // Placeholder - would integrate with PagerDuty/OpsGenie
                return SreDashboard.OnCallInfo.builder()
                                .currentOnCall("SRE Team")
                                .nextOnCall("SRE Team")
                                .rotationEndTime(Instant.now().plus(8, ChronoUnit.HOURS).toString())
                                .pendingEscalations(0)
                                .build();
        }

        private List<SreDashboard.ServiceHealth> getServiceHealth() {
                return applicationRepository.findByActiveTrue()
                                .stream()
                                .limit(10)
                                .map(app -> SreDashboard.ServiceHealth.builder()
                                                .applicationId(app.getId().toString())
                                                .applicationName(app.getName())
                                                .status("HEALTHY")
                                                .cpuUtilization(0.0)
                                                .memoryUtilization(0.0)
                                                .errorCount(0L)
                                                .latencyP99(0.0)
                                                .build())
                                .collect(Collectors.toList());
        }

        private SreDashboard.AlertSummary getAlertSummary() {
                long pending = notificationRepository.countByDeliveryStatus(DeliveryStatus.PENDING);
                long sent = notificationRepository.countByDeliveryStatus(DeliveryStatus.SENT);
                long failed = notificationRepository.countByDeliveryStatus(DeliveryStatus.FAILED);

                return SreDashboard.AlertSummary.builder()
                                .pendingAlerts(pending)
                                .acknowledgedAlerts(sent)
                                .silencedAlerts(0L)
                                .alertsByChannel(new HashMap<>())
                                .build();
        }

        private List<SreDashboard.RcaCard> getRecentRcaResults() {
                return rcaResultRepository.findAll(PageRequest.of(0, 5))
                                .stream()
                                .map(rca -> SreDashboard.RcaCard.builder()
                                                .id(rca.getId().toString())
                                                .incidentNumber(rca.getIncidentId() != null
                                                                ? rca.getIncidentId().toString()
                                                                : "N/A")
                                                .patternType(rca.getPatternType() != null ? rca.getPatternType().name()
                                                                : "UNKNOWN")
                                                .confidenceLevel(rca.getConfidenceLevel().name())
                                                .summary(rca.getRootCauseSummary())
                                                .createdAt(rca.getCreatedAt().toString())
                                                .build())
                                .collect(Collectors.toList());
        }
}

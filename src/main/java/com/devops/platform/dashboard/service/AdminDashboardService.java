package com.devops.platform.dashboard.service;

import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.AnomalyStatus;
import com.devops.platform.analyzer.repository.AnomalyRepository;
import com.devops.platform.auth.repository.UserRepository;
import com.devops.platform.cost.model.ResourceStatus;
import com.devops.platform.cost.repository.CloudResourceRepository;
import com.devops.platform.cost.repository.CostRecommendationRepository;
import com.devops.platform.dashboard.dto.AdminDashboard;
import com.devops.platform.incident.model.IncidentSeverity;
import com.devops.platform.incident.model.IncidentStatus;
import com.devops.platform.incident.repository.IncidentRepository;
import com.devops.platform.metrics.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Service for Admin dashboard data aggregation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final IncidentRepository incidentRepository;
    private final AnomalyRepository anomalyRepository;
    private final CloudResourceRepository resourceRepository;
    private final CostRecommendationRepository recommendationRepository;

    public AdminDashboard getDashboard() {
        return AdminDashboard.builder()
                .systemHealth(getSystemHealth())
                .userStats(getUserStats())
                .costOverview(getCostOverview())
                .incidentSummary(getIncidentSummary())
                .anomalySummary(getAnomalySummary())
                .resourceSummary(getResourceSummary())
                .recentActivity(getRecentActivity())
                .build();
    }

    private AdminDashboard.SystemHealth getSystemHealth() {
        long totalApps = applicationRepository.count();
        long activeApps = applicationRepository.countByActiveTrue();

        // Calculate health score based on incidents and anomalies
        long criticalIncidents = incidentRepository.countByStatusNotAndSeverity(
                IncidentStatus.CLOSED, IncidentSeverity.SEV1);
        long criticalAnomalies = anomalyRepository.countBySeverityAndStatus(
                AnomalySeverity.CRITICAL, AnomalyStatus.OPEN);

        String status = "HEALTHY";
        int healthScore = 100;

        if (criticalIncidents > 0 || criticalAnomalies > 2) {
            status = "CRITICAL";
            healthScore = 40;
        } else if (criticalAnomalies > 0) {
            status = "DEGRADED";
            healthScore = 70;
        }

        return AdminDashboard.SystemHealth.builder()
                .overallStatus(status)
                .healthScore(healthScore)
                .totalApplications(totalApps)
                .healthyApplications(activeApps)
                .degradedApplications(0L)
                .criticalApplications(criticalIncidents)
                .build();
    }

    private AdminDashboard.UserStats getUserStats() {
        long totalUsers = userRepository.count();

        Map<String, Long> byRole = new HashMap<>();
        byRole.put("ADMIN", userRepository.countByRole("ADMIN"));
        byRole.put("SRE", userRepository.countByRole("SRE"));
        byRole.put("DEVELOPER", userRepository.countByRole("DEVELOPER"));
        byRole.put("MANAGER", userRepository.countByRole("MANAGER"));

        return AdminDashboard.UserStats.builder()
                .totalUsers(totalUsers)
                .activeUsers(totalUsers)
                .usersByRole(byRole)
                .build();
    }

    private AdminDashboard.CostOverview getCostOverview() {
        BigDecimal totalCost = resourceRepository.getTotalMonthlyCost();
        if (totalCost == null)
            totalCost = BigDecimal.ZERO;

        BigDecimal potentialSavings = recommendationRepository.getTotalPotentialSavings();
        if (potentialSavings == null)
            potentialSavings = BigDecimal.ZERO;

        BigDecimal actualSavings = recommendationRepository.getTotalActualSavings();
        if (actualSavings == null)
            actualSavings = BigDecimal.ZERO;

        Map<String, BigDecimal> byProvider = new HashMap<>();
        for (Object[] row : resourceRepository.getCostByProvider()) {
            if (row[0] != null && row[1] != null) {
                byProvider.put(row[0].toString(), (BigDecimal) row[1]);
            }
        }

        return AdminDashboard.CostOverview.builder()
                .totalMonthlyCost(totalCost)
                .costChangePercent(0.0)
                .potentialSavings(potentialSavings)
                .realizedSavings(actualSavings)
                .costByProvider(byProvider)
                .build();
    }

    private AdminDashboard.IncidentSummary getIncidentSummary() {
        long openIncidents = incidentRepository.countByStatusNot(IncidentStatus.CLOSED);
        long criticalIncidents = incidentRepository.countByStatusNotAndSeverity(
                IncidentStatus.CLOSED, IncidentSeverity.SEV1);

        Instant todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        long resolvedToday = incidentRepository.countByStatusAndResolvedAtAfter(
                IncidentStatus.RESOLVED, todayStart);

        Map<String, Long> bySeverity = new HashMap<>();
        bySeverity.put("SEV1",
                incidentRepository.countByStatusNotAndSeverity(IncidentStatus.CLOSED, IncidentSeverity.SEV1));
        bySeverity.put("SEV2",
                incidentRepository.countByStatusNotAndSeverity(IncidentStatus.CLOSED, IncidentSeverity.SEV2));
        bySeverity.put("SEV3",
                incidentRepository.countByStatusNotAndSeverity(IncidentStatus.CLOSED, IncidentSeverity.SEV3));

        return AdminDashboard.IncidentSummary.builder()
                .openIncidents(openIncidents)
                .criticalIncidents(criticalIncidents)
                .resolvedToday(resolvedToday)
                .avgResolutionTimeHours(0.0)
                .incidentsBySeverity(bySeverity)
                .build();
    }

    private AdminDashboard.AnomalySummary getAnomalySummary() {
        long openAnomalies = anomalyRepository.countByStatus(AnomalyStatus.OPEN);

        Instant todayStart = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
        long detectedToday = anomalyRepository.countByDetectedAtAfter(todayStart);

        Map<String, Long> bySeverity = new HashMap<>();
        bySeverity.put("CRITICAL",
                anomalyRepository.countBySeverityAndStatus(AnomalySeverity.CRITICAL, AnomalyStatus.OPEN));
        bySeverity.put("HIGH", anomalyRepository.countBySeverityAndStatus(AnomalySeverity.HIGH, AnomalyStatus.OPEN));
        bySeverity.put("MEDIUM",
                anomalyRepository.countBySeverityAndStatus(AnomalySeverity.MEDIUM, AnomalyStatus.OPEN));

        return AdminDashboard.AnomalySummary.builder()
                .openAnomalies(openAnomalies)
                .detectedToday(detectedToday)
                .resolvedToday(0L)
                .anomaliesBySeverity(bySeverity)
                .build();
    }

    private AdminDashboard.ResourceSummary getResourceSummary() {
        long total = resourceRepository.count();
        long active = resourceRepository.countByStatus(ResourceStatus.ACTIVE);
        long idle = resourceRepository.countByStatus(ResourceStatus.IDLE);
        long underutilized = resourceRepository.countByStatus(ResourceStatus.UNDERUTILIZED);

        return AdminDashboard.ResourceSummary.builder()
                .totalResources(total)
                .activeResources(active)
                .idleResources(idle)
                .underutilizedResources(underutilized)
                .build();
    }

    private List<AdminDashboard.ActivityItem> getRecentActivity() {
        List<AdminDashboard.ActivityItem> activities = new ArrayList<>();

        // Add recent incidents
        incidentRepository.findTop5ByOrderByCreatedAtDesc()
                .forEach(incident -> activities.add(AdminDashboard.ActivityItem.builder()
                        .timestamp(incident.getCreatedAt().toString())
                        .type("INCIDENT")
                        .description("Incident created: " + incident.getTitle())
                        .severity(incident.getSeverity().name())
                        .actor("System")
                        .build()));

        // Sort by timestamp
        activities.sort((a, b) -> b.getTimestamp().compareTo(a.getTimestamp()));

        return activities.subList(0, Math.min(10, activities.size()));
    }
}

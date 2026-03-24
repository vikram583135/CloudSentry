package com.devops.platform.dashboard.service;

import com.devops.platform.cost.repository.CloudResourceRepository;
import com.devops.platform.cost.repository.CostRecommendationRepository;
import com.devops.platform.dashboard.dto.ManagerDashboard;
import com.devops.platform.incident.model.IncidentSeverity;
import com.devops.platform.incident.model.IncidentStatus;
import com.devops.platform.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for Manager dashboard data aggregation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerDashboardService {

    private final CloudResourceRepository resourceRepository;
    private final CostRecommendationRepository recommendationRepository;
    private final IncidentRepository incidentRepository;

    public ManagerDashboard getDashboard() {
        return ManagerDashboard.builder()
                .costSummary(getCostSummary())
                .teamPerformance(getTeamPerformance())
                .incidentTrends(getIncidentTrends())
                .topRecommendations(getTopRecommendations())
                .resourceAllocation(getResourceAllocation())
                .complianceOverview(getComplianceOverview())
                .build();
    }

    private ManagerDashboard.CostSummary getCostSummary() {
        BigDecimal totalCost = resourceRepository.getTotalMonthlyCost();
        if (totalCost == null)
            totalCost = BigDecimal.ZERO;

        BigDecimal potentialSavings = recommendationRepository.getTotalPotentialSavings();
        if (potentialSavings == null)
            potentialSavings = BigDecimal.ZERO;

        BigDecimal actualSavings = recommendationRepository.getTotalActualSavings();
        if (actualSavings == null)
            actualSavings = BigDecimal.ZERO;

        Map<String, BigDecimal> byTeam = new HashMap<>();
        for (Object[] row : resourceRepository.getCostByTeam()) {
            if (row[0] != null && row[1] != null) {
                byTeam.put((String) row[0], (BigDecimal) row[1]);
            }
        }

        Map<String, BigDecimal> byEnv = new HashMap<>();
        for (Object[] row : resourceRepository.getCostByEnvironment()) {
            if (row[0] != null && row[1] != null) {
                byEnv.put((String) row[0], (BigDecimal) row[1]);
            }
        }

        return ManagerDashboard.CostSummary.builder()
                .totalMonthlyCost(totalCost)
                .previousMonthCost(totalCost)
                .changePercent(0.0)
                .projectedAnnualCost(totalCost.multiply(BigDecimal.valueOf(12)))
                .potentialSavings(potentialSavings)
                .realizedSavingsYtd(actualSavings)
                .costByTeam(byTeam)
                .costByEnvironment(byEnv)
                .costTrend(new ArrayList<>())
                .build();
    }

    private ManagerDashboard.TeamPerformance getTeamPerformance() {
        Double avgMttr = incidentRepository.getAverageTimeToResolve();

        Instant monthStart = LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
        long resolvedThisMonth = incidentRepository.countByStatusAndResolvedAtAfter(
                IncidentStatus.RESOLVED, monthStart);

        return ManagerDashboard.TeamPerformance.builder()
                .avgMttr(avgMttr != null ? avgMttr / 60 : 0.0)
                .mttrChange(0.0)
                .incidentsResolved(resolvedThisMonth)
                .slaBreaches(0L)
                .slaCompliancePercent(95.0)
                .incidentsByTeam(new HashMap<>())
                .build();
    }

    private ManagerDashboard.IncidentTrends getIncidentTrends() {
        Instant monthStart = LocalDate.now().withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();
        Instant lastMonthStart = LocalDate.now().minusMonths(1).withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault()).toInstant();

        long thisMonth = incidentRepository.countByCreatedAtAfter(monthStart);
        long lastMonth = incidentRepository.countByCreatedAtBetween(lastMonthStart, monthStart);

        double changePercent = lastMonth > 0 ? ((double) (thisMonth - lastMonth) / lastMonth) * 100 : 0;

        Map<String, Long> bySeverity = new HashMap<>();
        bySeverity.put("SEV1",
                incidentRepository.countByStatusNotAndSeverity(IncidentStatus.CLOSED, IncidentSeverity.SEV1));
        bySeverity.put("SEV2",
                incidentRepository.countByStatusNotAndSeverity(IncidentStatus.CLOSED, IncidentSeverity.SEV2));
        bySeverity.put("SEV3",
                incidentRepository.countByStatusNotAndSeverity(IncidentStatus.CLOSED, IncidentSeverity.SEV3));

        return ManagerDashboard.IncidentTrends.builder()
                .totalIncidentsThisMonth(thisMonth)
                .totalIncidentsLastMonth(lastMonth)
                .changePercent(changePercent)
                .incidentsBySeverity(bySeverity)
                .weeklyTrend(new ArrayList<>())
                .build();
    }

    private List<ManagerDashboard.RecommendationCard> getTopRecommendations() {
        return recommendationRepository.findByStatusOrderByEstimatedSavingsDesc(
                "OPEN", PageRequest.of(0, 5))
                .stream()
                .map(rec -> ManagerDashboard.RecommendationCard.builder()
                        .id(rec.getId().toString())
                        .title(rec.getTitle())
                        .type(rec.getOptimizationType().name())
                        .estimatedSavings(rec.getEstimatedSavings())
                        .effortLevel(rec.getEffortLevel())
                        .riskLevel(rec.getRiskLevel())
                        .build())
                .collect(Collectors.toList());
    }

    private ManagerDashboard.ResourceAllocation getResourceAllocation() {
        long total = resourceRepository.count();

        Map<String, Long> byProvider = new HashMap<>();
        Map<String, Long> byType = new HashMap<>();
        Map<String, BigDecimal> costByProvider = new HashMap<>();

        for (Object[] row : resourceRepository.getCostByProvider()) {
            if (row[0] != null) {
                String provider = row[0].toString();
                costByProvider.put(provider, row[1] != null ? (BigDecimal) row[1] : BigDecimal.ZERO);
            }
        }

        long idle = resourceRepository.countByStatus(com.devops.platform.cost.model.ResourceStatus.IDLE);

        return ManagerDashboard.ResourceAllocation.builder()
                .totalResources(total)
                .resourcesByProvider(byProvider)
                .resourcesByType(byType)
                .costByProvider(costByProvider)
                .idleResources(idle)
                .idleResourceCost(BigDecimal.ZERO)
                .build();
    }

    private ManagerDashboard.ComplianceOverview getComplianceOverview() {
        // Placeholder - would integrate with compliance scanning tool
        return ManagerDashboard.ComplianceOverview.builder()
                .overallScore(95.0)
                .totalChecks(100L)
                .passedChecks(95L)
                .failedChecks(5L)
                .criticalFindings(new ArrayList<>())
                .build();
    }
}

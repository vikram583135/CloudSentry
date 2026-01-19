package com.devops.platform.cost.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for cost summary/dashboard data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostSummary {

    private BigDecimal totalMonthlyCost;
    private BigDecimal totalDailyCost;
    private BigDecimal monthToDateCost;
    private BigDecimal projectedMonthlyCost;
    private BigDecimal totalPotentialSavings;
    private BigDecimal totalActualSavings;
    private String currency;

    private Long totalResources;
    private Long idleResources;
    private Long underutilizedResources;

    private Long openRecommendations;
    private Long implementedRecommendations;

    private Map<String, BigDecimal> costByProvider;
    private Map<String, BigDecimal> costByResourceType;
    private Map<String, BigDecimal> costByTeam;
    private Map<String, BigDecimal> costByEnvironment;

    private List<CostTrendPoint> costTrend;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CostTrendPoint {
        private String date;
        private BigDecimal cost;
    }
}

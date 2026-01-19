package com.devops.platform.cost.dto;

import com.devops.platform.cost.model.OptimizationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for cost recommendation response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CostRecommendationResponse {

    private UUID id;
    private UUID resourceId;
    private UUID applicationId;
    private OptimizationType optimizationType;
    private String title;
    private String description;
    private BigDecimal currentCost;
    private BigDecimal projectedCost;
    private BigDecimal estimatedSavings;
    private Double savingsPercentage;
    private String currency;
    private String effortLevel;
    private String riskLevel;
    private List<String> implementationSteps;
    private String status;
    private Instant createdAt;
}

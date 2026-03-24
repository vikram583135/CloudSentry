package com.devops.platform.rca.dto;

import com.devops.platform.rca.model.ConfidenceLevel;
import com.devops.platform.rca.model.PatternType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for root cause analysis response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RcaResponse {

    private UUID id;
    private UUID incidentId;
    private UUID anomalyId;
    private UUID applicationId;
    private PatternType patternType;
    private ConfidenceLevel confidenceLevel;
    private Double confidenceScore;
    private String rootCauseSummary;
    private String detailedAnalysis;
    private List<String> suggestedActions;
    private UUID matchedRuleId;
    private String matchedRuleName;
    private List<UUID> similarIncidents;
    private Boolean isAiGenerated;
    private String aiModelUsed;
    private Instant createdAt;
}

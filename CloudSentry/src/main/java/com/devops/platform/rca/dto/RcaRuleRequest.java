package com.devops.platform.rca.dto;

import com.devops.platform.metrics.model.MetricType;
import com.devops.platform.rca.model.PatternType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for creating/updating RCA rules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RcaRuleRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Pattern type is required")
    private PatternType patternType;

    private MetricType metricType;

    private String conditionExpression;

    private Double thresholdValue;

    private String thresholdOperator;

    @Builder.Default
    private Integer timeWindowMinutes = 15;

    private String rootCauseTemplate;

    private List<String> suggestedActions;

    private String runbookUrl;

    @Builder.Default
    private Integer priority = 100;

    @Builder.Default
    private Boolean enabled = true;
}

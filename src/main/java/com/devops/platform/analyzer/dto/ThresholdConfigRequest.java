package com.devops.platform.analyzer.dto;

import com.devops.platform.metrics.model.MetricType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating/updating threshold configuration.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ThresholdConfigRequest {

    private UUID applicationId;

    @NotNull(message = "Metric type is required")
    private MetricType metricType;

    private String metricName;

    private Double warningThreshold;

    private Double criticalThreshold;

    private Double minThreshold;

    private Double maxThreshold;

    @Builder.Default
    private Double zScoreThreshold = 3.0;

    @Builder.Default
    private Integer movingAvgWindowMinutes = 15;

    @Builder.Default
    private Double deviationPercentageThreshold = 20.0;

    @Builder.Default
    private Integer minSamplesRequired = 10;

    @Builder.Default
    private Integer cooldownMinutes = 5;

    @Builder.Default
    private Boolean enabled = true;

    private String description;
}

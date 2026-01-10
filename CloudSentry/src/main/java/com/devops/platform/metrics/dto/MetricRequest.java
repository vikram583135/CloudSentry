package com.devops.platform.metrics.dto;

import com.devops.platform.metrics.model.MetricType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for submitting a single metric data point.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricRequest {

    @NotNull(message = "Application ID is required")
    private UUID applicationId;

    private String applicationName;

    @NotNull(message = "Metric type is required")
    private MetricType metricType;

    private String metricName;

    @NotNull(message = "Value is required")
    private Double value;

    private String unit;

    private String host;

    private String environment;

    private Map<String, String> tags;

    private Instant timestamp;
}

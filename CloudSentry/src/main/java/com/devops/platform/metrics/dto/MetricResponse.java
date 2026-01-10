package com.devops.platform.metrics.dto;

import com.devops.platform.metrics.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * DTO for returning metric data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricResponse {

    private UUID id;
    private UUID applicationId;
    private String applicationName;
    private MetricType metricType;
    private String metricName;
    private Double value;
    private String unit;
    private String host;
    private String environment;
    private Map<String, String> tags;
    private Instant timestamp;
    private Instant createdAt;
}

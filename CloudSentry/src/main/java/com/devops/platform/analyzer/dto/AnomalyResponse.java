package com.devops.platform.analyzer.dto;

import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.AnomalyStatus;
import com.devops.platform.analyzer.model.DetectionType;
import com.devops.platform.metrics.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for anomaly response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnomalyResponse {

    private UUID id;
    private UUID applicationId;
    private String applicationName;
    private MetricType metricType;
    private String metricName;
    private DetectionType detectionType;
    private AnomalySeverity severity;
    private AnomalyStatus status;
    private Double currentValue;
    private Double expectedValue;
    private Double thresholdValue;
    private Double deviationPercentage;
    private Double zScore;
    private String description;
    private String host;
    private String environment;
    private Instant detectedAt;
    private Instant resolvedAt;
    private UUID incidentId;
    private Instant createdAt;
}

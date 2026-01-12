package com.devops.platform.analyzer.dto;

import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.DetectionType;
import com.devops.platform.metrics.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Internal DTO for detection results.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionResult {

    private boolean anomalyDetected;
    private UUID applicationId;
    private String applicationName;
    private MetricType metricType;
    private String metricName;
    private DetectionType detectionType;
    private AnomalySeverity severity;
    private Double currentValue;
    private Double expectedValue;
    private Double thresholdValue;
    private Double deviationPercentage;
    private Double zScore;
    private String description;
    private String host;
    private String environment;
    private Instant timestamp;
}

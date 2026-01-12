package com.devops.platform.analyzer.detection;

import com.devops.platform.analyzer.dto.DetectionResult;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.DetectionType;
import com.devops.platform.analyzer.model.ThresholdConfig;
import com.devops.platform.metrics.model.Metric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Static threshold-based anomaly detector.
 */
@Component
@Slf4j
public class ThresholdDetector {

    /**
     * Detects anomalies based on static thresholds.
     */
    public DetectionResult detect(Metric metric, ThresholdConfig config) {
        Double value = metric.getValue();
        boolean anomalyDetected = false;
        AnomalySeverity severity = null;
        Double thresholdValue = null;
        String description = null;

        // Check critical threshold (upper bound)
        if (config.getCriticalThreshold() != null && value >= config.getCriticalThreshold()) {
            anomalyDetected = true;
            severity = AnomalySeverity.CRITICAL;
            thresholdValue = config.getCriticalThreshold();
            description = String.format("Value %.2f exceeds critical threshold %.2f",
                    value, config.getCriticalThreshold());
        }
        // Check warning threshold
        else if (config.getWarningThreshold() != null && value >= config.getWarningThreshold()) {
            anomalyDetected = true;
            severity = AnomalySeverity.HIGH;
            thresholdValue = config.getWarningThreshold();
            description = String.format("Value %.2f exceeds warning threshold %.2f",
                    value, config.getWarningThreshold());
        }
        // Check max threshold
        else if (config.getMaxThreshold() != null && value > config.getMaxThreshold()) {
            anomalyDetected = true;
            severity = AnomalySeverity.MEDIUM;
            thresholdValue = config.getMaxThreshold();
            description = String.format("Value %.2f exceeds max threshold %.2f",
                    value, config.getMaxThreshold());
        }
        // Check min threshold
        else if (config.getMinThreshold() != null && value < config.getMinThreshold()) {
            anomalyDetected = true;
            severity = AnomalySeverity.MEDIUM;
            thresholdValue = config.getMinThreshold();
            description = String.format("Value %.2f below min threshold %.2f",
                    value, config.getMinThreshold());
        }

        return DetectionResult.builder()
                .anomalyDetected(anomalyDetected)
                .applicationId(metric.getApplicationId())
                .applicationName(metric.getApplicationName())
                .metricType(metric.getMetricType())
                .metricName(metric.getMetricName())
                .detectionType(DetectionType.THRESHOLD)
                .severity(severity)
                .currentValue(value)
                .thresholdValue(thresholdValue)
                .description(description)
                .host(metric.getHost())
                .environment(metric.getEnvironment())
                .timestamp(metric.getTimestamp() != null ? metric.getTimestamp() : Instant.now())
                .build();
    }
}

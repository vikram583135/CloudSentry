package com.devops.platform.analyzer.detection;

import com.devops.platform.analyzer.dto.DetectionResult;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.DetectionType;
import com.devops.platform.analyzer.model.ThresholdConfig;
import com.devops.platform.metrics.model.Metric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Moving average based anomaly detector.
 */
@Component
@Slf4j
public class MovingAverageDetector {

    /**
     * Detects anomalies based on deviation from moving average.
     */
    public DetectionResult detect(Metric metric, List<Double> recentValues, ThresholdConfig config) {
        if (recentValues == null || recentValues.size() < config.getMinSamplesRequired()) {
            return DetectionResult.builder()
                    .anomalyDetected(false)
                    .applicationId(metric.getApplicationId())
                    .metricType(metric.getMetricType())
                    .detectionType(DetectionType.MOVING_AVERAGE)
                    .description("Insufficient data for moving average analysis")
                    .build();
        }

        double movingAvg = calculateMovingAverage(recentValues);
        double value = metric.getValue();

        // Calculate deviation percentage
        double deviationPercentage = movingAvg != 0
                ? Math.abs((value - movingAvg) / movingAvg) * 100
                : (value != 0 ? 100 : 0);

        double threshold = config.getDeviationPercentageThreshold() != null
                ? config.getDeviationPercentageThreshold()
                : 20.0;

        boolean anomalyDetected = deviationPercentage > threshold;
        AnomalySeverity severity = null;
        String description = null;

        if (anomalyDetected) {
            String direction = value > movingAvg ? "above" : "below";

            if (deviationPercentage > threshold * 3) {
                severity = AnomalySeverity.CRITICAL;
                description = String.format("Critical deviation: %.1f%% %s moving average (%.2f vs avg %.2f)",
                        deviationPercentage, direction, value, movingAvg);
            } else if (deviationPercentage > threshold * 2) {
                severity = AnomalySeverity.HIGH;
                description = String.format("High deviation: %.1f%% %s moving average (%.2f vs avg %.2f)",
                        deviationPercentage, direction, value, movingAvg);
            } else {
                severity = AnomalySeverity.MEDIUM;
                description = String.format("Moving average anomaly: %.1f%% %s average (%.2f vs avg %.2f)",
                        deviationPercentage, direction, value, movingAvg);
            }
        }

        return DetectionResult.builder()
                .anomalyDetected(anomalyDetected)
                .applicationId(metric.getApplicationId())
                .applicationName(metric.getApplicationName())
                .metricType(metric.getMetricType())
                .metricName(metric.getMetricName())
                .detectionType(DetectionType.MOVING_AVERAGE)
                .severity(severity)
                .currentValue(value)
                .expectedValue(movingAvg)
                .thresholdValue(threshold)
                .deviationPercentage(deviationPercentage)
                .description(description)
                .host(metric.getHost())
                .environment(metric.getEnvironment())
                .timestamp(metric.getTimestamp() != null ? metric.getTimestamp() : Instant.now())
                .build();
    }

    private double calculateMovingAverage(List<Double> values) {
        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}

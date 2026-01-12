package com.devops.platform.analyzer.detection;

import com.devops.platform.analyzer.dto.DetectionResult;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.DetectionType;
import com.devops.platform.metrics.model.Metric;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Rate of change (spike/drop) detector.
 */
@Component
@Slf4j
public class RateOfChangeDetector {

    private static final double DEFAULT_SPIKE_THRESHOLD = 50.0; // 50% change
    private static final double HIGH_SPIKE_THRESHOLD = 100.0; // 100% change
    private static final double CRITICAL_SPIKE_THRESHOLD = 200.0; // 200% change

    /**
     * Detects sudden changes (spikes or drops) in metric values.
     */
    public DetectionResult detect(Metric metric, Double previousValue, double spikeThreshold) {
        if (previousValue == null || previousValue == 0) {
            return DetectionResult.builder()
                    .anomalyDetected(false)
                    .applicationId(metric.getApplicationId())
                    .metricType(metric.getMetricType())
                    .detectionType(DetectionType.RATE_OF_CHANGE)
                    .description("No previous value available for rate of change analysis")
                    .build();
        }

        double value = metric.getValue();
        double changePercentage = ((value - previousValue) / Math.abs(previousValue)) * 100;
        double absChangePercentage = Math.abs(changePercentage);

        boolean anomalyDetected = absChangePercentage > spikeThreshold;
        AnomalySeverity severity = null;
        String description = null;

        if (anomalyDetected) {
            String direction = changePercentage > 0 ? "spike" : "drop";

            if (absChangePercentage > CRITICAL_SPIKE_THRESHOLD) {
                severity = AnomalySeverity.CRITICAL;
                description = String.format("Critical %s: %.1f%% change (%.2f -> %.2f)",
                        direction, changePercentage, previousValue, value);
            } else if (absChangePercentage > HIGH_SPIKE_THRESHOLD) {
                severity = AnomalySeverity.HIGH;
                description = String.format("Severe %s: %.1f%% change (%.2f -> %.2f)",
                        direction, changePercentage, previousValue, value);
            } else {
                severity = AnomalySeverity.MEDIUM;
                description = String.format("Sudden %s: %.1f%% change (%.2f -> %.2f)",
                        direction, changePercentage, previousValue, value);
            }
        }

        return DetectionResult.builder()
                .anomalyDetected(anomalyDetected)
                .applicationId(metric.getApplicationId())
                .applicationName(metric.getApplicationName())
                .metricType(metric.getMetricType())
                .metricName(metric.getMetricName())
                .detectionType(DetectionType.RATE_OF_CHANGE)
                .severity(severity)
                .currentValue(value)
                .expectedValue(previousValue)
                .thresholdValue(spikeThreshold)
                .deviationPercentage(changePercentage)
                .description(description)
                .host(metric.getHost())
                .environment(metric.getEnvironment())
                .timestamp(metric.getTimestamp() != null ? metric.getTimestamp() : Instant.now())
                .build();
    }

    /**
     * Detects rate of change using recent values for trend analysis.
     */
    public DetectionResult detectFromTrend(Metric metric, List<Double> recentValues, double spikeThreshold) {
        if (recentValues == null || recentValues.isEmpty()) {
            return detect(metric, null, spikeThreshold);
        }

        // Use the most recent value before current
        Double previousValue = recentValues.get(recentValues.size() - 1);
        return detect(metric, previousValue, spikeThreshold);
    }
}

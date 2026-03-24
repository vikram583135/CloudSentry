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
 * Statistical Z-score based anomaly detector.
 */
@Component
@Slf4j
public class ZScoreDetector {

    /**
     * Detects anomalies based on statistical z-score.
     */
    public DetectionResult detect(Metric metric, List<Double> historicalValues, ThresholdConfig config) {
        if (historicalValues == null || historicalValues.size() < config.getMinSamplesRequired()) {
            return DetectionResult.builder()
                    .anomalyDetected(false)
                    .applicationId(metric.getApplicationId())
                    .metricType(metric.getMetricType())
                    .detectionType(DetectionType.Z_SCORE)
                    .description("Insufficient historical data for z-score analysis")
                    .build();
        }

        double mean = calculateMean(historicalValues);
        double stdDev = calculateStdDev(historicalValues, mean);

        // Avoid division by zero
        if (stdDev == 0) {
            return DetectionResult.builder()
                    .anomalyDetected(false)
                    .applicationId(metric.getApplicationId())
                    .metricType(metric.getMetricType())
                    .detectionType(DetectionType.Z_SCORE)
                    .expectedValue(mean)
                    .description("Standard deviation is zero, cannot calculate z-score")
                    .build();
        }

        double value = metric.getValue();
        double zScore = (value - mean) / stdDev;
        double absZScore = Math.abs(zScore);

        double threshold = config.getZScoreThreshold() != null ? config.getZScoreThreshold() : 3.0;

        boolean anomalyDetected = absZScore > threshold;
        AnomalySeverity severity = null;
        String description = null;

        if (anomalyDetected) {
            if (absZScore > threshold * 2) {
                severity = AnomalySeverity.CRITICAL;
                description = String.format("Extreme deviation: z-score %.2f (threshold: %.2f)", zScore, threshold);
            } else if (absZScore > threshold * 1.5) {
                severity = AnomalySeverity.HIGH;
                description = String.format("High deviation: z-score %.2f (threshold: %.2f)", zScore, threshold);
            } else {
                severity = AnomalySeverity.MEDIUM;
                description = String.format("Statistical anomaly: z-score %.2f exceeds threshold %.2f", zScore,
                        threshold);
            }
        }

        double deviationPercentage = mean != 0 ? ((value - mean) / Math.abs(mean)) * 100 : 0;

        return DetectionResult.builder()
                .anomalyDetected(anomalyDetected)
                .applicationId(metric.getApplicationId())
                .applicationName(metric.getApplicationName())
                .metricType(metric.getMetricType())
                .metricName(metric.getMetricName())
                .detectionType(DetectionType.Z_SCORE)
                .severity(severity)
                .currentValue(value)
                .expectedValue(mean)
                .thresholdValue(threshold)
                .deviationPercentage(deviationPercentage)
                .zScore(zScore)
                .description(description)
                .host(metric.getHost())
                .environment(metric.getEnvironment())
                .timestamp(metric.getTimestamp() != null ? metric.getTimestamp() : Instant.now())
                .build();
    }

    private double calculateMean(List<Double> values) {
        return values.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }

    private double calculateStdDev(List<Double> values, double mean) {
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);
        return Math.sqrt(variance);
    }
}

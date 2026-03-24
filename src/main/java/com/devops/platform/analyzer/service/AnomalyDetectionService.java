package com.devops.platform.analyzer.service;

import com.devops.platform.analyzer.detection.*;
import com.devops.platform.analyzer.dto.AnomalyResponse;
import com.devops.platform.analyzer.dto.DetectionResult;
import com.devops.platform.analyzer.model.*;
import com.devops.platform.analyzer.repository.AnomalyRepository;
import com.devops.platform.analyzer.repository.ThresholdConfigRepository;
import com.devops.platform.metrics.model.Metric;
import com.devops.platform.metrics.model.MetricType;
import com.devops.platform.metrics.repository.MetricRepository;
import com.devops.platform.metrics.service.CustomMetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Core service for anomaly detection.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyDetectionService {

    private final AnomalyRepository anomalyRepository;
    private final ThresholdConfigRepository thresholdConfigRepository;
    private final MetricRepository metricRepository;
    private final ThresholdDetector thresholdDetector;
    private final ZScoreDetector zScoreDetector;
    private final MovingAverageDetector movingAverageDetector;
    private final RateOfChangeDetector rateOfChangeDetector;
    private final CustomMetricsService customMetricsService;

    /**
     * Analyzes a metric for anomalies using all applicable detection methods.
     */
    @Transactional
    public List<AnomalyResponse> analyzeMetric(Metric metric) {
        long startTime = System.nanoTime();
        List<AnomalyResponse> detectedAnomalies = new ArrayList<>();

        try {
            // Get threshold config for this metric
            ThresholdConfig config = getThresholdConfig(metric.getApplicationId(), metric.getMetricType());

            if (config == null || !config.getEnabled()) {
                log.debug("No enabled config for app={}, type={}",
                        metric.getApplicationId(), metric.getMetricType());
                return detectedAnomalies;
            }

            // Check cooldown - avoid duplicate anomalies in short timeframe
            if (isInCooldown(metric, config)) {
                log.debug("In cooldown period for app={}, type={}",
                        metric.getApplicationId(), metric.getMetricType());
                return detectedAnomalies;
            }

            // Get historical values for statistical analysis
            List<Double> historicalValues = getHistoricalValues(metric, config);

            // Run all detection algorithms
            List<DetectionResult> results = new ArrayList<>();

            // 1. Threshold-based detection
            results.add(thresholdDetector.detect(metric, config));

            // 2. Z-score detection
            if (historicalValues.size() >= config.getMinSamplesRequired()) {
                results.add(zScoreDetector.detect(metric, historicalValues, config));
            }

            // 3. Moving average detection
            if (historicalValues.size() >= config.getMinSamplesRequired()) {
                results.add(movingAverageDetector.detect(metric, historicalValues, config));
            }

            // 4. Rate of change detection
            if (!historicalValues.isEmpty()) {
                Double previousValue = historicalValues.get(historicalValues.size() - 1);
                results.add(rateOfChangeDetector.detect(metric, previousValue,
                        config.getDeviationPercentageThreshold()));
            }

            // Process and save detected anomalies
            for (DetectionResult result : results) {
                if (result.isAnomalyDetected()) {
                    Anomaly anomaly = saveAnomaly(result);
                    detectedAnomalies.add(toResponse(anomaly));
                    customMetricsService.incrementCounter("anomalies.detected");
                }
            }

        } catch (Exception e) {
            log.error("Error analyzing metric: {}", e.getMessage(), e);
            customMetricsService.incrementCounter("metrics.errors");
        } finally {
            long duration = System.nanoTime() - startTime;
            customMetricsService.recordTime("metrics.processing", duration, TimeUnit.NANOSECONDS);
        }

        return detectedAnomalies;
    }

    /**
     * Gets or creates a default threshold config.
     */
    private ThresholdConfig getThresholdConfig(UUID applicationId, MetricType metricType) {
        // Try app-specific config first
        Optional<ThresholdConfig> appConfig = thresholdConfigRepository
                .findByApplicationIdAndMetricType(applicationId, metricType);

        if (appConfig.isPresent()) {
            return appConfig.get();
        }

        // Fall back to global config
        return thresholdConfigRepository
                .findByApplicationIdIsNullAndMetricType(metricType)
                .orElseGet(() -> createDefaultConfig(metricType));
    }

    /**
     * Creates a default threshold config for a metric type.
     */
    private ThresholdConfig createDefaultConfig(MetricType metricType) {
        ThresholdConfig config = ThresholdConfig.builder()
                .metricType(metricType)
                .zScoreThreshold(3.0)
                .movingAvgWindowMinutes(15)
                .deviationPercentageThreshold(20.0)
                .minSamplesRequired(10)
                .cooldownMinutes(5)
                .enabled(true)
                .description("Auto-generated default config for " + metricType)
                .build();

        // Set default thresholds based on metric type
        switch (metricType) {
            case CPU_USAGE, MEMORY_USAGE, DISK_USAGE:
                config.setWarningThreshold(80.0);
                config.setCriticalThreshold(95.0);
                break;
            case ERROR_COUNT:
                config.setWarningThreshold(10.0);
                config.setCriticalThreshold(50.0);
                break;
            case RESPONSE_TIME, LATENCY_P95, LATENCY_P99:
                config.setWarningThreshold(1000.0); // 1 second
                config.setCriticalThreshold(5000.0); // 5 seconds
                break;
            default:
                // Use statistical methods only
                break;
        }

        return thresholdConfigRepository.save(config);
    }

    /**
     * Gets historical metric values for statistical analysis.
     */
    private List<Double> getHistoricalValues(Metric metric, ThresholdConfig config) {
        Instant now = Instant.now();
        Instant start = now.minus(config.getMovingAvgWindowMinutes(), ChronoUnit.MINUTES);

        List<Metric> historicalMetrics = metricRepository.findByApplicationIdAndMetricTypeAndTimeRange(
                metric.getApplicationId(),
                metric.getMetricType(),
                start,
                now,
                PageRequest.of(0, 100));

        return historicalMetrics.stream()
                .map(Metric::getValue)
                .collect(Collectors.toList());
    }

    /**
     * Checks if we're in a cooldown period to avoid duplicate anomalies.
     */
    private boolean isInCooldown(Metric metric, ThresholdConfig config) {
        Instant cooldownStart = Instant.now().minus(config.getCooldownMinutes(), ChronoUnit.MINUTES);

        return anomalyRepository.existsByApplicationIdAndMetricTypeAndStatusAndDetectedAtAfter(
                metric.getApplicationId(),
                metric.getMetricType(),
                AnomalyStatus.OPEN,
                cooldownStart);
    }

    /**
     * Saves a detected anomaly.
     */
    @Transactional
    protected Anomaly saveAnomaly(DetectionResult result) {
        Anomaly anomaly = Anomaly.builder()
                .applicationId(result.getApplicationId())
                .applicationName(result.getApplicationName())
                .metricType(result.getMetricType())
                .metricName(result.getMetricName())
                .detectionType(result.getDetectionType())
                .severity(result.getSeverity())
                .status(AnomalyStatus.OPEN)
                .currentValue(result.getCurrentValue())
                .expectedValue(result.getExpectedValue())
                .thresholdValue(result.getThresholdValue())
                .deviationPercentage(result.getDeviationPercentage())
                .zScore(result.getZScore())
                .description(result.getDescription())
                .host(result.getHost())
                .environment(result.getEnvironment())
                .detectedAt(result.getTimestamp() != null ? result.getTimestamp() : Instant.now())
                .build();

        anomaly = anomalyRepository.save(anomaly);
        log.info("Anomaly detected: id={}, app={}, type={}, severity={}",
                anomaly.getId(), anomaly.getApplicationId(),
                anomaly.getMetricType(), anomaly.getSeverity());

        return anomaly;
    }

    private AnomalyResponse toResponse(Anomaly anomaly) {
        return AnomalyResponse.builder()
                .id(anomaly.getId())
                .applicationId(anomaly.getApplicationId())
                .applicationName(anomaly.getApplicationName())
                .metricType(anomaly.getMetricType())
                .metricName(anomaly.getMetricName())
                .detectionType(anomaly.getDetectionType())
                .severity(anomaly.getSeverity())
                .status(anomaly.getStatus())
                .currentValue(anomaly.getCurrentValue())
                .expectedValue(anomaly.getExpectedValue())
                .thresholdValue(anomaly.getThresholdValue())
                .deviationPercentage(anomaly.getDeviationPercentage())
                .zScore(anomaly.getZScore())
                .description(anomaly.getDescription())
                .host(anomaly.getHost())
                .environment(anomaly.getEnvironment())
                .detectedAt(anomaly.getDetectedAt())
                .resolvedAt(anomaly.getResolvedAt())
                .incidentId(anomaly.getIncidentId())
                .createdAt(anomaly.getCreatedAt())
                .build();
    }
}

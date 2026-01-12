package com.devops.platform.analyzer.service;

import com.devops.platform.analyzer.dto.ThresholdConfigRequest;
import com.devops.platform.analyzer.model.ThresholdConfig;
import com.devops.platform.analyzer.repository.ThresholdConfigRepository;
import com.devops.platform.common.exception.BadRequestException;
import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.metrics.model.MetricType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Service for managing threshold configurations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ThresholdService {

    private final ThresholdConfigRepository thresholdConfigRepository;

    /**
     * Creates a new threshold configuration.
     */
    @Transactional
    @CacheEvict(value = "thresholds", allEntries = true)
    public ThresholdConfig createThreshold(ThresholdConfigRequest request) {
        // Check if config already exists
        if (request.getApplicationId() != null) {
            if (thresholdConfigRepository.existsByApplicationIdAndMetricType(
                    request.getApplicationId(), request.getMetricType())) {
                throw new BadRequestException("Threshold config already exists for this application and metric type");
            }
        }

        ThresholdConfig config = ThresholdConfig.builder()
                .applicationId(request.getApplicationId())
                .metricType(request.getMetricType())
                .metricName(request.getMetricName())
                .warningThreshold(request.getWarningThreshold())
                .criticalThreshold(request.getCriticalThreshold())
                .minThreshold(request.getMinThreshold())
                .maxThreshold(request.getMaxThreshold())
                .zScoreThreshold(request.getZScoreThreshold())
                .movingAvgWindowMinutes(request.getMovingAvgWindowMinutes())
                .deviationPercentageThreshold(request.getDeviationPercentageThreshold())
                .minSamplesRequired(request.getMinSamplesRequired())
                .cooldownMinutes(request.getCooldownMinutes())
                .enabled(request.getEnabled())
                .description(request.getDescription())
                .build();

        config = thresholdConfigRepository.save(config);
        log.info("Created threshold config: id={}, app={}, type={}",
                config.getId(), config.getApplicationId(), config.getMetricType());

        return config;
    }

    /**
     * Gets a threshold config by ID.
     */
    public ThresholdConfig getThreshold(UUID id) {
        return thresholdConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThresholdConfig", "id", id));
    }

    /**
     * Gets threshold configs for an application.
     */
    @Cacheable(value = "thresholds", key = "#applicationId")
    public List<ThresholdConfig> getThresholdsForApplication(UUID applicationId) {
        return thresholdConfigRepository.findByApplicationId(applicationId);
    }

    /**
     * Gets global threshold configs (not application-specific).
     */
    @Cacheable(value = "thresholds", key = "'global'")
    public List<ThresholdConfig> getGlobalThresholds() {
        return thresholdConfigRepository.findByApplicationIdIsNull();
    }

    /**
     * Gets all enabled threshold configs.
     */
    public List<ThresholdConfig> getEnabledThresholds() {
        return thresholdConfigRepository.findByEnabledTrue();
    }

    /**
     * Updates a threshold configuration.
     */
    @Transactional
    @CacheEvict(value = "thresholds", allEntries = true)
    public ThresholdConfig updateThreshold(UUID id, ThresholdConfigRequest request) {
        ThresholdConfig config = thresholdConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThresholdConfig", "id", id));

        config.setMetricName(request.getMetricName());
        config.setWarningThreshold(request.getWarningThreshold());
        config.setCriticalThreshold(request.getCriticalThreshold());
        config.setMinThreshold(request.getMinThreshold());
        config.setMaxThreshold(request.getMaxThreshold());
        config.setZScoreThreshold(request.getZScoreThreshold());
        config.setMovingAvgWindowMinutes(request.getMovingAvgWindowMinutes());
        config.setDeviationPercentageThreshold(request.getDeviationPercentageThreshold());
        config.setMinSamplesRequired(request.getMinSamplesRequired());
        config.setCooldownMinutes(request.getCooldownMinutes());
        config.setEnabled(request.getEnabled());
        config.setDescription(request.getDescription());

        config = thresholdConfigRepository.save(config);
        log.info("Updated threshold config: id={}", id);

        return config;
    }

    /**
     * Enables/disables a threshold configuration.
     */
    @Transactional
    @CacheEvict(value = "thresholds", allEntries = true)
    public ThresholdConfig setEnabled(UUID id, boolean enabled) {
        ThresholdConfig config = thresholdConfigRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ThresholdConfig", "id", id));

        config.setEnabled(enabled);
        config = thresholdConfigRepository.save(config);
        log.info("Threshold config {} {}", id, enabled ? "enabled" : "disabled");

        return config;
    }

    /**
     * Deletes a threshold configuration.
     */
    @Transactional
    @CacheEvict(value = "thresholds", allEntries = true)
    public void deleteThreshold(UUID id) {
        if (!thresholdConfigRepository.existsById(id)) {
            throw new ResourceNotFoundException("ThresholdConfig", "id", id);
        }
        thresholdConfigRepository.deleteById(id);
        log.info("Deleted threshold config: id={}", id);
    }

    /**
     * Creates default thresholds for all metric types.
     */
    @Transactional
    @CacheEvict(value = "thresholds", allEntries = true)
    public List<ThresholdConfig> createDefaultThresholds() {
        List<ThresholdConfig> defaults = List.of(
                createDefaultForType(MetricType.CPU_USAGE, 80.0, 95.0),
                createDefaultForType(MetricType.MEMORY_USAGE, 80.0, 95.0),
                createDefaultForType(MetricType.DISK_USAGE, 80.0, 95.0),
                createDefaultForType(MetricType.ERROR_COUNT, 10.0, 50.0),
                createDefaultForType(MetricType.RESPONSE_TIME, 1000.0, 5000.0),
                createDefaultForType(MetricType.LATENCY_P95, 500.0, 2000.0),
                createDefaultForType(MetricType.LATENCY_P99, 1000.0, 5000.0));

        return thresholdConfigRepository.saveAll(defaults);
    }

    private ThresholdConfig createDefaultForType(MetricType type, Double warning, Double critical) {
        return ThresholdConfig.builder()
                .metricType(type)
                .warningThreshold(warning)
                .criticalThreshold(critical)
                .zScoreThreshold(3.0)
                .movingAvgWindowMinutes(15)
                .deviationPercentageThreshold(20.0)
                .minSamplesRequired(10)
                .cooldownMinutes(5)
                .enabled(true)
                .description("Default threshold for " + type)
                .build();
    }
}

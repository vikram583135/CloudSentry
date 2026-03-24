package com.devops.platform.analyzer.repository;

import com.devops.platform.analyzer.model.ThresholdConfig;
import com.devops.platform.metrics.model.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for ThresholdConfig entities.
 */
@Repository
public interface ThresholdConfigRepository extends JpaRepository<ThresholdConfig, UUID> {

    Optional<ThresholdConfig> findByApplicationIdAndMetricType(UUID applicationId, MetricType metricType);

    Optional<ThresholdConfig> findByApplicationIdIsNullAndMetricType(MetricType metricType);

    List<ThresholdConfig> findByApplicationId(UUID applicationId);

    List<ThresholdConfig> findByApplicationIdIsNull();

    List<ThresholdConfig> findByEnabledTrue();

    List<ThresholdConfig> findByApplicationIdAndEnabledTrue(UUID applicationId);

    boolean existsByApplicationIdAndMetricType(UUID applicationId, MetricType metricType);
}

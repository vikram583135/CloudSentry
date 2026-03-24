package com.devops.platform.metrics.repository;

import com.devops.platform.metrics.model.Metric;
import com.devops.platform.metrics.model.MetricType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Metric entities.
 */
@Repository
public interface MetricRepository extends JpaRepository<Metric, UUID> {

    List<Metric> findByApplicationIdOrderByTimestampDesc(UUID applicationId, Pageable pageable);

    List<Metric> findByApplicationIdAndMetricType(UUID applicationId, MetricType metricType, Pageable pageable);

    @Query("SELECT m FROM Metric m WHERE m.applicationId = :appId AND m.timestamp BETWEEN :start AND :end ORDER BY m.timestamp DESC")
    List<Metric> findByApplicationIdAndTimeRange(
            @Param("appId") UUID applicationId,
            @Param("start") Instant startTime,
            @Param("end") Instant endTime,
            Pageable pageable);

    @Query("SELECT m FROM Metric m WHERE m.applicationId = :appId AND m.metricType = :type AND m.timestamp BETWEEN :start AND :end ORDER BY m.timestamp DESC")
    List<Metric> findByApplicationIdAndMetricTypeAndTimeRange(
            @Param("appId") UUID applicationId,
            @Param("type") MetricType metricType,
            @Param("start") Instant startTime,
            @Param("end") Instant endTime,
            Pageable pageable);

    @Query("SELECT AVG(m.value) FROM Metric m WHERE m.applicationId = :appId AND m.metricType = :type AND m.timestamp BETWEEN :start AND :end")
    Double getAverageValue(
            @Param("appId") UUID applicationId,
            @Param("type") MetricType metricType,
            @Param("start") Instant startTime,
            @Param("end") Instant endTime);

    @Query("SELECT MAX(m.value) FROM Metric m WHERE m.applicationId = :appId AND m.metricType = :type AND m.timestamp BETWEEN :start AND :end")
    Double getMaxValue(
            @Param("appId") UUID applicationId,
            @Param("type") MetricType metricType,
            @Param("start") Instant startTime,
            @Param("end") Instant endTime);

    @Query("SELECT MIN(m.value) FROM Metric m WHERE m.applicationId = :appId AND m.metricType = :type AND m.timestamp BETWEEN :start AND :end")
    Double getMinValue(
            @Param("appId") UUID applicationId,
            @Param("type") MetricType metricType,
            @Param("start") Instant startTime,
            @Param("end") Instant endTime);

    @Query("SELECT m FROM Metric m WHERE m.timestamp < :cutoff")
    List<Metric> findOldMetrics(@Param("cutoff") Instant cutoffTime, Pageable pageable);

    long countByApplicationId(UUID applicationId);

    void deleteByTimestampBefore(Instant cutoffTime);
}

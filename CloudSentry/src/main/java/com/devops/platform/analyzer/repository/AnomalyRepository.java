package com.devops.platform.analyzer.repository;

import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.AnomalyStatus;
import com.devops.platform.metrics.model.MetricType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Repository for Anomaly entities.
 */
@Repository
public interface AnomalyRepository extends JpaRepository<Anomaly, UUID> {

    List<Anomaly> findByApplicationIdAndStatusOrderByDetectedAtDesc(
            UUID applicationId, AnomalyStatus status, Pageable pageable);

    List<Anomaly> findByStatusOrderByDetectedAtDesc(AnomalyStatus status, Pageable pageable);

    List<Anomaly> findByStatusInOrderByDetectedAtDesc(List<AnomalyStatus> statuses, Pageable pageable);

    List<Anomaly> findBySeverityAndStatusOrderByDetectedAtDesc(
            AnomalySeverity severity, AnomalyStatus status, Pageable pageable);

    @Query("SELECT a FROM Anomaly a WHERE a.applicationId = :appId AND a.detectedAt BETWEEN :start AND :end ORDER BY a.detectedAt DESC")
    List<Anomaly> findByApplicationIdAndTimeRange(
            @Param("appId") UUID applicationId,
            @Param("start") Instant startTime,
            @Param("end") Instant endTime,
            Pageable pageable);

    @Query("SELECT a FROM Anomaly a WHERE a.applicationId = :appId AND a.metricType = :type AND a.status = :status AND a.detectedAt > :since")
    List<Anomaly> findRecentByApplicationAndMetricType(
            @Param("appId") UUID applicationId,
            @Param("type") MetricType metricType,
            @Param("status") AnomalyStatus status,
            @Param("since") Instant since);

    @Query("SELECT COUNT(a) FROM Anomaly a WHERE a.status IN :statuses")
    long countByStatusIn(@Param("statuses") List<AnomalyStatus> statuses);

    @Query("SELECT COUNT(a) FROM Anomaly a WHERE a.applicationId = :appId AND a.status = :status")
    long countByApplicationIdAndStatus(@Param("appId") UUID applicationId, @Param("status") AnomalyStatus status);

    @Query("SELECT a.severity, COUNT(a) FROM Anomaly a WHERE a.status IN :statuses GROUP BY a.severity")
    List<Object[]> countBySeverityAndStatusIn(@Param("statuses") List<AnomalyStatus> statuses);

    boolean existsByApplicationIdAndMetricTypeAndStatusAndDetectedAtAfter(
            UUID applicationId, MetricType metricType, AnomalyStatus status, Instant after);
}

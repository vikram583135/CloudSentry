package com.devops.platform.analyzer.model;

import com.devops.platform.metrics.model.MetricType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing threshold configuration for anomaly detection.
 */
@Entity
@Table(name = "threshold_configs", indexes = {
        @Index(name = "idx_threshold_app_metric", columnList = "application_id, metric_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ThresholdConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "application_id")
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Column(name = "metric_name")
    private String metricName;

    @Column(name = "warning_threshold")
    private Double warningThreshold;

    @Column(name = "critical_threshold")
    private Double criticalThreshold;

    @Column(name = "min_threshold")
    private Double minThreshold;

    @Column(name = "max_threshold")
    private Double maxThreshold;

    @Column(name = "z_score_threshold")
    @Builder.Default
    private Double zScoreThreshold = 3.0;

    @Column(name = "moving_avg_window_minutes")
    @Builder.Default
    private Integer movingAvgWindowMinutes = 15;

    @Column(name = "deviation_percentage_threshold")
    @Builder.Default
    private Double deviationPercentageThreshold = 20.0;

    @Column(name = "min_samples_required")
    @Builder.Default
    private Integer minSamplesRequired = 10;

    @Column(name = "cooldown_minutes")
    @Builder.Default
    private Integer cooldownMinutes = 5;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

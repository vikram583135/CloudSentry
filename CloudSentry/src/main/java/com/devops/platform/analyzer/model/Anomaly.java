package com.devops.platform.analyzer.model;

import com.devops.platform.metrics.model.MetricType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a detected anomaly.
 */
@Entity
@Table(name = "anomalies", indexes = {
        @Index(name = "idx_anomalies_app_status", columnList = "application_id, status"),
        @Index(name = "idx_anomalies_detected_at", columnList = "detected_at"),
        @Index(name = "idx_anomalies_severity", columnList = "severity")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Anomaly {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "application_id", nullable = false)
    private UUID applicationId;

    @Column(name = "application_name")
    private String applicationName;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false)
    private MetricType metricType;

    @Column(name = "metric_name")
    private String metricName;

    @Enumerated(EnumType.STRING)
    @Column(name = "detection_type", nullable = false)
    private DetectionType detectionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AnomalySeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AnomalyStatus status = AnomalyStatus.OPEN;

    @Column(name = "current_value", nullable = false)
    private Double currentValue;

    @Column(name = "expected_value")
    private Double expectedValue;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "deviation_percentage")
    private Double deviationPercentage;

    @Column(name = "z_score")
    private Double zScore;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "host")
    private String host;

    @Column(name = "environment")
    private String environment;

    @Column(name = "detected_at", nullable = false)
    private Instant detectedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "incident_id")
    private UUID incidentId;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

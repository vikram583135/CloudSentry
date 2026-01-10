package com.devops.platform.metrics.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a metric data point.
 */
@Entity
@Table(name = "metrics", indexes = {
    @Index(name = "idx_metrics_app_type_time", columnList = "application_id, metric_type, timestamp"),
    @Index(name = "idx_metrics_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Metric {

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

    @Column(nullable = false)
    private Double value;

    @Column
    private String unit;

    @Column(name = "host")
    private String host;

    @Column(name = "environment")
    private String environment;

    @Column(columnDefinition = "jsonb")
    private String tags;

    @Column(nullable = false)
    private Instant timestamp;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

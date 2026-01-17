package com.devops.platform.rca.model;

import com.devops.platform.metrics.model.MetricType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a rule for pattern-based root cause detection.
 */
@Entity
@Table(name = "rca_rules", indexes = {
        @Index(name = "idx_rca_rules_pattern", columnList = "pattern_type"),
        @Index(name = "idx_rca_rules_metric", columnList = "metric_type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RcaRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_type", nullable = false)
    private PatternType patternType;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type")
    private MetricType metricType;

    @Column(name = "condition_expression", columnDefinition = "TEXT")
    private String conditionExpression;

    @Column(name = "threshold_value")
    private Double thresholdValue;

    @Column(name = "threshold_operator")
    private String thresholdOperator;

    @Column(name = "time_window_minutes")
    @Builder.Default
    private Integer timeWindowMinutes = 15;

    @Column(name = "root_cause_template", columnDefinition = "TEXT")
    private String rootCauseTemplate;

    @Column(name = "suggested_actions", columnDefinition = "TEXT")
    private String suggestedActions;

    @Column(name = "runbook_url")
    private String runbookUrl;

    @Column(name = "priority")
    @Builder.Default
    private Integer priority = 100;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "times_matched")
    @Builder.Default
    private Long timesMatched = 0L;

    @Column(name = "last_matched_at")
    private Instant lastMatchedAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public void incrementMatchCount() {
        this.timesMatched++;
        this.lastMatchedAt = Instant.now();
    }
}

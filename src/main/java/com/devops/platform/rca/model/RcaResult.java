package com.devops.platform.rca.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a root cause analysis result.
 */
@Entity
@Table(name = "rca_results", indexes = {
        @Index(name = "idx_rca_incident", columnList = "incident_id"),
        @Index(name = "idx_rca_anomaly", columnList = "anomaly_id"),
        @Index(name = "idx_rca_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RcaResult {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "incident_id")
    private UUID incidentId;

    @Column(name = "anomaly_id")
    private UUID anomalyId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "pattern_type")
    private PatternType patternType;

    @Enumerated(EnumType.STRING)
    @Column(name = "confidence_level", nullable = false)
    private ConfidenceLevel confidenceLevel;

    @Column(name = "confidence_score")
    private Double confidenceScore;

    @Column(name = "root_cause_summary", nullable = false, columnDefinition = "TEXT")
    private String rootCauseSummary;

    @Column(name = "detailed_analysis", columnDefinition = "TEXT")
    private String detailedAnalysis;

    @Column(name = "suggested_actions", columnDefinition = "TEXT")
    private String suggestedActions;

    @Column(name = "matched_rule_id")
    private UUID matchedRuleId;

    @Column(name = "matched_rule_name")
    private String matchedRuleName;

    @Column(name = "similar_incidents", columnDefinition = "TEXT")
    private String similarIncidents;

    @Column(name = "is_ai_generated")
    @Builder.Default
    private Boolean isAiGenerated = false;

    @Column(name = "ai_model_used")
    private String aiModelUsed;

    @Column(name = "user_feedback")
    private String userFeedback;

    @Column(name = "was_helpful")
    private Boolean wasHelpful;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

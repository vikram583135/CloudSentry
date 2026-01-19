package com.devops.platform.cost.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a cost optimization recommendation.
 */
@Entity
@Table(name = "cost_recommendations", indexes = {
        @Index(name = "idx_rec_resource", columnList = "resource_id"),
        @Index(name = "idx_rec_type", columnList = "optimization_type"),
        @Index(name = "idx_rec_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "optimization_type", nullable = false)
    private OptimizationType optimizationType;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "current_cost", precision = 12, scale = 2)
    private BigDecimal currentCost;

    @Column(name = "projected_cost", precision = 12, scale = 2)
    private BigDecimal projectedCost;

    @Column(name = "estimated_savings", precision = 12, scale = 2)
    private BigDecimal estimatedSavings;

    @Column(name = "savings_percentage")
    private Double savingsPercentage;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "USD";

    @Column(name = "effort_level")
    @Builder.Default
    private String effortLevel = "LOW"; // LOW, MEDIUM, HIGH

    @Column(name = "risk_level")
    @Builder.Default
    private String riskLevel = "LOW"; // LOW, MEDIUM, HIGH

    @Column(name = "implementation_steps", columnDefinition = "TEXT")
    private String implementationSteps;

    @Column(name = "status")
    @Builder.Default
    private String status = "OPEN"; // OPEN, ACCEPTED, REJECTED, IMPLEMENTED, DISMISSED

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "implemented_at")
    private Instant implementedAt;

    @Column(name = "actual_savings", precision = 12, scale = 2)
    private BigDecimal actualSavings;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;
}

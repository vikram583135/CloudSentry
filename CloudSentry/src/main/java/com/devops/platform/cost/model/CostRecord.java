package com.devops.platform.cost.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity representing a cost record for a specific time period.
 */
@Entity
@Table(name = "cost_records", indexes = {
        @Index(name = "idx_cost_resource", columnList = "resource_id"),
        @Index(name = "idx_cost_date", columnList = "record_date"),
        @Index(name = "idx_cost_app", columnList = "application_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CostRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "resource_id")
    private UUID resourceId;

    @Column(name = "application_id")
    private UUID applicationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "cloud_provider")
    private CloudProvider cloudProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type")
    private ResourceType resourceType;

    @Column(name = "record_date", nullable = false)
    private LocalDate recordDate;

    @Column(name = "cost_amount", precision = 12, scale = 4, nullable = false)
    private BigDecimal costAmount;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "USD";

    @Column(name = "usage_quantity")
    private Double usageQuantity;

    @Column(name = "usage_unit")
    private String usageUnit;

    @Column(name = "service_name")
    private String serviceName;

    @Column(name = "service_category")
    private String serviceCategory;

    @Column
    private String region;

    @Column
    private String environment;

    @Column
    private String team;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;
}

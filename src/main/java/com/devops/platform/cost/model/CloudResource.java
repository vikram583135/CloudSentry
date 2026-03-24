package com.devops.platform.cost.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Entity representing a cloud resource being tracked for cost.
 */
@Entity
@Table(name = "cloud_resources", indexes = {
        @Index(name = "idx_resources_provider", columnList = "cloud_provider"),
        @Index(name = "idx_resources_type", columnList = "resource_type"),
        @Index(name = "idx_resources_status", columnList = "status"),
        @Index(name = "idx_resources_app", columnList = "application_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CloudResource {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "resource_id", nullable = false)
    private String resourceId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "cloud_provider", nullable = false)
    private CloudProvider cloudProvider;

    @Enumerated(EnumType.STRING)
    @Column(name = "resource_type", nullable = false)
    private ResourceType resourceType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ResourceStatus status = ResourceStatus.ACTIVE;

    @Column
    private String region;

    @Column(name = "availability_zone")
    private String availabilityZone;

    @Column(name = "instance_type")
    private String instanceType;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "application_name")
    private String applicationName;

    @Column
    private String environment;

    @Column
    private String team;

    @Column
    private String owner;

    @Column(name = "hourly_cost", precision = 12, scale = 6)
    private BigDecimal hourlyCost;

    @Column(name = "monthly_cost", precision = 12, scale = 2)
    private BigDecimal monthlyCost;

    @Column(name = "currency")
    @Builder.Default
    private String currency = "USD";

    @Column(name = "cpu_utilization")
    private Double cpuUtilization;

    @Column(name = "memory_utilization")
    private Double memoryUtilization;

    @Column(name = "disk_utilization")
    private Double diskUtilization;

    @Column(name = "network_in_bytes")
    private Long networkInBytes;

    @Column(name = "network_out_bytes")
    private Long networkOutBytes;

    @Column(name = "idle_since")
    private Instant idleSince;

    @Column(name = "idle_threshold_percent")
    @Builder.Default
    private Double idleThresholdPercent = 10.0;

    @Column(columnDefinition = "TEXT")
    private String tags;

    @Column(name = "last_activity_at")
    private Instant lastActivityAt;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Checks if resource is considered idle.
     */
    public boolean isIdle() {
        if (cpuUtilization != null && cpuUtilization < idleThresholdPercent &&
                memoryUtilization != null && memoryUtilization < idleThresholdPercent) {
            return true;
        }
        return status == ResourceStatus.IDLE;
    }

    /**
     * Calculates potential monthly savings if resource was terminated.
     */
    public BigDecimal getPotentialMonthlySavings() {
        if (isIdle() && monthlyCost != null) {
            return monthlyCost;
        }
        return BigDecimal.ZERO;
    }
}

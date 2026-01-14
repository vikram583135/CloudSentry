package com.devops.platform.incident.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Entity representing an incident.
 */
@Entity
@Table(name = "incidents", indexes = {
        @Index(name = "idx_incidents_status", columnList = "status"),
        @Index(name = "idx_incidents_severity", columnList = "severity"),
        @Index(name = "idx_incidents_app", columnList = "application_id"),
        @Index(name = "idx_incidents_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "incident_number", unique = true, nullable = false)
    private String incidentNumber;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IncidentPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private IncidentStatus status = IncidentStatus.OPEN;

    @Column(name = "application_id")
    private UUID applicationId;

    @Column(name = "application_name")
    private String applicationName;

    @Column(name = "environment")
    private String environment;

    @Column(name = "affected_services", columnDefinition = "TEXT")
    private String affectedServices;

    @Column(name = "assigned_to")
    private UUID assignedTo;

    @Column(name = "assigned_team")
    private String assignedTeam;

    @Column(name = "reporter_id")
    private UUID reporterId;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "impact_summary", columnDefinition = "TEXT")
    private String impactSummary;

    @Column(name = "customers_affected")
    private Integer customersAffected;

    @Column(name = "auto_created")
    @Builder.Default
    private Boolean autoCreated = false;

    @Column(name = "triggered_by_anomaly_id")
    private UUID triggeredByAnomalyId;

    @Column(name = "acknowledged_at")
    private Instant acknowledgedAt;

    @Column(name = "identified_at")
    private Instant identifiedAt;

    @Column(name = "mitigated_at")
    private Instant mitigatedAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "time_to_acknowledge_minutes")
    private Long timeToAcknowledgeMinutes;

    @Column(name = "time_to_resolve_minutes")
    private Long timeToResolveMinutes;

    @OneToMany(mappedBy = "incident", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<IncidentTimeline> timeline = new ArrayList<>();

    @Column(columnDefinition = "TEXT")
    private String tags;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    /**
     * Adds a timeline entry.
     */
    public void addTimelineEntry(IncidentTimeline entry) {
        timeline.add(entry);
        entry.setIncident(this);
    }

    /**
     * Calculates time to acknowledge.
     */
    public void calculateTimeToAcknowledge() {
        if (acknowledgedAt != null && createdAt != null) {
            timeToAcknowledgeMinutes = Duration.between(createdAt, acknowledgedAt).toMinutes();
        }
    }

    /**
     * Calculates time to resolve.
     */
    public void calculateTimeToResolve() {
        if (resolvedAt != null && createdAt != null) {
            timeToResolveMinutes = Duration.between(createdAt, resolvedAt).toMinutes();
        }
    }
}

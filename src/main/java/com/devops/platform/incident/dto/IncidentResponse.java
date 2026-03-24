package com.devops.platform.incident.dto;

import com.devops.platform.incident.model.IncidentPriority;
import com.devops.platform.incident.model.IncidentSeverity;
import com.devops.platform.incident.model.IncidentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO for incident response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentResponse {

    private UUID id;
    private String incidentNumber;
    private String title;
    private String description;
    private IncidentSeverity severity;
    private IncidentPriority priority;
    private IncidentStatus status;
    private UUID applicationId;
    private String applicationName;
    private String environment;
    private List<String> affectedServices;
    private UUID assignedTo;
    private String assignedTeam;
    private UUID reporterId;
    private String rootCause;
    private String resolution;
    private String impactSummary;
    private Integer customersAffected;
    private Boolean autoCreated;
    private UUID triggeredByAnomalyId;
    private Instant acknowledgedAt;
    private Instant identifiedAt;
    private Instant mitigatedAt;
    private Instant resolvedAt;
    private Instant closedAt;
    private Long timeToAcknowledgeMinutes;
    private Long timeToResolveMinutes;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
}

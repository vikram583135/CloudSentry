package com.devops.platform.incident.dto;

import com.devops.platform.incident.model.IncidentPriority;
import com.devops.platform.incident.model.IncidentSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for updating an incident.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentUpdateRequest {

    private String title;

    private String description;

    private IncidentSeverity severity;

    private IncidentPriority priority;

    private List<String> affectedServices;

    private UUID assignedTo;

    private String assignedTeam;

    private String rootCause;

    private String resolution;

    private String impactSummary;

    private Integer customersAffected;

    private List<String> tags;
}

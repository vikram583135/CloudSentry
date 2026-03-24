package com.devops.platform.incident.dto;

import com.devops.platform.incident.model.IncidentPriority;
import com.devops.platform.incident.model.IncidentSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO for creating an incident.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IncidentCreateRequest {

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    @NotNull(message = "Severity is required")
    private IncidentSeverity severity;

    @NotNull(message = "Priority is required")
    private IncidentPriority priority;

    private UUID applicationId;

    private String applicationName;

    private String environment;

    private List<String> affectedServices;

    private UUID assignedTo;

    private String assignedTeam;

    private String impactSummary;

    private Integer customersAffected;

    private List<String> tags;
}

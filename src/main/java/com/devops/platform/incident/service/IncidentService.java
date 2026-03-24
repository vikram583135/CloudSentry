package com.devops.platform.incident.service;

import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.incident.dto.*;
import com.devops.platform.incident.model.*;
import com.devops.platform.incident.repository.IncidentRepository;
import com.devops.platform.incident.repository.IncidentTimelineRepository;
import com.devops.platform.metrics.service.CustomMetricsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for incident management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final IncidentTimelineRepository timelineRepository;
    private final CustomMetricsService customMetricsService;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new incident manually.
     */
    @Transactional
    public IncidentResponse createIncident(IncidentCreateRequest request, UUID reporterId) {
        Incident incident = Incident.builder()
                .incidentNumber(generateIncidentNumber())
                .title(request.getTitle())
                .description(request.getDescription())
                .severity(request.getSeverity())
                .priority(request.getPriority())
                .status(IncidentStatus.OPEN)
                .applicationId(request.getApplicationId())
                .applicationName(request.getApplicationName())
                .environment(request.getEnvironment())
                .affectedServices(
                        request.getAffectedServices() != null ? String.join(",", request.getAffectedServices()) : null)
                .assignedTo(request.getAssignedTo())
                .assignedTeam(request.getAssignedTeam())
                .reporterId(reporterId)
                .impactSummary(request.getImpactSummary())
                .customersAffected(request.getCustomersAffected())
                .autoCreated(false)
                .tags(request.getTags() != null ? String.join(",", request.getTags()) : null)
                .build();

        // Add creation timeline entry
        IncidentTimeline timelineEntry = IncidentTimeline.builder()
                .entryType(TimelineEntryType.CREATED)
                .message("Incident created manually")
                .userId(reporterId)
                .isAutomated(false)
                .build();
        incident.addTimelineEntry(timelineEntry);

        incident = incidentRepository.save(incident);
        customMetricsService.incrementCounter("incidents.created");

        log.info("Created incident: number={}, severity={}", incident.getIncidentNumber(), incident.getSeverity());
        return toResponse(incident);
    }

    /**
     * Gets an incident by ID.
     */
    public IncidentResponse getIncident(UUID id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));
        return toResponse(incident);
    }

    /**
     * Gets an incident by number.
     */
    public IncidentResponse getIncidentByNumber(String incidentNumber) {
        Incident incident = incidentRepository.findByIncidentNumber(incidentNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "number", incidentNumber));
        return toResponse(incident);
    }

    /**
     * Gets open incidents.
     */
    public List<IncidentResponse> getOpenIncidents(int limit) {
        List<IncidentStatus> openStatuses = Arrays.asList(
                IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED,
                IncidentStatus.INVESTIGATING, IncidentStatus.IDENTIFIED,
                IncidentStatus.MITIGATING, IncidentStatus.MONITORING);

        return incidentRepository.findByStatusInOrderByCreatedAtDesc(openStatuses, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets incidents by application.
     */
    public List<IncidentResponse> getIncidentsByApplication(UUID applicationId, int limit) {
        return incidentRepository.findByApplicationIdOrderByCreatedAtDesc(applicationId, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an incident.
     */
    @Transactional
    public IncidentResponse updateIncident(UUID id, IncidentUpdateRequest request, UUID userId) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        List<String> changes = new ArrayList<>();

        if (request.getTitle() != null && !request.getTitle().equals(incident.getTitle())) {
            changes.add("Title updated");
            incident.setTitle(request.getTitle());
        }
        if (request.getDescription() != null) {
            incident.setDescription(request.getDescription());
        }
        if (request.getSeverity() != null && request.getSeverity() != incident.getSeverity()) {
            addTimelineEntry(incident, TimelineEntryType.SEVERITY_CHANGED,
                    "Severity changed", incident.getSeverity().name(), request.getSeverity().name(), userId);
            incident.setSeverity(request.getSeverity());
        }
        if (request.getPriority() != null && request.getPriority() != incident.getPriority()) {
            addTimelineEntry(incident, TimelineEntryType.PRIORITY_CHANGED,
                    "Priority changed", incident.getPriority().name(), request.getPriority().name(), userId);
            incident.setPriority(request.getPriority());
        }
        if (request.getAssignedTo() != null && !request.getAssignedTo().equals(incident.getAssignedTo())) {
            addTimelineEntry(incident, TimelineEntryType.ASSIGNED,
                    "Incident assigned", null, request.getAssignedTo().toString(), userId);
            incident.setAssignedTo(request.getAssignedTo());
        }
        if (request.getAssignedTeam() != null) {
            incident.setAssignedTeam(request.getAssignedTeam());
        }
        if (request.getRootCause() != null && !request.getRootCause().equals(incident.getRootCause())) {
            addTimelineEntry(incident, TimelineEntryType.ROOT_CAUSE_IDENTIFIED,
                    "Root cause identified: " + request.getRootCause(), null, null, userId);
            incident.setRootCause(request.getRootCause());
        }
        if (request.getResolution() != null) {
            incident.setResolution(request.getResolution());
        }
        if (request.getImpactSummary() != null) {
            incident.setImpactSummary(request.getImpactSummary());
        }
        if (request.getCustomersAffected() != null) {
            incident.setCustomersAffected(request.getCustomersAffected());
        }
        if (request.getAffectedServices() != null) {
            incident.setAffectedServices(String.join(",", request.getAffectedServices()));
        }
        if (request.getTags() != null) {
            incident.setTags(String.join(",", request.getTags()));
        }

        incident = incidentRepository.save(incident);
        log.info("Updated incident: id={}", id);
        return toResponse(incident);
    }

    /**
     * Updates incident status with workflow validation.
     */
    @Transactional
    public IncidentResponse updateStatus(UUID id, IncidentStatus newStatus, UUID userId, String comment) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        IncidentStatus oldStatus = incident.getStatus();
        incident.setStatus(newStatus);

        // Set timestamps based on status
        Instant now = Instant.now();
        switch (newStatus) {
            case ACKNOWLEDGED -> {
                if (incident.getAcknowledgedAt() == null) {
                    incident.setAcknowledgedAt(now);
                    incident.calculateTimeToAcknowledge();
                }
            }
            case IDENTIFIED -> {
                if (incident.getIdentifiedAt() == null) {
                    incident.setIdentifiedAt(now);
                }
            }
            case MITIGATING -> {
                if (incident.getMitigatedAt() == null) {
                    incident.setMitigatedAt(now);
                }
            }
            case RESOLVED -> {
                if (incident.getResolvedAt() == null) {
                    incident.setResolvedAt(now);
                    incident.calculateTimeToResolve();
                }
            }
            case CLOSED -> {
                if (incident.getClosedAt() == null) {
                    incident.setClosedAt(now);
                }
            }
        }

        String message = comment != null ? comment
                : String.format("Status changed from %s to %s", oldStatus, newStatus);
        addTimelineEntry(incident, TimelineEntryType.STATUS_CHANGED, message,
                oldStatus.name(), newStatus.name(), userId);

        incident = incidentRepository.save(incident);
        log.info("Incident status updated: id={}, {} -> {}", id, oldStatus, newStatus);
        return toResponse(incident);
    }

    /**
     * Adds a comment to an incident.
     */
    @Transactional
    public TimelineEntryResponse addComment(UUID id, String comment, UUID userId, String userName) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Incident", "id", id));

        IncidentTimeline entry = IncidentTimeline.builder()
                .entryType(TimelineEntryType.COMMENT)
                .message(comment)
                .userId(userId)
                .userName(userName)
                .isAutomated(false)
                .build();
        incident.addTimelineEntry(entry);

        incidentRepository.save(incident);
        log.debug("Added comment to incident: id={}", id);

        return toTimelineResponse(entry);
    }

    /**
     * Gets incident timeline.
     */
    public List<TimelineEntryResponse> getTimeline(UUID incidentId) {
        return timelineRepository.findByIncidentIdOrderByCreatedAtAsc(incidentId)
                .stream()
                .map(this::toTimelineResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets incident statistics.
     */
    public Map<String, Object> getIncidentStats() {
        List<IncidentStatus> openStatuses = Arrays.asList(
                IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED,
                IncidentStatus.INVESTIGATING, IncidentStatus.IDENTIFIED,
                IncidentStatus.MITIGATING, IncidentStatus.MONITORING);

        long openCount = incidentRepository.countByStatusIn(openStatuses);

        Instant last30Days = Instant.now().minus(30, ChronoUnit.DAYS);
        Double avgTTA = incidentRepository.averageTimeToAcknowledge(last30Days);
        Double avgTTR = incidentRepository.averageTimeToResolve(last30Days);

        List<Object[]> severityCounts = incidentRepository.countBySeverityAndStatusIn(openStatuses);
        Map<String, Long> bySeverity = new HashMap<>();
        for (Object[] row : severityCounts) {
            bySeverity.put(((IncidentSeverity) row[0]).name(), (Long) row[1]);
        }

        List<Object[]> statusCounts = incidentRepository.countByStatus();
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : statusCounts) {
            byStatus.put(((IncidentStatus) row[0]).name(), (Long) row[1]);
        }

        return Map.of(
                "openCount", openCount,
                "bySeverity", bySeverity,
                "byStatus", byStatus,
                "avgTimeToAcknowledgeMinutes", avgTTA != null ? avgTTA : 0,
                "avgTimeToResolveMinutes", avgTTR != null ? avgTTR : 0);
    }

    private void addTimelineEntry(Incident incident, TimelineEntryType type,
            String message, String oldValue, String newValue, UUID userId) {
        IncidentTimeline entry = IncidentTimeline.builder()
                .entryType(type)
                .message(message)
                .oldValue(oldValue)
                .newValue(newValue)
                .userId(userId)
                .isAutomated(false)
                .build();
        incident.addTimelineEntry(entry);
    }

    private String generateIncidentNumber() {
        Integer maxNumber = incidentRepository.findMaxIncidentNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("INC-%06d", nextNumber);
    }

    private IncidentResponse toResponse(Incident incident) {
        List<String> affectedServices = incident.getAffectedServices() != null
                ? Arrays.asList(incident.getAffectedServices().split(","))
                : null;
        List<String> tags = incident.getTags() != null ? Arrays.asList(incident.getTags().split(",")) : null;

        return IncidentResponse.builder()
                .id(incident.getId())
                .incidentNumber(incident.getIncidentNumber())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .priority(incident.getPriority())
                .status(incident.getStatus())
                .applicationId(incident.getApplicationId())
                .applicationName(incident.getApplicationName())
                .environment(incident.getEnvironment())
                .affectedServices(affectedServices)
                .assignedTo(incident.getAssignedTo())
                .assignedTeam(incident.getAssignedTeam())
                .reporterId(incident.getReporterId())
                .rootCause(incident.getRootCause())
                .resolution(incident.getResolution())
                .impactSummary(incident.getImpactSummary())
                .customersAffected(incident.getCustomersAffected())
                .autoCreated(incident.getAutoCreated())
                .triggeredByAnomalyId(incident.getTriggeredByAnomalyId())
                .acknowledgedAt(incident.getAcknowledgedAt())
                .identifiedAt(incident.getIdentifiedAt())
                .mitigatedAt(incident.getMitigatedAt())
                .resolvedAt(incident.getResolvedAt())
                .closedAt(incident.getClosedAt())
                .timeToAcknowledgeMinutes(incident.getTimeToAcknowledgeMinutes())
                .timeToResolveMinutes(incident.getTimeToResolveMinutes())
                .tags(tags)
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt())
                .build();
    }

    private TimelineEntryResponse toTimelineResponse(IncidentTimeline entry) {
        return TimelineEntryResponse.builder()
                .id(entry.getId())
                .entryType(entry.getEntryType())
                .message(entry.getMessage())
                .oldValue(entry.getOldValue())
                .newValue(entry.getNewValue())
                .userId(entry.getUserId())
                .userName(entry.getUserName())
                .isAutomated(entry.getIsAutomated())
                .createdAt(entry.getCreatedAt())
                .build();
    }
}

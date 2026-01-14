package com.devops.platform.incident.controller;

import com.devops.platform.common.dto.ApiResponse;
import com.devops.platform.incident.dto.*;
import com.devops.platform.incident.model.IncidentSeverity;
import com.devops.platform.incident.model.IncidentStatus;
import com.devops.platform.incident.service.IncidentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for incident management.
 */
@RestController
@RequestMapping("/api/v1/incidents")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Incidents", description = "Incident management endpoints")
public class IncidentController {

    private final IncidentService incidentService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Create incident", description = "Creates a new incident manually")
    public ResponseEntity<ApiResponse<IncidentResponse>> createIncident(
            @Valid @RequestBody IncidentCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Creating incident: title={}, severity={}", request.getTitle(), request.getSeverity());
        // In real implementation, extract user ID from userDetails
        UUID reporterId = UUID.randomUUID(); // Placeholder
        IncidentResponse response = incidentService.createIncident(request, reporterId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Incident created"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get incident by ID")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncident(
            @Parameter(description = "Incident ID") @PathVariable UUID id) {

        IncidentResponse response = incidentService.getIncident(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/number/{incidentNumber}")
    @Operation(summary = "Get incident by number", description = "Retrieves incident by INC-XXXXXX number")
    public ResponseEntity<ApiResponse<IncidentResponse>> getIncidentByNumber(
            @Parameter(description = "Incident number (e.g., INC-000001)") @PathVariable String incidentNumber) {

        IncidentResponse response = incidentService.getIncidentByNumber(incidentNumber);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/open")
    @Operation(summary = "Get open incidents", description = "Retrieves all incidents not yet closed")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getOpenIncidents(
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<IncidentResponse> incidents = incidentService.getOpenIncidents(limit);
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get incidents by application")
    public ResponseEntity<ApiResponse<List<IncidentResponse>>> getIncidentsByApplication(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<IncidentResponse> incidents = incidentService.getIncidentsByApplication(applicationId, limit);
        return ResponseEntity.ok(ApiResponse.success(incidents));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Update incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateIncident(
            @Parameter(description = "Incident ID") @PathVariable UUID id,
            @Valid @RequestBody IncidentUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating incident: id={}", id);
        UUID userId = UUID.randomUUID(); // Placeholder
        IncidentResponse response = incidentService.updateIncident(id, request, userId);

        return ResponseEntity.ok(ApiResponse.success(response, "Incident updated"));
    }

    @PostMapping("/{id}/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Update incident status")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateStatus(
            @Parameter(description = "Incident ID") @PathVariable UUID id,
            @Parameter(description = "New status") @PathVariable IncidentStatus status,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal UserDetails userDetails) {

        log.info("Updating incident status: id={}, status={}", id, status);
        UUID userId = UUID.randomUUID(); // Placeholder
        IncidentResponse response = incidentService.updateStatus(id, status, userId, comment);

        return ResponseEntity.ok(ApiResponse.success(response, "Status updated to " + status));
    }

    @PostMapping("/{id}/acknowledge")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Acknowledge incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> acknowledge(
            @Parameter(description = "Incident ID") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.randomUUID();
        IncidentResponse response = incidentService.updateStatus(id, IncidentStatus.ACKNOWLEDGED, userId,
                "Incident acknowledged");
        return ResponseEntity.ok(ApiResponse.success(response, "Incident acknowledged"));
    }

    @PostMapping("/{id}/investigate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Start investigation")
    public ResponseEntity<ApiResponse<IncidentResponse>> investigate(
            @Parameter(description = "Incident ID") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.randomUUID();
        IncidentResponse response = incidentService.updateStatus(id, IncidentStatus.INVESTIGATING, userId,
                "Investigation started");
        return ResponseEntity.ok(ApiResponse.success(response, "Investigation started"));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Resolve incident")
    public ResponseEntity<ApiResponse<IncidentResponse>> resolve(
            @Parameter(description = "Incident ID") @PathVariable UUID id,
            @RequestParam(required = false) String resolution,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.randomUUID();
        String comment = resolution != null ? "Resolved: " + resolution : "Incident resolved";
        IncidentResponse response = incidentService.updateStatus(id, IncidentStatus.RESOLVED, userId, comment);
        return ResponseEntity.ok(ApiResponse.success(response, "Incident resolved"));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Close incident", description = "Closes incident after post-mortem")
    public ResponseEntity<ApiResponse<IncidentResponse>> close(
            @Parameter(description = "Incident ID") @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {

        UUID userId = UUID.randomUUID();
        IncidentResponse response = incidentService.updateStatus(id, IncidentStatus.CLOSED, userId, "Incident closed");
        return ResponseEntity.ok(ApiResponse.success(response, "Incident closed"));
    }

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Add comment to incident")
    public ResponseEntity<ApiResponse<TimelineEntryResponse>> addComment(
            @Parameter(description = "Incident ID") @PathVariable UUID id,
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {

        String comment = body.get("comment");
        UUID userId = UUID.randomUUID();
        String userName = userDetails != null ? userDetails.getUsername() : "Unknown";

        TimelineEntryResponse response = incidentService.addComment(id, comment, userId, userName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Comment added"));
    }

    @GetMapping("/{id}/timeline")
    @Operation(summary = "Get incident timeline")
    public ResponseEntity<ApiResponse<List<TimelineEntryResponse>>> getTimeline(
            @Parameter(description = "Incident ID") @PathVariable UUID id) {

        List<TimelineEntryResponse> timeline = incidentService.getTimeline(id);
        return ResponseEntity.ok(ApiResponse.success(timeline));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get incident statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = incidentService.getIncidentStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/severities")
    @Operation(summary = "Get available severity levels")
    public ResponseEntity<ApiResponse<IncidentSeverity[]>> getSeverities() {
        return ResponseEntity.ok(ApiResponse.success(IncidentSeverity.values()));
    }

    @GetMapping("/statuses")
    @Operation(summary = "Get available status values")
    public ResponseEntity<ApiResponse<IncidentStatus[]>> getStatuses() {
        return ResponseEntity.ok(ApiResponse.success(IncidentStatus.values()));
    }
}

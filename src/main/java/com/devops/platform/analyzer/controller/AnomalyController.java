package com.devops.platform.analyzer.controller;

import com.devops.platform.analyzer.dto.AnomalyResponse;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.AnomalyStatus;
import com.devops.platform.analyzer.service.AnomalyService;
import com.devops.platform.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for anomaly management.
 */
@RestController
@RequestMapping("/api/v1/anomalies")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Anomalies", description = "Anomaly management endpoints")
public class AnomalyController {

    private final AnomalyService anomalyService;

    @GetMapping("/{id}")
    @Operation(summary = "Get anomaly by ID")
    public ResponseEntity<ApiResponse<AnomalyResponse>> getAnomaly(
            @Parameter(description = "Anomaly ID") @PathVariable UUID id) {

        AnomalyResponse anomaly = anomalyService.getAnomaly(id);
        return ResponseEntity.ok(ApiResponse.success(anomaly));
    }

    @GetMapping("/open")
    @Operation(summary = "Get open anomalies", description = "Retrieves all open, acknowledged, and investigating anomalies")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> getOpenAnomalies(
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<AnomalyResponse> anomalies = anomalyService.getOpenAnomalies(limit);
        return ResponseEntity.ok(ApiResponse.success(anomalies));
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get anomalies by application")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> getAnomaliesByApplication(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "Status filter") @RequestParam(defaultValue = "OPEN") AnomalyStatus status,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<AnomalyResponse> anomalies = anomalyService.getAnomaliesByApplication(applicationId, status, limit);
        return ResponseEntity.ok(ApiResponse.success(anomalies));
    }

    @GetMapping("/severity/{severity}")
    @Operation(summary = "Get anomalies by severity")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> getAnomaliesBySeverity(
            @Parameter(description = "Severity level") @PathVariable AnomalySeverity severity,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<AnomalyResponse> anomalies = anomalyService.getAnomaliesBySeverity(severity, limit);
        return ResponseEntity.ok(ApiResponse.success(anomalies));
    }

    @GetMapping("/application/{applicationId}/timerange")
    @Operation(summary = "Get anomalies by time range")
    public ResponseEntity<ApiResponse<List<AnomalyResponse>>> getAnomaliesByTimeRange(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "Start time (ISO-8601)") @RequestParam Instant startTime,
            @Parameter(description = "End time (ISO-8601)") @RequestParam Instant endTime,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<AnomalyResponse> anomalies = anomalyService.getAnomaliesByTimeRange(
                applicationId, startTime, endTime, limit);
        return ResponseEntity.ok(ApiResponse.success(anomalies));
    }

    @PostMapping("/{id}/acknowledge")
    @Operation(summary = "Acknowledge an anomaly")
    public ResponseEntity<ApiResponse<AnomalyResponse>> acknowledge(
            @Parameter(description = "Anomaly ID") @PathVariable UUID id) {

        log.info("Acknowledging anomaly: id={}", id);
        AnomalyResponse anomaly = anomalyService.acknowledge(id);
        return ResponseEntity.ok(ApiResponse.success(anomaly, "Anomaly acknowledged"));
    }

    @PostMapping("/{id}/investigate")
    @Operation(summary = "Mark anomaly as being investigated")
    public ResponseEntity<ApiResponse<AnomalyResponse>> investigate(
            @Parameter(description = "Anomaly ID") @PathVariable UUID id) {

        log.info("Starting investigation for anomaly: id={}", id);
        AnomalyResponse anomaly = anomalyService.investigate(id);
        return ResponseEntity.ok(ApiResponse.success(anomaly, "Investigation started"));
    }

    @PostMapping("/{id}/resolve")
    @Operation(summary = "Resolve an anomaly")
    public ResponseEntity<ApiResponse<AnomalyResponse>> resolve(
            @Parameter(description = "Anomaly ID") @PathVariable UUID id) {

        log.info("Resolving anomaly: id={}", id);
        AnomalyResponse anomaly = anomalyService.resolve(id);
        return ResponseEntity.ok(ApiResponse.success(anomaly, "Anomaly resolved"));
    }

    @PostMapping("/{id}/false-positive")
    @Operation(summary = "Mark anomaly as false positive")
    public ResponseEntity<ApiResponse<AnomalyResponse>> markFalsePositive(
            @Parameter(description = "Anomaly ID") @PathVariable UUID id) {

        log.info("Marking anomaly as false positive: id={}", id);
        AnomalyResponse anomaly = anomalyService.markFalsePositive(id);
        return ResponseEntity.ok(ApiResponse.success(anomaly, "Marked as false positive"));
    }

    @PostMapping("/{anomalyId}/link-incident/{incidentId}")
    @Operation(summary = "Link anomaly to incident")
    public ResponseEntity<ApiResponse<AnomalyResponse>> linkToIncident(
            @Parameter(description = "Anomaly ID") @PathVariable UUID anomalyId,
            @Parameter(description = "Incident ID") @PathVariable UUID incidentId) {

        log.info("Linking anomaly {} to incident {}", anomalyId, incidentId);
        AnomalyResponse anomaly = anomalyService.linkToIncident(anomalyId, incidentId);
        return ResponseEntity.ok(ApiResponse.success(anomaly, "Anomaly linked to incident"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get anomaly statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = anomalyService.getAnomalyStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}

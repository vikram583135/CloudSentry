package com.devops.platform.rca.controller;

import com.devops.platform.common.dto.ApiResponse;
import com.devops.platform.rca.dto.RcaRequest;
import com.devops.platform.rca.dto.RcaResponse;
import com.devops.platform.rca.dto.RcaRuleRequest;
import com.devops.platform.rca.model.PatternType;
import com.devops.platform.rca.model.RcaRule;
import com.devops.platform.rca.service.RcaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for Root Cause Analysis.
 */
@RestController
@RequestMapping("/api/v1/rca")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Root Cause Analysis", description = "AI-powered root cause analysis endpoints")
public class RcaController {

    private final RcaService rcaService;

    @PostMapping("/analyze")
    @Operation(summary = "Perform RCA", description = "Analyzes anomaly or incident for root cause")
    public ResponseEntity<ApiResponse<RcaResponse>> analyze(
            @Valid @RequestBody RcaRequest request) {

        log.info("RCA request: anomalyId={}, incidentId={}",
                request.getAnomalyId(), request.getIncidentId());

        RcaResponse response = rcaService.analyze(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Analysis complete"));
    }

    @PostMapping("/analyze/anomaly/{anomalyId}")
    @Operation(summary = "Analyze anomaly", description = "Performs RCA on a specific anomaly")
    public ResponseEntity<ApiResponse<RcaResponse>> analyzeAnomaly(
            @Parameter(description = "Anomaly ID") @PathVariable UUID anomalyId) {

        log.info("Analyzing anomaly: id={}", anomalyId);
        RcaResponse response = rcaService.analyzeAnomaly(anomalyId);
        return ResponseEntity.ok(ApiResponse.success(response, "Anomaly analysis complete"));
    }

    @PostMapping("/analyze/incident/{incidentId}")
    @Operation(summary = "Analyze incident", description = "Performs RCA on a specific incident")
    public ResponseEntity<ApiResponse<RcaResponse>> analyzeIncident(
            @Parameter(description = "Incident ID") @PathVariable UUID incidentId) {

        log.info("Analyzing incident: id={}", incidentId);
        RcaResponse response = rcaService.analyzeIncident(incidentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Incident analysis complete"));
    }

    @GetMapping("/results/{id}")
    @Operation(summary = "Get RCA result", description = "Retrieves a specific RCA result")
    public ResponseEntity<ApiResponse<RcaResponse>> getResult(
            @Parameter(description = "Result ID") @PathVariable UUID id) {

        RcaResponse response = rcaService.getResult(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/incident/{incidentId}/results")
    @Operation(summary = "Get results for incident", description = "Gets all RCA results for an incident")
    public ResponseEntity<ApiResponse<List<RcaResponse>>> getResultsForIncident(
            @Parameter(description = "Incident ID") @PathVariable UUID incidentId) {

        List<RcaResponse> results = rcaService.getResultsForIncident(incidentId);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @PostMapping("/results/{id}/feedback")
    @Operation(summary = "Provide feedback", description = "Provides feedback on RCA result accuracy")
    public ResponseEntity<ApiResponse<Void>> provideFeedback(
            @Parameter(description = "Result ID") @PathVariable UUID id,
            @RequestBody Map<String, Object> body) {

        boolean wasHelpful = Boolean.parseBoolean(body.get("wasHelpful").toString());
        String feedback = body.get("feedback") != null ? body.get("feedback").toString() : null;

        rcaService.provideFeedback(id, wasHelpful, feedback);
        return ResponseEntity.ok(ApiResponse.success(null, "Feedback recorded"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get RCA statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = rcaService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // Rule management endpoints

    @PostMapping("/rules")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Create RCA rule")
    public ResponseEntity<ApiResponse<RcaRule>> createRule(
            @Valid @RequestBody RcaRuleRequest request) {

        log.info("Creating RCA rule: name={}", request.getName());
        RcaRule rule = rcaService.createRule(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(rule, "Rule created"));
    }

    @GetMapping("/rules")
    @Operation(summary = "List RCA rules")
    public ResponseEntity<ApiResponse<List<RcaRule>>> getAllRules() {
        List<RcaRule> rules = rcaService.getAllRules();
        return ResponseEntity.ok(ApiResponse.success(rules));
    }

    @GetMapping("/rules/{id}")
    @Operation(summary = "Get RCA rule by ID")
    public ResponseEntity<ApiResponse<RcaRule>> getRule(
            @Parameter(description = "Rule ID") @PathVariable UUID id) {

        RcaRule rule = rcaService.getRule(id);
        return ResponseEntity.ok(ApiResponse.success(rule));
    }

    @DeleteMapping("/rules/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete RCA rule")
    public ResponseEntity<ApiResponse<Void>> deleteRule(
            @Parameter(description = "Rule ID") @PathVariable UUID id) {

        log.info("Deleting RCA rule: id={}", id);
        rcaService.deleteRule(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Rule deleted"));
    }

    @GetMapping("/patterns")
    @Operation(summary = "Get available pattern types")
    public ResponseEntity<ApiResponse<PatternType[]>> getPatternTypes() {
        return ResponseEntity.ok(ApiResponse.success(PatternType.values()));
    }
}

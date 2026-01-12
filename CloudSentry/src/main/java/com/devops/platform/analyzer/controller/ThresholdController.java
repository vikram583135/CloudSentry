package com.devops.platform.analyzer.controller;

import com.devops.platform.analyzer.dto.ThresholdConfigRequest;
import com.devops.platform.analyzer.model.ThresholdConfig;
import com.devops.platform.analyzer.service.ThresholdService;
import com.devops.platform.common.dto.ApiResponse;
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
import java.util.UUID;

/**
 * REST controller for threshold configuration management.
 */
@RestController
@RequestMapping("/api/v1/thresholds")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Thresholds", description = "Threshold configuration endpoints")
public class ThresholdController {

    private final ThresholdService thresholdService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Create threshold configuration")
    public ResponseEntity<ApiResponse<ThresholdConfig>> createThreshold(
            @Valid @RequestBody ThresholdConfigRequest request) {

        log.info("Creating threshold config for metricType={}", request.getMetricType());
        ThresholdConfig config = thresholdService.createThreshold(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(config, "Threshold configuration created"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get threshold by ID")
    public ResponseEntity<ApiResponse<ThresholdConfig>> getThreshold(
            @Parameter(description = "Threshold ID") @PathVariable UUID id) {

        ThresholdConfig config = thresholdService.getThreshold(id);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get thresholds for application")
    public ResponseEntity<ApiResponse<List<ThresholdConfig>>> getThresholdsForApplication(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId) {

        List<ThresholdConfig> configs = thresholdService.getThresholdsForApplication(applicationId);
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/global")
    @Operation(summary = "Get global thresholds", description = "Gets thresholds not specific to any application")
    public ResponseEntity<ApiResponse<List<ThresholdConfig>>> getGlobalThresholds() {
        List<ThresholdConfig> configs = thresholdService.getGlobalThresholds();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/enabled")
    @Operation(summary = "Get all enabled thresholds")
    public ResponseEntity<ApiResponse<List<ThresholdConfig>>> getEnabledThresholds() {
        List<ThresholdConfig> configs = thresholdService.getEnabledThresholds();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Update threshold configuration")
    public ResponseEntity<ApiResponse<ThresholdConfig>> updateThreshold(
            @Parameter(description = "Threshold ID") @PathVariable UUID id,
            @Valid @RequestBody ThresholdConfigRequest request) {

        log.info("Updating threshold config: id={}", id);
        ThresholdConfig config = thresholdService.updateThreshold(id, request);

        return ResponseEntity.ok(ApiResponse.success(config, "Threshold configuration updated"));
    }

    @PostMapping("/{id}/enable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Enable threshold")
    public ResponseEntity<ApiResponse<ThresholdConfig>> enableThreshold(
            @Parameter(description = "Threshold ID") @PathVariable UUID id) {

        log.info("Enabling threshold: id={}", id);
        ThresholdConfig config = thresholdService.setEnabled(id, true);

        return ResponseEntity.ok(ApiResponse.success(config, "Threshold enabled"));
    }

    @PostMapping("/{id}/disable")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Disable threshold")
    public ResponseEntity<ApiResponse<ThresholdConfig>> disableThreshold(
            @Parameter(description = "Threshold ID") @PathVariable UUID id) {

        log.info("Disabling threshold: id={}", id);
        ThresholdConfig config = thresholdService.setEnabled(id, false);

        return ResponseEntity.ok(ApiResponse.success(config, "Threshold disabled"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete threshold configuration")
    public ResponseEntity<ApiResponse<Void>> deleteThreshold(
            @Parameter(description = "Threshold ID") @PathVariable UUID id) {

        log.info("Deleting threshold: id={}", id);
        thresholdService.deleteThreshold(id);

        return ResponseEntity.ok(ApiResponse.success(null, "Threshold configuration deleted"));
    }

    @PostMapping("/defaults")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create default thresholds", description = "Creates default thresholds for all metric types")
    public ResponseEntity<ApiResponse<List<ThresholdConfig>>> createDefaultThresholds() {
        log.info("Creating default thresholds");
        List<ThresholdConfig> configs = thresholdService.createDefaultThresholds();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(configs, "Default thresholds created"));
    }
}

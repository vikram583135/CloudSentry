package com.devops.platform.metrics.controller;

import com.devops.platform.common.dto.ApiResponse;
import com.devops.platform.metrics.dto.ApplicationRequest;
import com.devops.platform.metrics.dto.ApplicationResponse;
import com.devops.platform.metrics.service.ApplicationService;
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
 * REST controller for application management.
 */
@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Applications", description = "Application management endpoints")
public class ApplicationController {

    private final ApplicationService applicationService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Create application", description = "Creates a new application to monitor")
    public ResponseEntity<ApiResponse<ApplicationResponse>> createApplication(
            @Valid @RequestBody ApplicationRequest request) {
        
        log.info("Creating application: name={}", request.getName());
        ApplicationResponse response = applicationService.createApplication(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Application created"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get application by ID")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplication(
            @Parameter(description = "Application ID") @PathVariable UUID id) {
        
        ApplicationResponse response = applicationService.getApplication(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get application by name")
    public ResponseEntity<ApiResponse<ApplicationResponse>> getApplicationByName(
            @Parameter(description = "Application name") @PathVariable String name) {
        
        ApplicationResponse response = applicationService.getApplicationByName(name);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @Operation(summary = "List all applications")
    public ResponseEntity<ApiResponse<List<ApplicationResponse>>> getAllApplications(
            @Parameter(description = "Only active applications") @RequestParam(defaultValue = "true") boolean activeOnly) {
        
        List<ApplicationResponse> applications = activeOnly ?
                applicationService.getActiveApplications() :
                applicationService.getAllApplications();
        
        return ResponseEntity.ok(ApiResponse.success(applications));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Update application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> updateApplication(
            @Parameter(description = "Application ID") @PathVariable UUID id,
            @Valid @RequestBody ApplicationRequest request) {
        
        log.info("Updating application: id={}", id);
        ApplicationResponse response = applicationService.updateApplication(id, request);
        
        return ResponseEntity.ok(ApiResponse.success(response, "Application updated"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Deactivate application", description = "Soft deletes an application")
    public ResponseEntity<ApiResponse<Void>> deactivateApplication(
            @Parameter(description = "Application ID") @PathVariable UUID id) {
        
        log.info("Deactivating application: id={}", id);
        applicationService.deactivateApplication(id);
        
        return ResponseEntity.ok(ApiResponse.success(null, "Application deactivated"));
    }

    @PostMapping("/{id}/regenerate-api-key")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Regenerate API key", description = "Generates a new API key for the application")
    public ResponseEntity<ApiResponse<ApplicationResponse>> regenerateApiKey(
            @Parameter(description = "Application ID") @PathVariable UUID id) {
        
        log.info("Regenerating API key for application: id={}", id);
        ApplicationResponse response = applicationService.regenerateApiKey(id);
        
        return ResponseEntity.ok(ApiResponse.success(response, "API key regenerated"));
    }
}

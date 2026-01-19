package com.devops.platform.cost.controller;

import com.devops.platform.common.dto.ApiResponse;
import com.devops.platform.cost.dto.*;
import com.devops.platform.cost.model.*;
import com.devops.platform.cost.service.*;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for cloud cost management.
 */
@RestController
@RequestMapping("/api/v1/costs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Cost Management", description = "Cloud cost tracking and optimization endpoints")
public class CostController {

    private final CostService costService;
    private final CostOptimizationService optimizationService;

    // Resource endpoints

    @PostMapping("/resources")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Create or update resource", description = "Creates or updates a cloud resource for cost tracking")
    public ResponseEntity<ApiResponse<CloudResourceResponse>> createOrUpdateResource(
            @Valid @RequestBody CloudResourceRequest request) {

        log.info("Creating/updating resource: resourceId={}", request.getResourceId());
        CloudResourceResponse response = costService.createOrUpdateResource(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Resource created/updated"));
    }

    @GetMapping("/resources/{id}")
    @Operation(summary = "Get resource by ID")
    public ResponseEntity<ApiResponse<CloudResourceResponse>> getResource(
            @Parameter(description = "Resource ID") @PathVariable UUID id) {

        CloudResourceResponse response = costService.getResource(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/resources")
    @Operation(summary = "List all resources")
    public ResponseEntity<ApiResponse<List<CloudResourceResponse>>> getAllResources(
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<CloudResourceResponse> resources = costService.getAllResources(limit);
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @GetMapping("/resources/provider/{provider}")
    @Operation(summary = "Get resources by provider")
    public ResponseEntity<ApiResponse<List<CloudResourceResponse>>> getResourcesByProvider(
            @Parameter(description = "Cloud provider") @PathVariable CloudProvider provider,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "100") int limit) {

        List<CloudResourceResponse> resources = costService.getResourcesByProvider(provider, limit);
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @GetMapping("/resources/idle")
    @Operation(summary = "Get idle resources", description = "Resources with low utilization, candidates for termination")
    public ResponseEntity<ApiResponse<List<CloudResourceResponse>>> getIdleResources() {
        List<CloudResourceResponse> resources = costService.getIdleResources();
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @GetMapping("/resources/underutilized")
    @Operation(summary = "Get underutilized resources", description = "Resources with medium utilization, candidates for rightsizing")
    public ResponseEntity<ApiResponse<List<CloudResourceResponse>>> getUnderutilizedResources(
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "50") int limit) {

        List<CloudResourceResponse> resources = costService.getUnderutilizedResources(limit);
        return ResponseEntity.ok(ApiResponse.success(resources));
    }

    @PutMapping("/resources/{id}/utilization")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Update resource utilization")
    public ResponseEntity<ApiResponse<CloudResourceResponse>> updateUtilization(
            @Parameter(description = "Resource ID") @PathVariable UUID id,
            @RequestBody Map<String, Double> utilization) {

        Double cpuUtil = utilization.get("cpu");
        Double memoryUtil = utilization.get("memory");
        Double diskUtil = utilization.get("disk");

        CloudResourceResponse response = costService.updateUtilization(id, cpuUtil, memoryUtil, diskUtil);
        return ResponseEntity.ok(ApiResponse.success(response, "Utilization updated"));
    }

    // Summary and analytics endpoints

    @GetMapping("/summary")
    @Operation(summary = "Get cost summary", description = "Dashboard overview of costs and savings")
    public ResponseEntity<ApiResponse<CostSummary>> getCostSummary() {
        CostSummary summary = costService.getCostSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // Recommendation endpoints

    @PostMapping("/recommendations/generate")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Generate recommendations", description = "Analyzes all resources and generates optimization recommendations")
    public ResponseEntity<ApiResponse<List<CostRecommendationResponse>>> generateRecommendations() {
        log.info("Generating cost optimization recommendations");
        List<CostRecommendationResponse> recommendations = optimizationService.generateRecommendations();

        return ResponseEntity.ok(ApiResponse.success(recommendations,
                "Generated " + recommendations.size() + " recommendations"));
    }

    @GetMapping("/recommendations")
    @Operation(summary = "Get open recommendations")
    public ResponseEntity<ApiResponse<List<CostRecommendationResponse>>> getOpenRecommendations(
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "50") int limit) {

        List<CostRecommendationResponse> recommendations = optimizationService.getOpenRecommendations(limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/recommendations/type/{type}")
    @Operation(summary = "Get recommendations by type")
    public ResponseEntity<ApiResponse<List<CostRecommendationResponse>>> getRecommendationsByType(
            @Parameter(description = "Optimization type") @PathVariable OptimizationType type,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "50") int limit) {

        List<CostRecommendationResponse> recommendations = optimizationService.getRecommendationsByType(type, limit);
        return ResponseEntity.ok(ApiResponse.success(recommendations));
    }

    @GetMapping("/recommendations/{id}")
    @Operation(summary = "Get recommendation by ID")
    public ResponseEntity<ApiResponse<CostRecommendationResponse>> getRecommendation(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id) {

        CostRecommendationResponse response = optimizationService.getRecommendation(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/recommendations/{id}/accept")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Accept recommendation")
    public ResponseEntity<ApiResponse<CostRecommendationResponse>> acceptRecommendation(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id) {

        log.info("Accepting recommendation: id={}", id);
        CostRecommendationResponse response = optimizationService.acceptRecommendation(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendation accepted"));
    }

    @PostMapping("/recommendations/{id}/implement")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Mark recommendation as implemented")
    public ResponseEntity<ApiResponse<CostRecommendationResponse>> implementRecommendation(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id,
            @RequestBody(required = false) Map<String, Object> body) {

        BigDecimal actualSavings = null;
        if (body != null && body.get("actualSavings") != null) {
            actualSavings = new BigDecimal(body.get("actualSavings").toString());
        }

        log.info("Implementing recommendation: id={}, actualSavings={}", id, actualSavings);
        CostRecommendationResponse response = optimizationService.implementRecommendation(id, actualSavings);
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendation implemented"));
    }

    @PostMapping("/recommendations/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Reject recommendation")
    public ResponseEntity<ApiResponse<CostRecommendationResponse>> rejectRecommendation(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id,
            @RequestBody Map<String, String> body) {

        String reason = body.get("reason");
        log.info("Rejecting recommendation: id={}, reason={}", id, reason);
        CostRecommendationResponse response = optimizationService.rejectRecommendation(id, reason);
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendation rejected"));
    }

    @PostMapping("/recommendations/{id}/dismiss")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Dismiss recommendation")
    public ResponseEntity<ApiResponse<CostRecommendationResponse>> dismissRecommendation(
            @Parameter(description = "Recommendation ID") @PathVariable UUID id) {

        log.info("Dismissing recommendation: id={}", id);
        CostRecommendationResponse response = optimizationService.dismissRecommendation(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Recommendation dismissed"));
    }

    @GetMapping("/optimization/summary")
    @Operation(summary = "Get optimization summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getOptimizationSummary() {
        Map<String, Object> summary = optimizationService.getOptimizationSummary();
        return ResponseEntity.ok(ApiResponse.success(summary));
    }

    // Enums

    @GetMapping("/providers")
    @Operation(summary = "Get available cloud providers")
    public ResponseEntity<ApiResponse<CloudProvider[]>> getProviders() {
        return ResponseEntity.ok(ApiResponse.success(CloudProvider.values()));
    }

    @GetMapping("/resource-types")
    @Operation(summary = "Get available resource types")
    public ResponseEntity<ApiResponse<ResourceType[]>> getResourceTypes() {
        return ResponseEntity.ok(ApiResponse.success(ResourceType.values()));
    }

    @GetMapping("/optimization-types")
    @Operation(summary = "Get available optimization types")
    public ResponseEntity<ApiResponse<OptimizationType[]>> getOptimizationTypes() {
        return ResponseEntity.ok(ApiResponse.success(OptimizationType.values()));
    }
}

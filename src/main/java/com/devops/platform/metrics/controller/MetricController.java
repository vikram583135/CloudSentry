package com.devops.platform.metrics.controller;

import com.devops.platform.common.dto.ApiResponse;
import com.devops.platform.metrics.dto.*;
import com.devops.platform.metrics.model.MetricType;
import com.devops.platform.metrics.service.MetricService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for metrics ingestion and retrieval.
 */
@RestController
@RequestMapping("/api/v1/metrics")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Metrics", description = "Metrics ingestion and retrieval endpoints")
public class MetricController {

    private final MetricService metricService;

    @PostMapping
    @Operation(summary = "Ingest a single metric", description = "Sends a metric to Kafka for async processing")
    public ResponseEntity<ApiResponse<Map<String, String>>> ingestMetric(
            @Valid @RequestBody MetricRequest request) {
        
        log.info("Ingesting metric: appId={}, type={}", 
                request.getApplicationId(), request.getMetricType());
        
        metricService.ingestMetric(request);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        Map.of("status", "accepted", "message", "Metric queued for processing"),
                        "Metric accepted"));
    }

    @PostMapping("/batch")
    @Operation(summary = "Ingest multiple metrics", description = "Sends a batch of metrics to Kafka")
    public ResponseEntity<ApiResponse<Map<String, Object>>> ingestMetricBatch(
            @Valid @RequestBody MetricBatchRequest batchRequest) {
        
        log.info("Ingesting batch of {} metrics", batchRequest.getMetrics().size());
        
        metricService.ingestMetricBatch(batchRequest);
        
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(
                        Map.of("status", "accepted", 
                               "count", batchRequest.getMetrics().size(),
                               "message", "Metrics queued for processing"),
                        "Batch accepted"));
    }

    @PostMapping("/direct")
    @Operation(summary = "Ingest metric directly", description = "Saves a metric directly to database (bypasses Kafka)")
    public ResponseEntity<ApiResponse<MetricResponse>> ingestMetricDirect(
            @Valid @RequestBody MetricRequest request) {
        
        log.info("Direct ingestion: appId={}, type={}", 
                request.getApplicationId(), request.getMetricType());
        
        MetricResponse response = metricService.saveMetricDirect(request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Metric saved"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get metric by ID")
    public ResponseEntity<ApiResponse<MetricResponse>> getMetricById(
            @Parameter(description = "Metric ID") @PathVariable UUID id) {
        
        MetricResponse metric = metricService.getMetricById(id);
        return ResponseEntity.ok(ApiResponse.success(metric));
    }

    @GetMapping("/application/{applicationId}")
    @Operation(summary = "Get metrics by application", description = "Retrieves recent metrics for an application")
    public ResponseEntity<ApiResponse<List<MetricResponse>>> getMetricsByApplication(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "100") int limit) {
        
        List<MetricResponse> metrics = metricService.getMetricsByApplication(applicationId, limit);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/application/{applicationId}/type/{type}")
    @Operation(summary = "Get metrics by application and type")
    public ResponseEntity<ApiResponse<List<MetricResponse>>> getMetricsByApplicationAndType(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "Metric type") @PathVariable MetricType type,
            @Parameter(description = "Maximum number of results") @RequestParam(defaultValue = "100") int limit) {
        
        List<MetricResponse> metrics = metricService.getMetricsByApplicationAndType(applicationId, type, limit);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @PostMapping("/query")
    @Operation(summary = "Query metrics", description = "Query metrics with filters")
    public ResponseEntity<ApiResponse<List<MetricResponse>>> queryMetrics(
            @Valid @RequestBody MetricQueryRequest query) {
        
        List<MetricResponse> metrics = metricService.queryMetrics(query);
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/application/{applicationId}/average")
    @Operation(summary = "Get average metric value", description = "Calculate average for a metric type over time range")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getAverageMetricValue(
            @Parameter(description = "Application ID") @PathVariable UUID applicationId,
            @Parameter(description = "Metric type") @RequestParam MetricType type,
            @Parameter(description = "Start time (ISO-8601)") @RequestParam Instant startTime,
            @Parameter(description = "End time (ISO-8601)") @RequestParam Instant endTime) {
        
        Double average = metricService.getAverageMetricValue(applicationId, type, startTime, endTime);
        
        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "applicationId", applicationId,
                "metricType", type,
                "average", average != null ? average : 0.0,
                "startTime", startTime,
                "endTime", endTime
        )));
    }

    @GetMapping("/types")
    @Operation(summary = "Get available metric types")
    public ResponseEntity<ApiResponse<MetricType[]>> getMetricTypes() {
        return ResponseEntity.ok(ApiResponse.success(MetricType.values()));
    }
}

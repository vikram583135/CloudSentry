package com.devops.platform.common.controller;

import com.devops.platform.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for application status.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Health check endpoints")
public class HealthController {

    @Value("${spring.application.name}")
    private String applicationName;

    @GetMapping("/health")
    @Operation(summary = "Health check", description = "Check if the application is running")
    public ResponseEntity<ApiResponse<Map<String, Object>>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("application", applicationName);
        healthInfo.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(ApiResponse.success(healthInfo));
    }

    @GetMapping("/info")
    @Operation(summary = "Application info", description = "Get application information")
    public ResponseEntity<ApiResponse<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("name", applicationName);
        info.put("version", "1.0.0-SNAPSHOT");
        info.put("description", "AI-Powered Smart DevOps Incident & Cost Optimization Platform");
        info.put("java", System.getProperty("java.version"));
        
        return ResponseEntity.ok(ApiResponse.success(info));
    }
}

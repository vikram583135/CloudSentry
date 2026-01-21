package com.devops.platform.notification.controller;

import com.devops.platform.common.dto.ApiResponse;
import com.devops.platform.notification.dto.*;
import com.devops.platform.notification.model.*;
import com.devops.platform.notification.service.NotificationService;
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
 * REST controller for notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE', 'DEVELOPER')")
    @Operation(summary = "Send notification", description = "Sends a notification to the specified recipient")
    public ResponseEntity<ApiResponse<NotificationResponse>> send(
            @Valid @RequestBody SendNotificationRequest request) {

        log.info("Sending notification: type={}, recipient={}",
                request.getNotificationType(), request.getRecipient());

        NotificationResponse response = notificationService.send(request);

        if (response == null) {
            return ResponseEntity.ok(ApiResponse.success(null, "Duplicate notification skipped"));
        }

        return ResponseEntity.status(HttpStatus.ACCEPTED)
                .body(ApiResponse.success(response, "Notification queued for delivery"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @Parameter(description = "Notification ID") @PathVariable UUID id) {

        NotificationResponse response = notificationService.getNotification(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get notifications by status")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByStatus(
            @Parameter(description = "Delivery status") @PathVariable DeliveryStatus status,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "50") int limit) {

        List<NotificationResponse> notifications = notificationService.getByStatus(status, limit);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @GetMapping("/recipient/{recipient}")
    @Operation(summary = "Get notifications by recipient")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getByRecipient(
            @Parameter(description = "Recipient email/ID") @PathVariable String recipient,
            @Parameter(description = "Maximum results") @RequestParam(defaultValue = "50") int limit) {

        List<NotificationResponse> notifications = notificationService.getByRecipient(recipient, limit);
        return ResponseEntity.ok(ApiResponse.success(notifications));
    }

    @PostMapping("/retry-failed")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Retry failed notifications", description = "Retries all failed notifications that haven't exceeded max retries")
    public ResponseEntity<ApiResponse<Map<String, Integer>>> retryFailed() {
        log.info("Retrying failed notifications");
        int count = notificationService.retryFailedNotifications();
        return ResponseEntity.ok(ApiResponse.success(Map.of("retriedCount", count),
                "Retried " + count + " notifications"));
    }

    @GetMapping("/stats")
    @Operation(summary = "Get notification statistics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        Map<String, Object> stats = notificationService.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // Template endpoints

    @GetMapping("/templates")
    @Operation(summary = "List notification templates")
    public ResponseEntity<ApiResponse<List<NotificationTemplate>>> getTemplates() {
        List<NotificationTemplate> templates = notificationService.getAllTemplates();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }

    @GetMapping("/templates/{id}")
    @Operation(summary = "Get template by ID")
    public ResponseEntity<ApiResponse<NotificationTemplate>> getTemplate(
            @Parameter(description = "Template ID") @PathVariable UUID id) {

        NotificationTemplate template = notificationService.getTemplate(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }

    // Config endpoints

    @GetMapping("/configs")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "List notification configurations")
    public ResponseEntity<ApiResponse<List<NotificationConfig>>> getConfigs() {
        List<NotificationConfig> configs = notificationService.getAllConfigs();
        return ResponseEntity.ok(ApiResponse.success(configs));
    }

    @GetMapping("/configs/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Get configuration by ID")
    public ResponseEntity<ApiResponse<NotificationConfig>> getConfig(
            @Parameter(description = "Config ID") @PathVariable UUID id) {

        NotificationConfig config = notificationService.getConfig(id);
        return ResponseEntity.ok(ApiResponse.success(config));
    }

    // Enums

    @GetMapping("/channels")
    @Operation(summary = "Get available notification channels")
    public ResponseEntity<ApiResponse<NotificationChannel[]>> getChannels() {
        return ResponseEntity.ok(ApiResponse.success(NotificationChannel.values()));
    }

    @GetMapping("/types")
    @Operation(summary = "Get available notification types")
    public ResponseEntity<ApiResponse<NotificationType[]>> getTypes() {
        return ResponseEntity.ok(ApiResponse.success(NotificationType.values()));
    }

    @GetMapping("/priorities")
    @Operation(summary = "Get available priorities")
    public ResponseEntity<ApiResponse<NotificationPriority[]>> getPriorities() {
        return ResponseEntity.ok(ApiResponse.success(NotificationPriority.values()));
    }
}

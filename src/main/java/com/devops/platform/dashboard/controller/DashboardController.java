package com.devops.platform.dashboard.controller;

import com.devops.platform.common.dto.ApiResponse;
import com.devops.platform.dashboard.dto.*;
import com.devops.platform.dashboard.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import com.devops.platform.auth.security.UserPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for role-based dashboards.
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Dashboard", description = "Role-based dashboard endpoints")
public class DashboardController {

    private final AdminDashboardService adminDashboardService;
    private final SreDashboardService sreDashboardService;
    private final DeveloperDashboardService developerDashboardService;
    private final ManagerDashboardService managerDashboardService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get Admin dashboard", description = "Complete system overview for administrators")
    public ResponseEntity<ApiResponse<AdminDashboard>> getAdminDashboard() {
        log.info("Fetching admin dashboard");
        AdminDashboard dashboard = adminDashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/sre")
    @PreAuthorize("hasAnyRole('ADMIN', 'SRE')")
    @Operation(summary = "Get SRE dashboard", description = "Reliability and incident focused view for SRE team")
    public ResponseEntity<ApiResponse<SreDashboard>> getSreDashboard() {
        log.info("Fetching SRE dashboard");
        SreDashboard dashboard = sreDashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/developer")
    @PreAuthorize("hasAnyRole('ADMIN', 'DEVELOPER')")
    @Operation(summary = "Get Developer dashboard", description = "Application health and performance for developers")
    public ResponseEntity<ApiResponse<DeveloperDashboard>> getDeveloperDashboard(
            @AuthenticationPrincipal UserPrincipal userDetails) {
        log.info("Fetching developer dashboard");
        UUID userId = userDetails.getId();
        DeveloperDashboard dashboard = developerDashboardService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/manager")
    @PreAuthorize("hasAnyRole('ADMIN', 'MANAGER')")
    @Operation(summary = "Get Manager dashboard", description = "Cost, trends and team performance for managers")
    public ResponseEntity<ApiResponse<ManagerDashboard>> getManagerDashboard() {
        log.info("Fetching manager dashboard");
        ManagerDashboard dashboard = managerDashboardService.getDashboard();
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    @GetMapping("/my")
    @Operation(summary = "Get dashboard for current user", description = "Returns appropriate dashboard based on user role")
    public ResponseEntity<ApiResponse<Object>> getMyDashboard(
            @AuthenticationPrincipal UserPrincipal userDetails) {
        UUID userId = userDetails.getId();
        DeveloperDashboard dashboard = developerDashboardService.getDashboard(userId);
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}

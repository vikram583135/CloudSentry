package com.devops.platform.cost.model;

/**
 * Types of optimization recommendations.
 */
public enum OptimizationType {
    TERMINATE_IDLE, // Terminate idle resources
    RIGHTSIZE, // Resize to smaller instance
    RESERVED_INSTANCE, // Switch to reserved pricing
    SPOT_INSTANCE, // Use spot/preemptible instances
    STORAGE_TIER, // Move to cheaper storage tier
    DELETE_UNUSED, // Delete unused resources
    SCHEDULE_SHUTDOWN, // Auto-shutdown during off-hours
    CONSOLIDATE, // Consolidate multiple resources
    REGION_MIGRATION, // Move to cheaper region
    SAVINGS_PLAN, // Purchase savings plans
    ARCHITECTURE_CHANGE, // Architectural optimization
    LICENSE_OPTIMIZATION, // Optimize software licenses
    OTHER
}

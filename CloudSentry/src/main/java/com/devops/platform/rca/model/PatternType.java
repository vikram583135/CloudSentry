package com.devops.platform.rca.model;

/**
 * Types of root cause patterns.
 */
public enum PatternType {
    RESOURCE_EXHAUSTION, // CPU, Memory, Disk full
    CONNECTION_ISSUE, // Database, API, network problems
    CONFIGURATION_ERROR, // Misconfigs, missing env vars
    DEPLOYMENT_RELATED, // Recent deployment issues
    DEPENDENCY_FAILURE, // Upstream/downstream service failure
    TRAFFIC_SPIKE, // Sudden load increase
    MEMORY_LEAK, // Gradual memory increase
    DISK_IO_BOTTLENECK, // Slow disk operations
    NETWORK_LATENCY, // Network delays
    DATABASE_DEADLOCK, // DB locking issues
    CACHE_MISS, // Cache invalidation/miss
    RATE_LIMITING, // API rate limits hit
    CERTIFICATE_EXPIRY, // SSL/TLS cert issues
    DNS_RESOLUTION, // DNS lookup failures
    AUTHENTICATION_FAILURE, // Auth/permission issues
    CUSTOM // Custom pattern
}

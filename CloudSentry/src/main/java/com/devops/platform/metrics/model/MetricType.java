package com.devops.platform.metrics.model;

/**
 * Types of metrics that can be collected.
 */
public enum MetricType {
    CPU_USAGE,
    MEMORY_USAGE,
    DISK_USAGE,
    NETWORK_IN,
    NETWORK_OUT,
    REQUEST_COUNT,
    ERROR_COUNT,
    RESPONSE_TIME,
    LATENCY_P50,
    LATENCY_P95,
    LATENCY_P99,
    THROUGHPUT,
    ACTIVE_CONNECTIONS,
    QUEUE_SIZE,
    CUSTOM
}

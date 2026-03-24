package com.devops.platform.analyzer.model;

/**
 * Status of an anomaly.
 */
public enum AnomalyStatus {
    OPEN,
    ACKNOWLEDGED,
    INVESTIGATING,
    RESOLVED,
    FALSE_POSITIVE
}

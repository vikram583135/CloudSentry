package com.devops.platform.analyzer.model;

/**
 * Types of anomaly detection algorithms.
 */
public enum DetectionType {
    THRESHOLD, // Static threshold violation
    Z_SCORE, // Statistical z-score based
    MOVING_AVERAGE, // Moving average deviation
    PERCENTILE, // Percentile-based detection
    RATE_OF_CHANGE, // Sudden change detection
    PATTERN_MATCH, // Pattern-based detection
    PREDICTIVE // ML/predictive-based
}

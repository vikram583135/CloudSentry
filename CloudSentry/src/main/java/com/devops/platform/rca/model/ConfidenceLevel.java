package com.devops.platform.rca.model;

/**
 * Confidence levels for root cause suggestions.
 */
public enum ConfidenceLevel {
    HIGH, // 80%+ certainty
    MEDIUM, // 50-80% certainty
    LOW, // Below 50% certainty
    SPECULATIVE // Pattern-based guess
}

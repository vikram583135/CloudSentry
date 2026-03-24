package com.devops.platform.incident.model;

/**
 * Severity levels for incidents (SEV1 = most critical).
 */
public enum IncidentSeverity {
    SEV1, // Critical - Full outage, immediate response required
    SEV2, // High - Major degradation, urgent response
    SEV3, // Medium - Partial degradation, timely response
    SEV4, // Low - Minor issue, scheduled response
    SEV5 // Informational - No immediate action required
}

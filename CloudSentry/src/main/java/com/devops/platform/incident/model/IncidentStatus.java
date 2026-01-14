package com.devops.platform.incident.model;

/**
 * Status of an incident through its lifecycle.
 */
public enum IncidentStatus {
    OPEN, // Newly created incident
    ACKNOWLEDGED, // Someone has seen and acknowledged
    INVESTIGATING, // Active investigation in progress
    IDENTIFIED, // Root cause identified
    MITIGATING, // Fix being applied
    MONITORING, // Fix applied, monitoring for stability
    RESOLVED, // Incident fully resolved
    CLOSED // Post-mortem complete, incident closed
}

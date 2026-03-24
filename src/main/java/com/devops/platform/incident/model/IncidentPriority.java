package com.devops.platform.incident.model;

/**
 * Priority levels for incident response.
 */
public enum IncidentPriority {
    P1, // Immediate - Drop everything
    P2, // High - Next available resource
    P3, // Medium - Within business hours
    P4, // Low - When convenient
    P5 // Planned - Scheduled maintenance window
}

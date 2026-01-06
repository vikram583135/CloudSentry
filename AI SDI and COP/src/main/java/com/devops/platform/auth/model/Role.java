package com.devops.platform.auth.model;

/**
 * User roles for role-based access control.
 */
public enum Role {
    ADMIN,      // Full access to all features
    SRE,        // Incident management, alerts, metrics
    DEVELOPER,  // App metrics, logs, deployment correlation
    MANAGER     // Cost analytics, SLA reports, read-only dashboards
}

package com.devops.platform.notification.model;

/**
 * Notification types/categories.
 */
public enum NotificationType {
    ANOMALY_DETECTED,
    INCIDENT_CREATED,
    INCIDENT_UPDATED,
    INCIDENT_RESOLVED,
    COST_ALERT,
    COST_RECOMMENDATION,
    THRESHOLD_BREACH,
    SYSTEM_ALERT,
    SCHEDULED_REPORT,
    CUSTOM
}

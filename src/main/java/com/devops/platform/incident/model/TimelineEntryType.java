package com.devops.platform.incident.model;

/**
 * Types of timeline entries.
 */
public enum TimelineEntryType {
    CREATED,
    STATUS_CHANGED,
    SEVERITY_CHANGED,
    PRIORITY_CHANGED,
    ASSIGNED,
    COMMENT,
    ANOMALY_LINKED,
    ROOT_CAUSE_IDENTIFIED,
    RESOLUTION_APPLIED,
    NOTIFICATION_SENT,
    ESCALATED,
    EXTERNAL_UPDATE,
    AUTOMATION_ACTION
}

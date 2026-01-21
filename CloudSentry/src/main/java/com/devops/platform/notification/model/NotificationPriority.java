package com.devops.platform.notification.model;

/**
 * Priority of notifications.
 */
public enum NotificationPriority {
    CRITICAL, // Immediate delivery, all channels
    HIGH, // Urgent, primary channels
    MEDIUM, // Standard priority
    LOW, // Can be batched/delayed
    INFO // Informational only
}

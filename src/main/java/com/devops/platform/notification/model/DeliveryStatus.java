package com.devops.platform.notification.model;

/**
 * Delivery status of a notification.
 */
public enum DeliveryStatus {
    PENDING,
    SENT,
    DELIVERED,
    FAILED,
    RETRYING,
    CANCELLED
}

package com.devops.platform.cost.model;

/**
 * Status of a cloud resource.
 */
public enum ResourceStatus {
    ACTIVE,
    IDLE,
    UNDERUTILIZED,
    STOPPED,
    TERMINATED,
    UNKNOWN
}

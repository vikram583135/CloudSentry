package com.devops.platform.cost.dto;

import com.devops.platform.cost.model.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * DTO for cloud resource response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudResourceResponse {

    private UUID id;
    private String resourceId;
    private String name;
    private CloudProvider cloudProvider;
    private ResourceType resourceType;
    private ResourceStatus status;
    private String region;
    private String instanceType;
    private UUID applicationId;
    private String applicationName;
    private String environment;
    private String team;
    private String owner;
    private BigDecimal hourlyCost;
    private BigDecimal monthlyCost;
    private String currency;
    private Double cpuUtilization;
    private Double memoryUtilization;
    private Double diskUtilization;
    private Boolean isIdle;
    private Instant idleSince;
    private BigDecimal potentialSavings;
    private Instant createdAt;
    private Instant updatedAt;
}

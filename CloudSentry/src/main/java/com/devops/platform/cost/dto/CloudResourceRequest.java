package com.devops.platform.cost.dto;

import com.devops.platform.cost.model.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating/updating cloud resource.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CloudResourceRequest {

    @NotBlank(message = "Resource ID is required")
    private String resourceId;

    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "Cloud provider is required")
    private CloudProvider cloudProvider;

    @NotNull(message = "Resource type is required")
    private ResourceType resourceType;

    private ResourceStatus status;

    private String region;

    private String availabilityZone;

    private String instanceType;

    private UUID applicationId;

    private String applicationName;

    private String environment;

    private String team;

    private String owner;

    private BigDecimal hourlyCost;

    private BigDecimal monthlyCost;

    private String currency;

    private String tags;
}

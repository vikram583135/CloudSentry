package com.devops.platform.metrics.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating an application.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String displayName;

    private String description;

    private String environment;

    private String team;

    private String owner;
}

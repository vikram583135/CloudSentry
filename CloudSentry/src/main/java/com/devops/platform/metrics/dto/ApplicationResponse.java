package com.devops.platform.metrics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for application response.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApplicationResponse {

    private UUID id;
    private String name;
    private String displayName;
    private String description;
    private String environment;
    private String team;
    private String owner;
    private String apiKey;
    private Boolean active;
    private Instant createdAt;
    private Instant updatedAt;
}

package com.devops.platform.rca.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for RCA request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RcaRequest {

    private UUID incidentId;
    private UUID anomalyId;
    private UUID applicationId;
    private boolean includeAiAnalysis;
    private boolean includeSimilarIncidents;
}

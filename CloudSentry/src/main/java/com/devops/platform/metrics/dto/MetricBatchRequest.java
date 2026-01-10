package com.devops.platform.metrics.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for submitting multiple metrics in a batch.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricBatchRequest {

    @NotEmpty(message = "Metrics list cannot be empty")
    @Valid
    private List<MetricRequest> metrics;
}

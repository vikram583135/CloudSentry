package com.devops.platform.metrics.dto;

import com.devops.platform.metrics.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * DTO for querying metrics with filters.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricQueryRequest {

    private UUID applicationId;
    private MetricType metricType;
    private String host;
    private String environment;
    private Instant startTime;
    private Instant endTime;
    
    @Builder.Default
    private Integer limit = 100;
    
    @Builder.Default
    private Integer offset = 0;
}

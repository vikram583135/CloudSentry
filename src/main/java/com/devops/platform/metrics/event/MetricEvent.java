package com.devops.platform.metrics.event;

import com.devops.platform.metrics.model.MetricType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka event for metric data.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MetricEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID eventId;
    private UUID applicationId;
    private String applicationName;
    private MetricType metricType;
    private String metricName;
    private Double value;
    private String unit;
    private String host;
    private String environment;
    private Map<String, String> tags;
    private Instant timestamp;
    private Instant receivedAt;
}

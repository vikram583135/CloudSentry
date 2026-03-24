package com.devops.platform.metrics.kafka;

import com.devops.platform.metrics.dto.MetricRequest;
import com.devops.platform.metrics.event.MetricEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Kafka producer for sending metric events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.metrics-raw:metrics-raw}")
    private String metricsTopic;

    /**
     * Sends a metric event to Kafka asynchronously.
     */
    public CompletableFuture<SendResult<String, String>> sendMetric(MetricRequest request) {
        MetricEvent event = toEvent(request);
        return sendEvent(event);
    }

    /**
     * Sends a pre-built metric event to Kafka.
     */
    public CompletableFuture<SendResult<String, String>> sendEvent(MetricEvent event) {
        try {
            String key = event.getApplicationId().toString();
            String value = objectMapper.writeValueAsString(event);

            log.debug("Sending metric event to Kafka: topic={}, key={}, type={}", 
                    metricsTopic, key, event.getMetricType());

            return kafkaTemplate.send(metricsTopic, key, value)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to send metric to Kafka: {}", ex.getMessage(), ex);
                        } else {
                            log.debug("Metric sent successfully: partition={}, offset={}",
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize metric event: {}", e.getMessage(), e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Converts a MetricRequest to a MetricEvent.
     */
    private MetricEvent toEvent(MetricRequest request) {
        return MetricEvent.builder()
                .eventId(UUID.randomUUID())
                .applicationId(request.getApplicationId())
                .applicationName(request.getApplicationName())
                .metricType(request.getMetricType())
                .metricName(request.getMetricName())
                .value(request.getValue())
                .unit(request.getUnit())
                .host(request.getHost())
                .environment(request.getEnvironment())
                .tags(request.getTags())
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now())
                .receivedAt(Instant.now())
                .build();
    }
}

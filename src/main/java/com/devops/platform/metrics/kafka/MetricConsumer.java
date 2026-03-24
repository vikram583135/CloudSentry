package com.devops.platform.metrics.kafka;

import com.devops.platform.metrics.event.MetricEvent;
import com.devops.platform.metrics.model.Metric;
import com.devops.platform.metrics.repository.MetricRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Kafka consumer for processing and persisting metric events.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricConsumer {

    private final MetricRepository metricRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${kafka.topics.metrics-raw:metrics-raw}",
            groupId = "${spring.kafka.consumer.group-id:devops-platform-metrics}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeMetric(
            @Payload String message,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            @Header(KafkaHeaders.OFFSET) long offset,
            Acknowledgment acknowledgment) {

        log.debug("Received metric from Kafka: partition={}, offset={}", partition, offset);

        try {
            MetricEvent event = objectMapper.readValue(message, MetricEvent.class);
            processMetricEvent(event);
            acknowledgment.acknowledge();
            log.debug("Successfully processed metric event: {}", event.getEventId());
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize metric event: {}", e.getMessage(), e);
            // Still acknowledge to prevent infinite retry of malformed messages
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Error processing metric event: {}", e.getMessage(), e);
            // Don't acknowledge - will be retried
            throw e;
        }
    }

    @KafkaListener(
            topics = "${kafka.topics.metrics-raw:metrics-raw}",
            groupId = "${spring.kafka.consumer.group-id:devops-platform-metrics-batch}",
            containerFactory = "batchKafkaListenerContainerFactory",
            autoStartup = "false"
    )
    public void consumeMetricBatch(
            @Payload List<String> messages,
            Acknowledgment acknowledgment) {

        log.debug("Received batch of {} metrics from Kafka", messages.size());

        try {
            List<Metric> metrics = messages.stream()
                    .map(this::parseEvent)
                    .filter(event -> event != null)
                    .map(this::toEntity)
                    .toList();

            metricRepository.saveAll(metrics);
            acknowledgment.acknowledge();
            log.debug("Successfully persisted {} metrics", metrics.size());
        } catch (Exception e) {
            log.error("Error processing metric batch: {}", e.getMessage(), e);
            throw e;
        }
    }

    private MetricEvent parseEvent(String message) {
        try {
            return objectMapper.readValue(message, MetricEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse metric event: {}", e.getMessage());
            return null;
        }
    }

    private void processMetricEvent(MetricEvent event) {
        Metric metric = toEntity(event);
        metricRepository.save(metric);
        log.debug("Persisted metric: appId={}, type={}, value={}",
                event.getApplicationId(), event.getMetricType(), event.getValue());
    }

    private Metric toEntity(MetricEvent event) {
        return Metric.builder()
                .applicationId(event.getApplicationId())
                .applicationName(event.getApplicationName())
                .metricType(event.getMetricType())
                .metricName(event.getMetricName())
                .value(event.getValue())
                .unit(event.getUnit())
                .host(event.getHost())
                .environment(event.getEnvironment())
                .tags(event.getTags() != null ? event.getTags().toString() : null)
                .timestamp(event.getTimestamp())
                .build();
    }
}

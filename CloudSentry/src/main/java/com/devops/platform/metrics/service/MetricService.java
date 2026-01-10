package com.devops.platform.metrics.service;

import com.devops.platform.common.exception.ResourceNotFoundException;
import com.devops.platform.metrics.dto.*;
import com.devops.platform.metrics.kafka.MetricProducer;
import com.devops.platform.metrics.model.Metric;
import com.devops.platform.metrics.model.MetricType;
import com.devops.platform.metrics.repository.MetricRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for metrics operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MetricService {

    private final MetricRepository metricRepository;
    private final MetricProducer metricProducer;
    private final ObjectMapper objectMapper;

    /**
     * Ingests a single metric by sending it to Kafka.
     */
    public void ingestMetric(MetricRequest request) {
        log.debug("Ingesting metric: appId={}, type={}, value={}",
                request.getApplicationId(), request.getMetricType(), request.getValue());
        
        if (request.getTimestamp() == null) {
            request.setTimestamp(Instant.now());
        }
        
        metricProducer.sendMetric(request);
    }

    /**
     * Ingests multiple metrics in a batch.
     */
    public void ingestMetricBatch(MetricBatchRequest batchRequest) {
        log.debug("Ingesting batch of {} metrics", batchRequest.getMetrics().size());
        
        batchRequest.getMetrics().forEach(request -> {
            if (request.getTimestamp() == null) {
                request.setTimestamp(Instant.now());
            }
            metricProducer.sendMetric(request);
        });
    }

    /**
     * Saves a metric directly to the database (bypassing Kafka).
     */
    @Transactional
    public MetricResponse saveMetricDirect(MetricRequest request) {
        Metric metric = Metric.builder()
                .applicationId(request.getApplicationId())
                .applicationName(request.getApplicationName())
                .metricType(request.getMetricType())
                .metricName(request.getMetricName())
                .value(request.getValue())
                .unit(request.getUnit())
                .host(request.getHost())
                .environment(request.getEnvironment())
                .tags(request.getTags() != null ? request.getTags().toString() : null)
                .timestamp(request.getTimestamp() != null ? request.getTimestamp() : Instant.now())
                .build();

        metric = metricRepository.save(metric);
        return toResponse(metric);
    }

    /**
     * Retrieves metrics by application ID.
     */
    @Cacheable(value = "metrics", key = "#applicationId + '-' + #limit")
    public List<MetricResponse> getMetricsByApplication(UUID applicationId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return metricRepository.findByApplicationIdOrderByTimestampDesc(applicationId, pageable)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves metrics by application ID and type.
     */
    public List<MetricResponse> getMetricsByApplicationAndType(UUID applicationId, MetricType type, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return metricRepository.findByApplicationIdAndMetricType(applicationId, type, pageable)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Queries metrics with various filters.
     */
    public List<MetricResponse> queryMetrics(MetricQueryRequest query) {
        Pageable pageable = PageRequest.of(query.getOffset() / query.getLimit(), query.getLimit());
        
        Instant startTime = query.getStartTime() != null ? 
                query.getStartTime() : Instant.now().minus(1, ChronoUnit.HOURS);
        Instant endTime = query.getEndTime() != null ? 
                query.getEndTime() : Instant.now();

        List<Metric> metrics;
        if (query.getMetricType() != null) {
            metrics = metricRepository.findByApplicationIdAndMetricTypeAndTimeRange(
                    query.getApplicationId(), query.getMetricType(), startTime, endTime, pageable);
        } else {
            metrics = metricRepository.findByApplicationIdAndTimeRange(
                    query.getApplicationId(), startTime, endTime, pageable);
        }

        return metrics.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets the average value for a metric type within a time range.
     */
    public Double getAverageMetricValue(UUID applicationId, MetricType type, Instant startTime, Instant endTime) {
        return metricRepository.getAverageValue(applicationId, type, startTime, endTime);
    }

    /**
     * Gets a metric by ID.
     */
    public MetricResponse getMetricById(UUID id) {
        Metric metric = metricRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Metric", "id", id));
        return toResponse(metric);
    }

    /**
     * Deletes old metrics before a cutoff time.
     */
    @Transactional
    public long deleteOldMetrics(Instant cutoffTime) {
        long count = metricRepository.count();
        metricRepository.deleteByTimestampBefore(cutoffTime);
        return count - metricRepository.count();
    }

    private MetricResponse toResponse(Metric metric) {
        Map<String, String> tags = Collections.emptyMap();
        if (metric.getTags() != null) {
            try {
                tags = objectMapper.readValue(metric.getTags(), new TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                log.warn("Failed to parse tags: {}", e.getMessage());
            }
        }

        return MetricResponse.builder()
                .id(metric.getId())
                .applicationId(metric.getApplicationId())
                .applicationName(metric.getApplicationName())
                .metricType(metric.getMetricType())
                .metricName(metric.getMetricName())
                .value(metric.getValue())
                .unit(metric.getUnit())
                .host(metric.getHost())
                .environment(metric.getEnvironment())
                .tags(tags)
                .timestamp(metric.getTimestamp())
                .createdAt(metric.getCreatedAt())
                .build();
    }
}

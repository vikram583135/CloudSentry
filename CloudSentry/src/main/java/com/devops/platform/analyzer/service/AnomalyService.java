package com.devops.platform.analyzer.service;

import com.devops.platform.analyzer.dto.AnomalyResponse;
import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.analyzer.model.AnomalyStatus;
import com.devops.platform.analyzer.repository.AnomalyRepository;
import com.devops.platform.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing anomalies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnomalyService {

    private final AnomalyRepository anomalyRepository;

    /**
     * Gets an anomaly by ID.
     */
    public AnomalyResponse getAnomaly(UUID id) {
        Anomaly anomaly = anomalyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly", "id", id));
        return toResponse(anomaly);
    }

    /**
     * Gets open anomalies.
     */
    public List<AnomalyResponse> getOpenAnomalies(int limit) {
        List<AnomalyStatus> openStatuses = Arrays.asList(
                AnomalyStatus.OPEN, AnomalyStatus.ACKNOWLEDGED, AnomalyStatus.INVESTIGATING);

        return anomalyRepository.findByStatusInOrderByDetectedAtDesc(openStatuses, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets anomalies by application.
     */
    public List<AnomalyResponse> getAnomaliesByApplication(UUID applicationId, AnomalyStatus status, int limit) {
        return anomalyRepository.findByApplicationIdAndStatusOrderByDetectedAtDesc(
                applicationId, status, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets anomalies by severity.
     */
    public List<AnomalyResponse> getAnomaliesBySeverity(AnomalySeverity severity, int limit) {
        return anomalyRepository.findBySeverityAndStatusOrderByDetectedAtDesc(
                severity, AnomalyStatus.OPEN, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Gets anomalies by time range.
     */
    public List<AnomalyResponse> getAnomaliesByTimeRange(UUID applicationId, Instant startTime, Instant endTime,
            int limit) {
        return anomalyRepository.findByApplicationIdAndTimeRange(
                applicationId, startTime, endTime, PageRequest.of(0, limit))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates anomaly status.
     */
    @Transactional
    public AnomalyResponse updateStatus(UUID id, AnomalyStatus newStatus) {
        Anomaly anomaly = anomalyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly", "id", id));

        AnomalyStatus oldStatus = anomaly.getStatus();
        anomaly.setStatus(newStatus);

        if (newStatus == AnomalyStatus.RESOLVED || newStatus == AnomalyStatus.FALSE_POSITIVE) {
            anomaly.setResolvedAt(Instant.now());
        }

        anomaly = anomalyRepository.save(anomaly);
        log.info("Anomaly status updated: id={}, {} -> {}", id, oldStatus, newStatus);

        return toResponse(anomaly);
    }

    /**
     * Acknowledges an anomaly.
     */
    @Transactional
    public AnomalyResponse acknowledge(UUID id) {
        return updateStatus(id, AnomalyStatus.ACKNOWLEDGED);
    }

    /**
     * Marks an anomaly as being investigated.
     */
    @Transactional
    public AnomalyResponse investigate(UUID id) {
        return updateStatus(id, AnomalyStatus.INVESTIGATING);
    }

    /**
     * Resolves an anomaly.
     */
    @Transactional
    public AnomalyResponse resolve(UUID id) {
        return updateStatus(id, AnomalyStatus.RESOLVED);
    }

    /**
     * Marks an anomaly as false positive.
     */
    @Transactional
    public AnomalyResponse markFalsePositive(UUID id) {
        return updateStatus(id, AnomalyStatus.FALSE_POSITIVE);
    }

    /**
     * Links an anomaly to an incident.
     */
    @Transactional
    public AnomalyResponse linkToIncident(UUID anomalyId, UUID incidentId) {
        Anomaly anomaly = anomalyRepository.findById(anomalyId)
                .orElseThrow(() -> new ResourceNotFoundException("Anomaly", "id", anomalyId));

        anomaly.setIncidentId(incidentId);
        anomaly = anomalyRepository.save(anomaly);
        log.info("Anomaly linked to incident: anomalyId={}, incidentId={}", anomalyId, incidentId);

        return toResponse(anomaly);
    }

    /**
     * Gets anomaly statistics.
     */
    public Map<String, Object> getAnomalyStats() {
        List<AnomalyStatus> openStatuses = Arrays.asList(
                AnomalyStatus.OPEN, AnomalyStatus.ACKNOWLEDGED, AnomalyStatus.INVESTIGATING);

        long openCount = anomalyRepository.countByStatusIn(openStatuses);
        List<Object[]> severityCounts = anomalyRepository.countBySeverityAndStatusIn(openStatuses);

        Map<String, Long> bySeverity = new HashMap<>();
        for (Object[] row : severityCounts) {
            AnomalySeverity severity = (AnomalySeverity) row[0];
            Long count = (Long) row[1];
            bySeverity.put(severity.name(), count);
        }

        return Map.of(
                "totalOpen", openCount,
                "bySeverity", bySeverity);
    }

    private AnomalyResponse toResponse(Anomaly anomaly) {
        return AnomalyResponse.builder()
                .id(anomaly.getId())
                .applicationId(anomaly.getApplicationId())
                .applicationName(anomaly.getApplicationName())
                .metricType(anomaly.getMetricType())
                .metricName(anomaly.getMetricName())
                .detectionType(anomaly.getDetectionType())
                .severity(anomaly.getSeverity())
                .status(anomaly.getStatus())
                .currentValue(anomaly.getCurrentValue())
                .expectedValue(anomaly.getExpectedValue())
                .thresholdValue(anomaly.getThresholdValue())
                .deviationPercentage(anomaly.getDeviationPercentage())
                .zScore(anomaly.getZScore())
                .description(anomaly.getDescription())
                .host(anomaly.getHost())
                .environment(anomaly.getEnvironment())
                .detectedAt(anomaly.getDetectedAt())
                .resolvedAt(anomaly.getResolvedAt())
                .incidentId(anomaly.getIncidentId())
                .createdAt(anomaly.getCreatedAt())
                .build();
    }
}

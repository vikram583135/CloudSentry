package com.devops.platform.incident.service;

import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.incident.model.*;
import com.devops.platform.incident.repository.IncidentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Service for auto-creating incidents from anomalies.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class IncidentAutoCreationService {

    private final IncidentRepository incidentRepository;
    private final IncidentService incidentService;

    /**
     * Creates an incident from an anomaly if appropriate.
     */
    @Transactional
    public Incident createFromAnomaly(Anomaly anomaly) {
        // Check if incident already exists for this anomaly
        if (incidentRepository.existsByTriggeredByAnomalyId(anomaly.getId())) {
            log.debug("Incident already exists for anomaly: {}", anomaly.getId());
            return null;
        }

        // Only create incidents for HIGH or CRITICAL anomalies
        if (anomaly.getSeverity() != AnomalySeverity.HIGH &&
                anomaly.getSeverity() != AnomalySeverity.CRITICAL) {
            log.debug("Anomaly severity {} not high enough for auto-incident", anomaly.getSeverity());
            return null;
        }

        IncidentSeverity severity = mapAnomalySeverity(anomaly.getSeverity());
        IncidentPriority priority = mapSeverityToPriority(severity);

        Incident incident = Incident.builder()
                .incidentNumber(generateIncidentNumber())
                .title(generateTitle(anomaly))
                .description(generateDescription(anomaly))
                .severity(severity)
                .priority(priority)
                .status(IncidentStatus.OPEN)
                .applicationId(anomaly.getApplicationId())
                .applicationName(anomaly.getApplicationName())
                .environment(anomaly.getEnvironment())
                .autoCreated(true)
                .triggeredByAnomalyId(anomaly.getId())
                .build();

        incident = incidentRepository.save(incident);

        // Add timeline entry
        IncidentTimeline timelineEntry = IncidentTimeline.builder()
                .entryType(TimelineEntryType.CREATED)
                .message("Incident auto-created from anomaly detection")
                .isAutomated(true)
                .build();
        incident.addTimelineEntry(timelineEntry);

        incident = incidentRepository.save(incident);

        log.info("Auto-created incident {} from anomaly {}: severity={}",
                incident.getIncidentNumber(), anomaly.getId(), severity);

        return incident;
    }

    /**
     * Maps anomaly severity to incident severity.
     */
    private IncidentSeverity mapAnomalySeverity(AnomalySeverity anomalySeverity) {
        return switch (anomalySeverity) {
            case CRITICAL -> IncidentSeverity.SEV1;
            case HIGH -> IncidentSeverity.SEV2;
            case MEDIUM -> IncidentSeverity.SEV3;
            case LOW -> IncidentSeverity.SEV4;
        };
    }

    /**
     * Maps incident severity to priority.
     */
    private IncidentPriority mapSeverityToPriority(IncidentSeverity severity) {
        return switch (severity) {
            case SEV1 -> IncidentPriority.P1;
            case SEV2 -> IncidentPriority.P2;
            case SEV3 -> IncidentPriority.P3;
            case SEV4 -> IncidentPriority.P4;
            case SEV5 -> IncidentPriority.P5;
        };
    }

    /**
     * Generates incident number.
     */
    private String generateIncidentNumber() {
        Integer maxNumber = incidentRepository.findMaxIncidentNumber();
        int nextNumber = (maxNumber != null ? maxNumber : 0) + 1;
        return String.format("INC-%06d", nextNumber);
    }

    /**
     * Generates incident title from anomaly.
     */
    private String generateTitle(Anomaly anomaly) {
        return String.format("[%s] %s anomaly detected - %s",
                anomaly.getSeverity(),
                anomaly.getMetricType(),
                anomaly.getApplicationName() != null ? anomaly.getApplicationName() : "Unknown App");
    }

    /**
     * Generates incident description from anomaly.
     */
    private String generateDescription(Anomaly anomaly) {
        StringBuilder sb = new StringBuilder();
        sb.append("Automatically created from anomaly detection.\n\n");
        sb.append("**Anomaly Details:**\n");
        sb.append(String.format("- Detection Type: %s\n", anomaly.getDetectionType()));
        sb.append(String.format("- Metric Type: %s\n", anomaly.getMetricType()));
        sb.append(String.format("- Current Value: %.2f\n", anomaly.getCurrentValue()));

        if (anomaly.getExpectedValue() != null) {
            sb.append(String.format("- Expected Value: %.2f\n", anomaly.getExpectedValue()));
        }
        if (anomaly.getThresholdValue() != null) {
            sb.append(String.format("- Threshold: %.2f\n", anomaly.getThresholdValue()));
        }
        if (anomaly.getDeviationPercentage() != null) {
            sb.append(String.format("- Deviation: %.1f%%\n", anomaly.getDeviationPercentage()));
        }
        if (anomaly.getZScore() != null) {
            sb.append(String.format("- Z-Score: %.2f\n", anomaly.getZScore()));
        }
        if (anomaly.getHost() != null) {
            sb.append(String.format("- Host: %s\n", anomaly.getHost()));
        }
        if (anomaly.getDescription() != null) {
            sb.append(String.format("\n**Analysis:** %s\n", anomaly.getDescription()));
        }

        return sb.toString();
    }
}

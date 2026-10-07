package com.devops.platform.incident.listener;

import com.devops.platform.analyzer.event.AnomalyDetectedEvent;
import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.incident.service.IncidentAutoCreationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener in the Incident module that responds to anomalies detected by the Analyzer module.
 * Preserves modular monolith boundaries by decoupling the detector from incident lifecycle.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IncidentEventListener {

    private final IncidentAutoCreationService autoCreationService;

    @EventListener
    public void onAnomalyDetected(AnomalyDetectedEvent event) {
        Anomaly anomaly = event.getAnomaly();
        if (anomaly == null) {
            return;
        }

        log.debug("Received AnomalyDetectedEvent for app={}, type={}, severity={}",
                anomaly.getApplicationName(), anomaly.getMetricType(), anomaly.getSeverity());

        // Automatically trigger incident creation for severe anomalies
        if (anomaly.getSeverity() == AnomalySeverity.HIGH || anomaly.getSeverity() == AnomalySeverity.CRITICAL) {
            try {
                Incident created = autoCreationService.createFromAnomaly(anomaly);
                if (created != null) {
                    log.info("Auto-created incident {} triggered by anomaly {}",
                            created.getIncidentNumber(), anomaly.getId());
                }
            } catch (Exception e) {
                log.error("Failed to auto-create incident from anomaly {}: {}", anomaly.getId(), e.getMessage(), e);
            }
        }
    }
}

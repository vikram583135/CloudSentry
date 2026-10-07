package com.devops.platform.rca.listener;

import com.devops.platform.incident.event.IncidentCreatedEvent;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.rca.dto.RcaResponse;
import com.devops.platform.rca.service.RcaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener in the RCA module that responds to new incidents created across the platform.
 * Decouples incident generation from root-cause analysis logic.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RcaEventListener {

    private final RcaService rcaService;

    @EventListener
    public void onIncidentCreated(IncidentCreatedEvent event) {
        Incident incident = event.getIncident();
        if (incident == null) {
            return;
        }

        log.debug("Received IncidentCreatedEvent: incidentId={}, number={}",
                incident.getId(), incident.getIncidentNumber());

        try {
            RcaResponse rca = rcaService.analyzeIncident(incident.getId());
            log.info("Auto-analyzed root cause for incident {}: pattern={}, confidence={}",
                    incident.getIncidentNumber(), rca.getPatternType(), rca.getConfidenceLevel());
        } catch (Exception e) {
            log.warn("Automatic RCA analysis for incident {} encountered an error: {}",
                    incident.getIncidentNumber(), e.getMessage());
        }
    }
}

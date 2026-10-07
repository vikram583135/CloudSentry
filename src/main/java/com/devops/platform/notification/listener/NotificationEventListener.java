package com.devops.platform.notification.listener;

import com.devops.platform.incident.event.IncidentCreatedEvent;
import com.devops.platform.incident.event.IncidentStatusChangedEvent;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.notification.dto.SendNotificationRequest;
import com.devops.platform.notification.model.NotificationChannel;
import com.devops.platform.notification.model.NotificationPriority;
import com.devops.platform.notification.model.NotificationType;
import com.devops.platform.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Event listener in the Notification module that listens to incident events.
 * Dispatches automated alerts to Slack, Email, and Webhook channels.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;

    @Value("${app.notification.default-recipient:alerts@cloudsentry.local}")
    private String defaultRecipient;

    @EventListener
    public void onIncidentCreated(IncidentCreatedEvent event) {
        Incident incident = event.getIncident();
        if (incident == null) {
            return;
        }

        log.debug("NotificationListener: processing IncidentCreatedEvent for {}", incident.getIncidentNumber());

        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("incidentNumber", incident.getIncidentNumber());
            vars.put("title", incident.getTitle());
            vars.put("severity", incident.getSeverity().name());
            vars.put("priority", incident.getPriority() != null ? incident.getPriority().name() : "P3");
            vars.put("application", incident.getApplicationName() != null ? incident.getApplicationName() : "System");
            vars.put("environment", incident.getEnvironment() != null ? incident.getEnvironment() : "prod");

            NotificationPriority priority = mapSeverityToNotificationPriority(incident.getSeverity().name());

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .notificationType(NotificationType.INCIDENT_CREATED)
                    .channel(NotificationChannel.SLACK)
                    .priority(priority)
                    .recipient(defaultRecipient)
                    .recipientName("DevOps Alert Team")
                    .subject(String.format("🚨 [%s] Incident %s: %s",
                            incident.getSeverity(), incident.getIncidentNumber(), incident.getTitle()))
                    .content(String.format("New incident %s (%s) triggered for %s [%s]:\n%s",
                            incident.getIncidentNumber(), incident.getSeverity(),
                            incident.getApplicationName(), incident.getEnvironment(), incident.getDescription()))
                    .referenceType("INCIDENT")
                    .referenceId(incident.getId())
                    .applicationId(incident.getApplicationId())
                    .templateVariables(vars)
                    .dedupKey("INCIDENT_CREATED_" + incident.getId())
                    .build();

            notificationService.send(request);
            log.info("Dispatched notification for created incident {}", incident.getIncidentNumber());
        } catch (Exception e) {
            log.warn("Failed to dispatch notification for incident {}: {}", incident.getIncidentNumber(), e.getMessage());
        }
    }

    @EventListener
    public void onIncidentStatusChanged(IncidentStatusChangedEvent event) {
        Incident incident = event.getIncident();
        if (incident == null) {
            return;
        }

        log.debug("NotificationListener: processing IncidentStatusChangedEvent for {} ({} -> {})",
                incident.getIncidentNumber(), event.getPreviousStatus(), event.getNewStatus());

        try {
            Map<String, Object> vars = new HashMap<>();
            vars.put("incidentNumber", incident.getIncidentNumber());
            vars.put("previousStatus", event.getPreviousStatus().name());
            vars.put("newStatus", event.getNewStatus().name());
            vars.put("comment", event.getComment() != null ? event.getComment() : "");

            SendNotificationRequest request = SendNotificationRequest.builder()
                    .notificationType(NotificationType.INCIDENT_RESOLVED)
                    .channel(NotificationChannel.SLACK)
                    .priority(NotificationPriority.MEDIUM)
                    .recipient(defaultRecipient)
                    .recipientName("DevOps Alert Team")
                    .subject(String.format("ℹ️ Incident %s transitioned to %s",
                            incident.getIncidentNumber(), event.getNewStatus()))
                    .content(String.format("Incident %s status updated: %s -> %s. %s",
                            incident.getIncidentNumber(), event.getPreviousStatus(),
                            event.getNewStatus(), event.getComment() != null ? "\nComment: " + event.getComment() : ""))
                    .referenceType("INCIDENT")
                    .referenceId(incident.getId())
                    .applicationId(incident.getApplicationId())
                    .templateVariables(vars)
                    .dedupKey("INCIDENT_STATUS_" + incident.getId() + "_" + event.getNewStatus())
                    .build();

            notificationService.send(request);
        } catch (Exception e) {
            log.warn("Failed to dispatch status change notification for incident {}: {}",
                    incident.getIncidentNumber(), e.getMessage());
        }
    }

    private NotificationPriority mapSeverityToNotificationPriority(String severity) {
        if ("SEV1".equalsIgnoreCase(severity)) return NotificationPriority.CRITICAL;
        if ("SEV2".equalsIgnoreCase(severity)) return NotificationPriority.HIGH;
        if ("SEV3".equalsIgnoreCase(severity)) return NotificationPriority.MEDIUM;
        return NotificationPriority.LOW;
    }
}

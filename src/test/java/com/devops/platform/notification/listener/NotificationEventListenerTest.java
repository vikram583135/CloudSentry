package com.devops.platform.notification.listener;

import com.devops.platform.incident.event.IncidentCreatedEvent;
import com.devops.platform.incident.event.IncidentStatusChangedEvent;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.incident.model.IncidentSeverity;
import com.devops.platform.incident.model.IncidentStatus;
import com.devops.platform.notification.dto.NotificationResponse;
import com.devops.platform.notification.dto.SendNotificationRequest;
import com.devops.platform.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationEventListener notificationEventListener;

    @Test
    @DisplayName("Should dispatch notification when IncidentCreatedEvent is received")
    void shouldDispatchNotificationOnIncidentCreated() {
        Incident incident = Incident.builder()
                .id(UUID.randomUUID())
                .incidentNumber("INC-000099")
                .title("Database connection timeout")
                .description("Hikari pool exhaustion")
                .severity(IncidentSeverity.SEV1)
                .applicationName("payment-service")
                .environment("prod")
                .build();

        IncidentCreatedEvent event = new IncidentCreatedEvent(this, incident);
        when(notificationService.send(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.builder().build());

        notificationEventListener.onIncidentCreated(event);

        verify(notificationService, times(1)).send(any(SendNotificationRequest.class));
    }

    @Test
    @DisplayName("Should dispatch notification when IncidentStatusChangedEvent is received")
    void shouldDispatchNotificationOnIncidentStatusChanged() {
        Incident incident = Incident.builder()
                .id(UUID.randomUUID())
                .incidentNumber("INC-000099")
                .severity(IncidentSeverity.SEV2)
                .build();

        IncidentStatusChangedEvent event = new IncidentStatusChangedEvent(
                this, incident, IncidentStatus.OPEN, IncidentStatus.RESOLVED, UUID.randomUUID(), "Resolved via scale up");
        when(notificationService.send(any(SendNotificationRequest.class)))
                .thenReturn(NotificationResponse.builder().build());

        notificationEventListener.onIncidentStatusChanged(event);

        verify(notificationService, times(1)).send(any(SendNotificationRequest.class));
    }
}

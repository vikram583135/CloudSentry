package com.devops.platform.incident.listener;

import com.devops.platform.analyzer.event.AnomalyDetectedEvent;
import com.devops.platform.analyzer.model.Anomaly;
import com.devops.platform.analyzer.model.AnomalySeverity;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.incident.service.IncidentAutoCreationService;
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
class IncidentEventListenerTest {

    @Mock
    private IncidentAutoCreationService autoCreationService;

    @InjectMocks
    private IncidentEventListener incidentEventListener;

    @Test
    @DisplayName("Should auto-create incident when critical anomaly event is received")
    void shouldAutoCreateIncidentOnCriticalAnomaly() {
        Anomaly criticalAnomaly = Anomaly.builder()
                .id(UUID.randomUUID())
                .applicationName("payment-service")
                .severity(AnomalySeverity.CRITICAL)
                .build();

        AnomalyDetectedEvent event = new AnomalyDetectedEvent(this, criticalAnomaly);
        when(autoCreationService.createFromAnomaly(criticalAnomaly))
                .thenReturn(Incident.builder().incidentNumber("INC-000001").build());

        incidentEventListener.onAnomalyDetected(event);

        verify(autoCreationService, times(1)).createFromAnomaly(criticalAnomaly);
    }

    @Test
    @DisplayName("Should ignore low severity anomaly events for auto incident creation")
    void shouldIgnoreLowSeverityAnomaly() {
        Anomaly lowAnomaly = Anomaly.builder()
                .id(UUID.randomUUID())
                .applicationName("payment-service")
                .severity(AnomalySeverity.LOW)
                .build();

        AnomalyDetectedEvent event = new AnomalyDetectedEvent(this, lowAnomaly);

        incidentEventListener.onAnomalyDetected(event);

        verify(autoCreationService, never()).createFromAnomaly(any());
    }
}

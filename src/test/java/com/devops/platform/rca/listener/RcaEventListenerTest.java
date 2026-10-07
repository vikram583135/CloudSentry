package com.devops.platform.rca.listener;

import com.devops.platform.incident.event.IncidentCreatedEvent;
import com.devops.platform.incident.model.Incident;
import com.devops.platform.rca.dto.RcaResponse;
import com.devops.platform.rca.model.ConfidenceLevel;
import com.devops.platform.rca.model.PatternType;
import com.devops.platform.rca.service.RcaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RcaEventListenerTest {

    @Mock
    private RcaService rcaService;

    @InjectMocks
    private RcaEventListener rcaEventListener;

    @Test
    @DisplayName("Should automatically trigger RCA analysis when IncidentCreatedEvent is received")
    void shouldTriggerRcaOnIncidentCreated() {
        UUID incidentId = UUID.randomUUID();
        Incident incident = Incident.builder()
                .id(incidentId)
                .incidentNumber("INC-000042")
                .build();

        IncidentCreatedEvent event = new IncidentCreatedEvent(this, incident);
        when(rcaService.analyzeIncident(incidentId)).thenReturn(RcaResponse.builder()
                .patternType(PatternType.CONNECTION_ISSUE)
                .confidenceLevel(ConfidenceLevel.HIGH)
                .build());

        rcaEventListener.onIncidentCreated(event);

        verify(rcaService, times(1)).analyzeIncident(incidentId);
    }
}

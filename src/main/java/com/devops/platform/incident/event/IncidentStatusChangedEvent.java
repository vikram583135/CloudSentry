package com.devops.platform.incident.event;

import com.devops.platform.incident.model.Incident;
import com.devops.platform.incident.model.IncidentStatus;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

/**
 * Domain event published by the Incident module when an incident's status transitions.
 */
@Getter
public class IncidentStatusChangedEvent extends ApplicationEvent {

    private final Incident incident;
    private final IncidentStatus previousStatus;
    private final IncidentStatus newStatus;
    private final UUID changedByUserId;
    private final String comment;

    public IncidentStatusChangedEvent(Object source, Incident incident, IncidentStatus previousStatus,
                                      IncidentStatus newStatus, UUID changedByUserId, String comment) {
        super(source);
        this.incident = incident;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedByUserId = changedByUserId;
        this.comment = comment;
    }
}

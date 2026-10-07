package com.devops.platform.incident.event;

import com.devops.platform.incident.model.Incident;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published by the Incident module when an incident is created.
 * Enables loose coupling across modular monolith boundaries.
 */
@Getter
public class IncidentCreatedEvent extends ApplicationEvent {

    private final Incident incident;

    public IncidentCreatedEvent(Object source, Incident incident) {
        super(source);
        this.incident = incident;
    }
}

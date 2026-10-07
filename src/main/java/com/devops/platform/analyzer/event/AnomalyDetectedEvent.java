package com.devops.platform.analyzer.event;

import com.devops.platform.analyzer.model.Anomaly;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * Domain event published by the Analyzer module when an anomaly is detected.
 * Enables loose coupling across modular monolith boundaries.
 */
@Getter
public class AnomalyDetectedEvent extends ApplicationEvent {

    private final Anomaly anomaly;

    public AnomalyDetectedEvent(Object source, Anomaly anomaly) {
        super(source);
        this.anomaly = anomaly;
    }
}

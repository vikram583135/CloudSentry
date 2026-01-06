package com.devops.platform.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

/**
 * Kafka Configuration
 * 
 * Defines Kafka topics for async message processing.
 */
@Configuration
public class KafkaConfig {

    // Metrics topics
    public static final String TOPIC_METRICS_INGEST = "metrics.ingest";
    public static final String TOPIC_METRICS_PROCESSED = "metrics.processed";

    // Anomaly topics
    public static final String TOPIC_ANOMALY_DETECTED = "anomaly.detected";
    public static final String TOPIC_ANOMALY_RESOLVED = "anomaly.resolved";

    // Incident topics
    public static final String TOPIC_INCIDENT_CREATED = "incident.created";
    public static final String TOPIC_INCIDENT_UPDATED = "incident.updated";
    public static final String TOPIC_INCIDENT_RESOLVED = "incident.resolved";

    // Notification topics
    public static final String TOPIC_NOTIFICATION_SEND = "notification.send";
    public static final String TOPIC_NOTIFICATION_DLQ = "notification.dlq";

    // Cost topics
    public static final String TOPIC_COST_ALERT = "cost.alert";

    @Bean
    public NewTopic metricsIngestTopic() {
        return TopicBuilder.name(TOPIC_METRICS_INGEST)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic metricsProcessedTopic() {
        return TopicBuilder.name(TOPIC_METRICS_PROCESSED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic anomalyDetectedTopic() {
        return TopicBuilder.name(TOPIC_ANOMALY_DETECTED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic anomalyResolvedTopic() {
        return TopicBuilder.name(TOPIC_ANOMALY_RESOLVED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic incidentCreatedTopic() {
        return TopicBuilder.name(TOPIC_INCIDENT_CREATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic incidentUpdatedTopic() {
        return TopicBuilder.name(TOPIC_INCIDENT_UPDATED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic incidentResolvedTopic() {
        return TopicBuilder.name(TOPIC_INCIDENT_RESOLVED)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationSendTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATION_SEND)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic notificationDlqTopic() {
        return TopicBuilder.name(TOPIC_NOTIFICATION_DLQ)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic costAlertTopic() {
        return TopicBuilder.name(TOPIC_COST_ALERT)
                .partitions(3)
                .replicas(1)
                .build();
    }
}

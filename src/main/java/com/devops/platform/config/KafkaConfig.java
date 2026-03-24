package com.devops.platform.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration
 * 
 * Defines Kafka topics and consumer factories for async message processing.
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:devops-platform}")
    private String groupId;

    // Metrics topics
    public static final String TOPIC_METRICS_RAW = "metrics-raw";
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

    // Consumer factory for single message processing
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(3);
        return factory;
    }

    // Consumer factory for batch processing
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String> batchKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, String> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(true);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
        factory.setConcurrency(2);
        return factory;
    }

    // Topic definitions
    @Bean
    public NewTopic metricsRawTopic() {
        return TopicBuilder.name(TOPIC_METRICS_RAW)
                .partitions(6)
                .replicas(1)
                .build();
    }

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
